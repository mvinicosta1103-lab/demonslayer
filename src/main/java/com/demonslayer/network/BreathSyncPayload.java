package com.demonslayer.network;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor -> Cliente: estado de respiração para o HUD. */
public record BreathSyncPayload(float breath, int style, int form, int cooldown, int exhausted,
								boolean concentrating) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BreathSyncPayload> TYPE =
			new CustomPacketPayload.Type<>(DemonSlayerMod.id("breath_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BreathSyncPayload> CODEC =
			CustomPacketPayload.codec(BreathSyncPayload::write, BreathSyncPayload::new);

	public BreathSyncPayload(RegistryFriendlyByteBuf buf) {
		this(buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
	}

	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeFloat(breath);
		buf.writeVarInt(style);
		buf.writeVarInt(form);
		buf.writeVarInt(cooldown);
		buf.writeVarInt(exhausted);
		buf.writeBoolean(concentrating);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
