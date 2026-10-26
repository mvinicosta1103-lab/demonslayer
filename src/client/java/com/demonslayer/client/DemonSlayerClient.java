package com.demonslayer.client;

import com.demonslayer.network.BreathSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class DemonSlayerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BreathingKeys.register();
		HudRenderCallback.EVENT.register(BreathHud::render);
		ClientPlayNetworking.registerGlobalReceiver(BreathSyncPayload.TYPE,
				(payload, context) -> ClientBreathState.apply(payload));
	}
}
