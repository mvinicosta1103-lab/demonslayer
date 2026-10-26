package com.demonslayer;

import com.demonslayer.attachment.ModAttachments;
import com.demonslayer.breathing.BreathManager;
import com.demonslayer.breathing.BreathingCommands;
import com.demonslayer.item.ModItemGroups;
import com.demonslayer.item.ModItems;
import com.demonslayer.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemonSlayerMod implements ModInitializer {
	public static final String MOD_ID = "demonslayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// A ordem importa: itens primeiro, aba criativa depois (ela referencia os itens).
		ModItems.register();
		ModItemGroups.register();

		// Sistema de respiração
		ModAttachments.register();
		ModNetworking.register();
		BreathManager.register();
		BreathingCommands.register();

		LOGGER.info("Demon Slayer carregado. Respiração da Água, Forma 1!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
