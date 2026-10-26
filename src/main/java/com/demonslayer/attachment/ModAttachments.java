package com.demonslayer.attachment;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.breathing.BreathData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	/** Dados de respiração salvos junto com o jogador (sobrevivem a relogar e morrer). */
	public static final AttachmentType<BreathData> BREATH = AttachmentRegistry.<BreathData>builder()
			.persistent(BreathData.CODEC)
			.copyOnDeath()
			.initializer(BreathData::new)
			.buildAndRegister(DemonSlayerMod.id("breath"));

	private ModAttachments() {}

	/** Só força o carregamento da classe. */
	public static void register() {}
}
