package com.demonslayer.breathing.styles;

import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.DelayedTasks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Respiração do Trovão: velocidade extrema. */
public final class ThunderBreathing {
	private ThunderBreathing() {}

	public static List<BreathingForm> forms() {
		return List.of(
				new BreathingForm(1, 20f, 40, ThunderBreathing::thunderclapAndFlash),
				new BreathingForm(2, 25f, 60, ThunderBreathing::riceSpirit),
				new BreathingForm(6, 35f, 100, ThunderBreathing::rumbleAndFlash));
	}

	/** 1ª forma: teleporte relâmpago para frente, cortando todos no caminho. */
	private static void thunderclapAndFlash(ServerPlayer p, ServerLevel l) {
		Vec3 start = p.position();
		Vec3 dir = FormUtil.flatLook(p);
		Vec3 end = start;
		for (double d = 0.5; d <= 12.0; d += 0.5) {
			Vec3 next = start.add(dir.scale(d));
			AABB box = p.getBoundingBox().move(next.subtract(start));
			if (!l.noCollision(p, box) || !l.getWorldBorder().isWithinBounds(box)) break;
			end = next;
		}

		FormUtil.visualLightning(l, start);
		FormUtil.sound(l, p, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0f);
		Vec3 lift = new Vec3(0, 1, 0);
		FormUtil.trail(l, ParticleTypes.ELECTRIC_SPARK, start.add(lift), end.add(lift), 0.4, 4, 0.3);

		for (LivingEntity e : FormUtil.segment(p, start.add(lift), end.add(lift), 1.6)) {
			FormUtil.hit(p, e, 8f, 0.3);
			e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3)); // "atordoado"
		}

		p.connection.teleport(end.x, end.y, end.z, p.getYRot(), p.getXRot());
	}

	/** 2ª forma: cinco cortes em sequência ao redor do jogador. */
	private static void riceSpirit(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.8f);
		for (int i = 0; i < 5; i++) {
			DelayedTasks.schedule(i * 2 + 1, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				FormUtil.burst(lvl, ParticleTypes.ELECTRIC_SPARK, p.position().add(0, 1, 0), 40, 1.2, 0.3);
				FormUtil.ring(lvl, ParticleTypes.ELECTRIC_SPARK, p.position().add(0, 1, 0), 3.0, 20);
				for (LivingEntity e : FormUtil.radius(p, 3.6)) FormUtil.hit(p, e, 3.5f, 0.2);
			});
		}
	}

	/** 6ª forma: sobe aos céus e despenca como um raio. */
	private static void rumbleAndFlash(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.6f);
		FormUtil.dash(p, 0.0, 1.25);
		DelayedTasks.schedule(8, () -> {
			if (!FormUtil.ok(p)) return;
			Vec3 f = FormUtil.flatLook(p);
			p.setDeltaMovement(f.x * 0.5, -2.2, f.z * 0.5);
			p.hurtMarked = true;
		});
		FormUtil.onLand(p, 10, 40, ThunderBreathing::slam);
	}

	private static void slam(ServerPlayer p) {
		ServerLevel lvl = FormUtil.level(p);
		Vec3 pos = p.position();
		FormUtil.visualLightning(lvl, pos);
		FormUtil.burst(lvl, ParticleTypes.ELECTRIC_SPARK, pos.add(0, 0.5, 0), 80, 1.5, 0.4);
		FormUtil.ring(lvl, ParticleTypes.ELECTRIC_SPARK, pos.add(0, 0.2, 0), 4.0, 36);
		FormUtil.sound(lvl, p, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2f);
		for (LivingEntity e : FormUtil.radius(p, 4.0)) FormUtil.hit(p, e, 10f, 1.0);
	}
}
