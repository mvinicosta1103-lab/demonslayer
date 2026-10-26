package com.demonslayer.breathing;

import com.demonslayer.DemonSlayerMod;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Efeitos visuais da respiração, feitos com o AAA Particles (Effekseer).
 *
 * O arquivo do efeito fica em:  assets/demonslayer/effeks/breath_wave.efkefc
 * (somente o formato .efkefc é aceito, exporte do editor do Effekseer).
 */
public final class BreathEffects {
	/** ID do efeito = namespace:caminho dentro de assets/<namespace>/effeks/ (sem a extensão). */
	public static final ResourceLocation BREATH_WAVE = DemonSlayerMod.id("breath_wave");

	/**
	 * Intervalo entre uma onda e outra enquanto o jogador segura R (20 ticks = 1s = 60 frames do Effekseer).
	 * IMPORTANTE: deve ser igual à duração da geração no .efkefc (Spawn Count x Spawn Rate, em frames, dividido por 3).
	 * Com 30 partículas a 1 por frame = 30 frames = 10 ticks, uma onda começa exatamente quando a anterior para de gerar,
	 * e o vento fica contínuo, sem buracos nem picos.
	 */
	public static final int WAVE_INTERVAL_TICKS = 10;

	// ----- Ajustes finos (mexa aqui se o efeito sair torto ou fora da boca) -----
	/** Distância da boca à frente do rosto, em blocos. */
	private static final double MOUTH_FORWARD = 0.25;
	/** Altura da boca em relação aos olhos (negativo = abaixo). */
	private static final double MOUTH_DOWN = -0.22;
	/** Tamanho do efeito. O efeito foi feito 10x maior no editor do Effekseer, por isso 0.06 aqui. */
	private static final float SCALE = 0.06f;
	/** Rotação extra em Y, em radianos. Se o vento sair pra trás, troque por (float) Math.PI. */
	private static final float YAW_OFFSET = 0f;
	/** Distância de envio. Como o efeito está preso à entidade (sem posição fixa), o AAA envia a todos do mesmo mundo. */
	private static final double VIEW_DISTANCE = 48.0;

	private BreathEffects() {}

	/** Solta uma pequena onda de vento saindo da boca do jogador. Chamar no servidor. */
	public static void spawnBreathWave(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();

		ParticleEmitterInfo info = ParticleEmitterInfo.create(level, BREATH_WAVE)
				.bindOnEntity(player)          // acompanha o jogador
				.useEntityHeadSpace()          // e a direção da cabeça (olhar pra cima/baixo/lados)
				// No espaço da cabeça, "frente" é o -Z.
				.entitySpaceRelativePosition(0, MOUTH_DOWN, -MOUTH_FORWARD)
				.rotation(0f, YAW_OFFSET, 0f)
				.scale(SCALE);

		AAALevel.addParticle(level, VIEW_DISTANCE, info);
	}
}
