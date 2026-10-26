package com.demonslayer.breathing;

import com.demonslayer.attachment.ModAttachments;
import com.demonslayer.item.ModItems;
import com.demonslayer.network.BreathSyncPayload;
import com.demonslayer.network.BreathingActionPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Toda a lógica de servidor do sistema de respiração. */
public final class BreathManager {
	// ----- Ajustes de balanceamento (tudo por tick; 20 ticks = 1s) -----
	public static final float REGEN_STILL = 0.20f;         // parado: 4/s
	public static final float REGEN_WALKING = 0.10f;       // andando: 2/s
	public static final float REGEN_CONCENTRATING = 0.75f; // segurando a tecla: 15/s
	public static final float DAMAGE_BREATH_LOSS = 1.5f;   // fôlego perdido por ponto de vida perdido
	public static final int EXHAUSTION_TICKS = 100;        // 5s sem fôlego
	public static final float RECOVERY_BREATH = 20f;       // fôlego ao sair da exaustão
	/** Enquanto não existir o sistema de classes, qualquer jogador pode trocar de estilo com a tecla. */
	public static final boolean ALLOW_FREE_STYLE_SWITCH = true;

	private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();

	private BreathManager() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			DelayedTasks.tick();
			tick(server);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_HEALTH.remove(handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			DelayedTasks.clear();
			LAST_HEALTH.clear();
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			BreathData d = get(newPlayer);
			d.breath = BreathData.MAX_BREATH;
			d.cooldown = 0;
			d.exhausted = 0;
			d.interrupted = 0;
			d.concentrating = false;
			LAST_HEALTH.remove(newPlayer.getUUID());
			sync(newPlayer);
		});
	}

	public static BreathData get(ServerPlayer player) {
		return player.getAttachedOrCreate(ModAttachments.BREATH);
	}

	// ---------------------------------------------------------------- tick

	private static void tick(MinecraftServer server) {
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			tickPlayer(p);
		}
	}

	private static void tickPlayer(ServerPlayer p) {
		BreathData d = get(p);

		if (d.cooldown > 0) d.cooldown--;
		if (d.interrupted > 0) d.interrupted--;

		// Levar dano tira o fôlego e quebra a concentração.
		float health = p.getHealth();
		float last = LAST_HEALTH.getOrDefault(p.getUUID(), health);
		if (health < last) {
			d.breath -= (last - health) * DAMAGE_BREATH_LOSS;
			d.interrupted = 20;
		}
		LAST_HEALTH.put(p.getUUID(), health);

		boolean canBreathe = p.getAirSupply() >= p.getMaxAirSupply(); // debaixo d'água não dá pra respirar

		if (d.exhausted > 0) {
			d.exhausted--;
			if (d.exhausted == 0) d.breath = RECOVERY_BREATH;
		} else if (canBreathe) {
			if (d.concentrating && d.interrupted <= 0 && p.isAlive()) {
				d.breath += REGEN_CONCENTRATING;
				p.setSprinting(false);
				p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 1, false, false, false));
				if (p.tickCount % 10 == 0) {
					ServerLevel level = (ServerLevel) p.level();
					level.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getEyeY() - 0.2, p.getZ(), 3, 0.2, 0.1, 0.2, 0.01);
				}
			} else if (p.isSprinting()) {
				// correndo: sem recuperar
			} else if (p.getDeltaMovement().horizontalDistanceSqr() < 0.0004) {
				d.breath += REGEN_STILL;
			} else {
				d.breath += REGEN_WALKING;
			}
		}

		d.breath = Mth.clamp(d.breath, 0f, BreathData.MAX_BREATH);

		if (d.breath <= 0f && d.exhausted <= 0) {
			startExhaustion(p, d);
		}

		if (p.tickCount % 2 == 0) sync(p, d);
	}

	private static void startExhaustion(ServerPlayer p, BreathData d) {
		d.breath = 0f;
		d.exhausted = EXHAUSTION_TICKS;
		p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EXHAUSTION_TICKS, 1, false, true, true));
		p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EXHAUSTION_TICKS, 0, false, true, true));
		message(p, "message.demonslayer.exhausted");
	}

	// ------------------------------------------------------------- actions

	public static void handleAction(ServerPlayer p, int action) {
		BreathData d = get(p);
		int formCount = d.style().forms().size();
		switch (action) {
			case BreathingActionPayload.USE_FORM -> useForm(p, d);
			case BreathingActionPayload.NEXT_FORM -> d.form = Math.floorMod(d.form + 1, formCount);
			case BreathingActionPayload.PREV_FORM -> d.form = Math.floorMod(d.form - 1, formCount);
			case BreathingActionPayload.NEXT_STYLE -> {
				if (ALLOW_FREE_STYLE_SWITCH) {
					d.style = Math.floorMod(d.style + 1, BreathingStyle.values().length);
					d.form = 0;
				}
			}
			case BreathingActionPayload.START_CONCENTRATE -> d.concentrating = true;
			case BreathingActionPayload.STOP_CONCENTRATE -> d.concentrating = false;
			default -> { }
		}
		sync(p, d);
	}

	private static void useForm(ServerPlayer p, BreathData d) {
		if (!p.isAlive() || p.isSpectator()) return;

		if (!p.getMainHandItem().is(ModItems.NICHIRIN_KATANA)) {
			message(p, "message.demonslayer.need_katana");
			return;
		}
		if (d.exhausted > 0) {
			message(p, "message.demonslayer.exhausted");
			return;
		}
		if (d.cooldown > 0) return;

		BreathingForm form = d.currentForm();
		if (d.breath < form.cost()) {
			message(p, "message.demonslayer.no_breath");
			return;
		}

		d.breath -= form.cost();
		d.cooldown = form.cooldown();

		form.action().execute(p, (ServerLevel) p.level());
		p.getMainHandItem().hurtAndBreak(1, p, EquipmentSlot.MAINHAND);

		if (d.breath <= 0.01f) startExhaustion(p, d);
	}

	// ---------------------------------------------------------------- misc

	public static void message(ServerPlayer p, String key) {
		p.displayClientMessage(Component.translatable(key), true);
	}

	public static void sync(ServerPlayer p) {
		sync(p, get(p));
	}

	public static void sync(ServerPlayer p, BreathData d) {
		ServerPlayNetworking.send(p, new BreathSyncPayload(d.breath, d.style, d.form, d.cooldown, d.exhausted, d.concentrating));
	}

	public static void restore(ServerPlayer p) {
		BreathData d = get(p);
		d.breath = BreathData.MAX_BREATH;
		d.exhausted = 0;
		d.cooldown = 0;
		sync(p, d);
	}
}
