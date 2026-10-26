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

/** Respiração das Chamas: dano alto e fogo. */
public final class FlameBreathing {
	private FlameBreathing() {}

	public static List<BreathingForm> forms() {
		return List.of(
				new BreathingForm(1, 15f, 30, FlameBreathing::unknowingFire),
				new BreathingForm(2, 20f, 45, FlameBreathing::risingScorchingSun),
				new BreathingForm(5, 28f, 70, FlameBreathing::flameTiger));
	}

	/** 1ª forma: investida flamejante que queima tudo no caminho. */
	private static void unknowingFire(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.FIRECHARGE_USE, 1.0f);
		FormUtil.dash(p, 1.6, 0.05);
		for (int delay : new int[]{1, 3, 5}) {
			DelayedTasks.schedule(delay, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				FormUtil.burst(lvl, ParticleTypes.FLAME, p.position().add(0, 1, 0), 25, 0.5, 0.05);
				for (LivingEntity e : FormUtil.radius(p, 2.6)) {
					FormUtil.hit(p, e, 5f, 0.3);
					e.setRemainingFireTicks(80);
				}
			});
		}
	}

	/** 2ª forma: corte ascendente que lança os inimigos pro alto. */
	private static void risingScorchingSun(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_STRONG, 0.9f);
		FormUtil.dash(p, 0.2, 0.55);
		DelayedTasks.schedule(2, () -> {
			if (!FormUtil.ok(p)) return;
			ServerLevel lvl = FormUtil.level(p);
			FormUtil.arc(lvl, ParticleTypes.FLAME, p, 2.5, 160, 0.5, 18);
			FormUtil.arc(lvl, ParticleTypes.FLAME, p, 3.2, 140, 1.5, 18);
			FormUtil.arc(lvl, ParticleTypes.LARGE_SMOKE, p, 2.8, 120, 2.3, 10);
			for (LivingEntity e : FormUtil.cone(p, 4.0, 120)) {
				FormUtil.hit(p, e, 7f, 0.2);
				FormUtil.launch(e, 0.85);
				e.setRemainingFireTicks(100);
			}
		});
	}

	/** 5ª forma: tigre de fogo em linha reta, longo alcance. */
	private static void flameTiger(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.BLAZE_SHOOT, 0.7f);
		DelayedTasks.schedule(3, () -> {
			if (!FormUtil.ok(p)) return;
			ServerLevel lvl = FormUtil.level(p);
			Vec3 eye = p.getEyePosition();
			Vec3 dir = p.getLookAngle().normalize();
			FormUtil.trail(lvl, ParticleTypes.FLAME, eye.add(dir), eye.add(dir.scale(10)), 0.5, 6, 0.35);
			FormUtil.trail(lvl, ParticleTypes.LARGE_SMOKE, eye.add(dir), eye.add(dir.scale(10)), 1.0, 2, 0.4);
			for (LivingEntity e : FormUtil.line(p, 10, 1.4)) {
				FormUtil.hit(p, e, 9f, 0.6);
				e.setRemainingFireTicks(120);
			}
		});
	}
}
