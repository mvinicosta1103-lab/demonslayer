package com.demonslayer.network;

import com.demonslayer.breathing.BreathManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
	private ModNetworking() {}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(BreathingActionPayload.TYPE, BreathingActionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BreathSyncPayload.TYPE, BreathSyncPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(BreathingActionPayload.TYPE,
				(payload, context) -> BreathManager.handleAction(context.player(), payload.action()));
	}
}
