package com.demonslayer.breathing.styles;

import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.DelayedTasks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Respiração da Água: fluida, mobilidade + controle de grupo. */
public final class WaterBreathing {
	private WaterBreathing() {}

	public static List<BreathingForm> forms() {
		return List.of(
				new BreathingForm(1, 15f, 25, WaterBreathing::surfaceSlash),
				new BreathingForm(2, 20f, 40, WaterBreathing::waterWheel),
				new BreathingForm(6, 30f, 80, WaterBreathing::whirlpool));
	}

	/** 1ª forma: investida rápida com dois cortes na frente. */
	private static void surfaceSlash(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f);
		FormUtil.dash(p, 1.4, 0.1);
		for (int delay : new int[]{1, 4}) {
			DelayedTasks.schedule(delay, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				FormUtil.arc(lvl, ParticleTypes.SPLASH, p, 2.5, 140, 1.0, 14);
				FormUtil.burst(lvl, ParticleTypes.BUBBLE, p.position().add(0, 1, 0), 12, 0.5, 0.1);
				for (LivingEntity e : FormUtil.cone(p, 3.5, 110)) FormUtil.hit(p, e, 5f, 0.6);
			});
		}
	}

	/** 2ª forma: salto giratório com dois golpes em área. */
	private static void waterWheel(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_SWEEP, 0.8f);
		FormUtil.dash(p, 0.5, 0.9);
		for (int delay : new int[]{6, 12}) {
			DelayedTasks.schedule(delay, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				p.fallDistance = 0;
				FormUtil.ring(lvl, ParticleTypes.SPLASH, p.position().add(0, 1, 0), 2.8, 28);
				FormUtil.ring(lvl, ParticleTypes.BUBBLE, p.position().add(0, 1.5, 0), 2.2, 18);
				for (LivingEntity e : FormUtil.radius(p, 3.2)) FormUtil.hit(p, e, 6f, 0.7);
			});
		}
	}

	/** 6ª forma: redemoinho que puxa inimigos para perto e corta várias vezes. */
	private static void whirlpool(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.TRIDENT_RIPTIDE_1, 1.0f);
		for (int i = 0; i < 4; i++) {
			DelayedTasks.schedule(i * 3 + 1, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				Vec3 center = p.position();
				FormUtil.ring(lvl, ParticleTypes.SPLASH, center.add(0, 0.5, 0), 4.5, 30);
				FormUtil.ring(lvl, ParticleTypes.BUBBLE, center.add(0, 1.2, 0), 3.0, 20);
				for (LivingEntity e : FormUtil.radius(p, 5.0)) {
					FormUtil.pull(e, center, 0.45);
					FormUtil.hit(p, e, 4f, 0.0);
				}
			});
		}
	}
}
