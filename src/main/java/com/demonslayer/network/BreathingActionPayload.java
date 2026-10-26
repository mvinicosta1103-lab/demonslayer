package com.demonslayer.network;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente -> Servidor: o jogador apertou uma tecla de respiração. */
public record BreathingActionPayload(int action) implements CustomPacketPayload {
	public static final int USE_FORM = 0;
	public static final int NEXT_FORM = 1;
	public static final int PREV_FORM = 2;
	public static final int NEXT_STYLE = 3;
	public static final int START_CONCENTRATE = 4;
	public static final int STOP_CONCENTRATE = 5;

	public static final CustomPacketPayload.Type<BreathingActionPayload> TYPE =
			new CustomPacketPayload.Type<>(DemonSlayerMod.id("breathing_action"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BreathingActionPayload> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, BreathingActionPayload::action, BreathingActionPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
