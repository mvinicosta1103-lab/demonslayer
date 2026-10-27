package com.demonslayer.breathing.styles;

import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.DelayedTasks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Respiração da Pedra: lenta, pesada e defensiva. */
public final class StoneBreathing {
	private StoneBreathing() {}

	public static List<BreathingForm> forms() {
		return List.of(
				new BreathingForm(1, 22f, 55, StoneBreathing::serpentiniteBipolar),
				new BreathingForm(2, 28f, 80, StoneBreathing::upperSmash),
				new BreathingForm(3, 20f, 200, StoneBreathing::stoneSkin));
	}

	/** 1ª forma: dois golpes pesados com grande empurrão. */
	private static void serpentiniteBipolar(ServerPlayer p, ServerLevel l) {
		for (int delay : new int[]{1, 9}) {
			DelayedTasks.schedule(delay, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				FormUtil.sound(lvl, p, SoundEvents.ANVIL_LAND, 0.7f);
				FormUtil.arc(lvl, FormUtil.STONE_PARTICLE, p, 2.8, 110, 0.8, 16);
				FormUtil.burst(lvl, FormUtil.STONE_PARTICLE, p.position().add(FormUtil.flatLook(p).scale(2.5)).add(0, 1, 0), 25, 0.6, 0.1);
				for (LivingEntity e : FormUtil.cone(p, 4.0, 110)) FormUtil.hit(p, e, 8f, 1.2);
			});
		}
	}

	/** 2ª forma: salta e esmaga o chão, atordoando todos ao redor. */
	private static void upperSmash(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_STRONG, 0.6f);
		FormUtil.dash(p, 0.3, 0.95);
		DelayedTasks.schedule(8, () -> {
			if (!FormUtil.ok(p)) return;
			Vec3 f = FormUtil.flatLook(p);
			p.setDeltaMovement(f.x * 0.2, -2.0, f.z * 0.2);
			p.hurtMarked = true;
		});
		FormUtil.onLand(p, 10, 40, pl -> {
			ServerLevel lvl = FormUtil.level(pl);
			Vec3 pos = pl.position();
			FormUtil.sound(lvl, pl, SoundEvents.ANVIL_LAND, 0.5f);
			FormUtil.ring(lvl, FormUtil.STONE_PARTICLE, pos.add(0, 0.2, 0), 2.0, 20);
			FormUtil.ring(lvl, FormUtil.STONE_PARTICLE, pos.add(0, 0.2, 0), 4.0, 36);
			FormUtil.burst(lvl, FormUtil.STONE_PARTICLE, pos.add(0, 0.5, 0), 60, 1.5, 0.2);
			for (LivingEntity e : FormUtil.radius(pl, 4.5)) {
				FormUtil.hit(pl, e, 9f, 0.9);
				e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
			}
		});
	}

	/** 3ª forma: pele de pedra. Muita resistência a dano, mas você fica pesado. */
	private static void stoneSkin(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.ANVIL_LAND, 1.5f);
		FormUtil.burst(l, FormUtil.STONE_PARTICLE, p.position().add(0, 1, 0), 50, 0.6, 0.05);
		p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
		p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 0));
	}
}
