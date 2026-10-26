package com.demonslayer.item;

import com.demonslayer.DemonSlayerMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
	public static final CreativeModeTab DEMON_SLAYER_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			DemonSlayerMod.id("demonslayer"),
			FabricItemGroup.builder()
					.title(Component.translatable("itemGroup.demonslayer"))
					.icon(() -> new ItemStack(ModItems.NICHIRIN_KATANA))
					.displayItems((params, output) -> {
						output.accept(ModItems.NICHIRIN_INGOT);
						output.accept(ModItems.NICHIRIN_KATANA);
					})
					.build());

	public static void register() {
		DemonSlayerMod.LOGGER.info("Registrando abas criativas de {}", DemonSlayerMod.MOD_ID);
	}
}
