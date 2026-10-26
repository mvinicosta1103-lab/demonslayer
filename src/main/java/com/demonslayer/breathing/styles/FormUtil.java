package com.demonslayer.breathing.styles;

import com.demonslayer.breathing.DelayedTasks;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Ferramentas compartilhadas pelas formas: movimento, alvos, dano, partículas e sons. */
public final class FormUtil {
	public static final ParticleOptions STONE_PARTICLE =
			new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());

	private FormUtil() {}

	// ---------- jogador ----------

	public static boolean ok(ServerPlayer p) {
		return p.isAlive() && !p.isRemoved();
	}

	public static ServerLevel level(ServerPlayer p) {
		return (ServerLevel) p.level();
	}

	/** Direção horizontal para onde o jogador olha (sempre normalizada). */
	public static Vec3 flatLook(ServerPlayer p) {
		return Vec3.directionFromRotation(0, p.getYRot());
	}

	/** Impulso horizontal na direção do olhar + velocidade vertical (lift). */
	public static void dash(ServerPlayer p, double speed, double lift) {
		Vec3 f = flatLook(p);
		p.setDeltaMovement(f.x * speed, lift, f.z * speed);
		p.hurtMarked = true;
		p.fallDistance = 0;
	}

	// ---------- alvos ----------

	public static boolean valid(ServerPlayer p, LivingEntity e) {
		return e != p && e.isAlive() && !e.isSpectator() && !(e instanceof ArmorStand)
				&& !p.isAlliedTo(e)
				&& !(e instanceof TamableAnimal pet && pet.isOwnedBy(p));
	}

	public static List<LivingEntity> radius(ServerPlayer p, double r) {
		return level(p).getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r),
				e -> valid(p, e) && e.distanceToSqr(p) <= r * r);
	}

	public static List<LivingEntity> cone(ServerPlayer p, double range, double angleDeg) {
		Vec3 eye = p.getEyePosition();
		Vec3 look = p.getLookAngle();
		double cos = Math.cos(Math.toRadians(angleDeg / 2.0));
		return level(p).getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range), e -> {
			if (!valid(p, e)) return false;
			Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
			double d = to.length();
			return d <= range + e.getBbWidth() * 0.5 && (d < 0.5 || look.dot(to.scale(1.0 / d)) >= cos);
		});
	}

	/** Entidades perto do segmento start->end (largura = raio do "tubo"). */
	public static List<LivingEntity> segment(ServerPlayer p, Vec3 start, Vec3 end, double width) {
		Vec3 diff = end.subtract(start);
		double length = diff.length();
		if (length < 1.0E-6) return new ArrayList<>();
		Vec3 dir = diff.scale(1.0 / length);
		AABB box = new AABB(start, end).inflate(width + 1.0);
		return level(p).getEntitiesOfClass(LivingEntity.class, box, e -> {
			if (!valid(p, e)) return false;
			Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
			double t = Mth.clamp(c.subtract(start).dot(dir), 0.0, length);
			return start.add(dir.scale(t)).distanceTo(c) <= width + e.getBbWidth() * 0.5;
		});
	}

	/** Linha na direção do olhar, a partir dos olhos. */
	public static List<LivingEntity> line(ServerPlayer p, double length, double width) {
		Vec3 start = p.getEyePosition();
		return segment(p, start, start.add(p.getLookAngle().normalize().scale(length)), width);
	}

	// ---------- dano e efeitos ----------

	/** Dano de jogador. Zera o tempo de invulnerabilidade para que golpes seguidos acertem. */
	public static void hit(ServerPlayer p, LivingEntity target, float damage, double knockback) {
		target.invulnerableTime = 0;
		if (target.hurt(level(p).damageSources().playerAttack(p), damage) && knockback > 0) {
			target.knockback(knockback, p.getX() - target.getX(), p.getZ() - target.getZ());
		}
	}

	public static void launch(LivingEntity e, double upward) {
		Vec3 m = e.getDeltaMovement();
		e.setDeltaMovement(m.x, upward, m.z);
		e.hurtMarked = true;
	}

	public static void pull(LivingEntity e, Vec3 center, double strength) {
		Vec3 d = center.subtract(e.position());
		if (d.lengthSqr() < 0.25) return;
		Vec3 m = e.getDeltaMovement().add(d.normalize().scale(strength));
		e.setDeltaMovement(m.x, Math.max(m.y, 0.1), m.z);
		e.hurtMarked = true;
	}

	// ---------- partículas e sons ----------

	public static void burst(ServerLevel l, ParticleOptions type, Vec3 pos, int count, double spread, double speed) {
		l.sendParticles(type, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
	}

	public static void trail(ServerLevel l, ParticleOptions type, Vec3 from, Vec3 to, double step, int perPoint, double spread) {
		Vec3 diff = to.subtract(from);
		double len = diff.length();
		if (len < 1.0E-6) return;
		Vec3 dir = diff.scale(1.0 / len);
		for (double d = 0; d <= len; d += step) {
			Vec3 pt = from.add(dir.scale(d));
			l.sendParticles(type, pt.x, pt.y, pt.z, perPoint, spread, spread, spread, 0.01);
		}
	}

	public static void ring(ServerLevel l, ParticleOptions type, Vec3 center, double radius, int points) {
		for (int i = 0; i < points; i++) {
			double a = Math.PI * 2.0 * i / points;
			l.sendParticles(type, center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius,
					1, 0, 0.05, 0, 0.02);
		}
	}

	/** Arco na frente do jogador (visual de golpe de espada). */
	public static void arc(ServerLevel l, ParticleOptions type, ServerPlayer p, double radius, double arcDeg, double yOffset, int points) {
		Vec3 f = flatLook(p);
		double base = Math.atan2(f.z, f.x);
		for (int i = 0; i < points; i++) {
			double a = base + Math.toRadians(-arcDeg / 2.0 + arcDeg * i / Math.max(1, points - 1));
			l.sendParticles(type, p.getX() + Math.cos(a) * radius, p.getY() + yOffset, p.getZ() + Math.sin(a) * radius,
					1, 0, 0, 0, 0);
		}
	}

	public static void sound(ServerLevel l, ServerPlayer p, SoundEvent sound, float pitch) {
		l.playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0F, pitch);
	}

	/** Alguns sons da 1.21.1 vêm embrulhados em Holder (ex.: TRIDENT_RIPTIDE_1). */
	public static void sound(ServerLevel l, ServerPlayer p, Holder<SoundEvent> sound, float pitch) {
		sound(l, p, sound.value(), pitch);
	}

	public static void visualLightning(ServerLevel l, Vec3 pos) {
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(l);
		if (bolt != null) {
			bolt.moveTo(pos);
			bolt.setVisualOnly(true);
			l.addFreshEntity(bolt);
		}
	}

	// ---------- pouso ----------

	/**
	 * Espera o jogador pousar. Só checa o chão depois de minTicks (para dar tempo de sair do chão)
	 * e chama o callback de qualquer jeito após maxTicks. Zera dano de queda no caminho.
	 */
	public static void onLand(ServerPlayer p, int minTicks, int maxTicks, Consumer<ServerPlayer> callback) {
		watch(p, 0, minTicks, maxTicks, callback);
	}

	private static void watch(ServerPlayer p, int elapsed, int min, int max, Consumer<ServerPlayer> callback) {
		DelayedTasks.schedule(1, () -> {
			if (!ok(p)) return;
			p.fallDistance = 0;
			int e = elapsed + 1;
			if ((e >= min && p.onGround()) || e >= max) {
				callback.accept(p);
			} else {
				watch(p, e, min, max, callback);
			}
		});
	}
}
