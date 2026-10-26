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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Respiração do Vento: controle de área e arremesso. */
public final class WindBreathing {
	private WindBreathing() {}

	public static List<BreathingForm> forms() {
		return List.of(
				new BreathingForm(1, 18f, 35, WindBreathing::dustWhirlwindCutter),
				new BreathingForm(2, 22f, 50, WindBreathing::clawsPurifyingWind),
				new BreathingForm(4, 30f, 90, WindBreathing::risingDustStorm));
	}

	/** 1ª forma: redemoinho que corta em linha e empurra para longe. */
	private static void dustWhirlwindCutter(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_SWEEP, 0.7f);
		FormUtil.dash(p, 0.8, 0.1);
		DelayedTasks.schedule(1, () -> {
			if (!FormUtil.ok(p)) return;
			ServerLevel lvl = FormUtil.level(p);
			Vec3 eye = p.getEyePosition();
			Vec3 dir = p.getLookAngle().normalize();
			FormUtil.trail(lvl, ParticleTypes.CLOUD, eye.add(dir), eye.add(dir.scale(7)), 0.5, 5, 0.5);
			FormUtil.trail(lvl, ParticleTypes.SWEEP_ATTACK, eye.add(dir.scale(2)), eye.add(dir.scale(7)), 1.5, 1, 0.1);
			for (LivingEntity e : FormUtil.line(p, 7, 1.5)) FormUtil.hit(p, e, 6f, 1.0);
		});
	}

	/** 2ª forma: três garras de vento; o último golpe causa sangramento. */
	private static void clawsPurifyingWind(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f);
		for (int i = 0; i < 3; i++) {
			final boolean last = i == 2;
			DelayedTasks.schedule(i * 3 + 1, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				FormUtil.arc(lvl, ParticleTypes.SWEEP_ATTACK, p, 3.0, 100, 1.0, 5);
				FormUtil.arc(lvl, ParticleTypes.CRIT, p, 3.2, 100, 1.2, 14);
				for (LivingEntity e : FormUtil.cone(p, 4.5, 100)) {
					FormUtil.hit(p, e, 4f, 0.3);
					if (last) e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0)); // "sangramento"
				}
			});
		}
	}

	/** 4ª forma: tempestade ao redor que lança inimigos para cima. */
	private static void risingDustStorm(ServerPlayer p, ServerLevel l) {
		FormUtil.sound(l, p, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 0.6f);
		for (int i = 0; i < 4; i++) {
			DelayedTasks.schedule(i * 3 + 1, () -> {
				if (!FormUtil.ok(p)) return;
				ServerLevel lvl = FormUtil.level(p);
				Vec3 base = p.position();
				for (int h = 0; h < 4; h++) {
					FormUtil.ring(lvl, ParticleTypes.CLOUD, base.add(0, 0.3 + h * 0.9, 0), 1.5 + h * 0.9, 20);
				}
				FormUtil.burst(lvl, ParticleTypes.POOF, base.add(0, 1, 0), 20, 2.0, 0.1);
				for (LivingEntity e : FormUtil.radius(p, 5.0)) {
					FormUtil.hit(p, e, 3f, 0.0);
					FormUtil.launch(e, 0.6);
				}
			});
		}
	}
}
