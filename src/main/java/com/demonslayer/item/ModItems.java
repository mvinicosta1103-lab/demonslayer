package com.demonslayer.item;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

public class ModItems {
	public static final Item NICHIRIN_INGOT = register("nichirin_ingot", new Item(new Item.Properties()));

	public static final Item NICHIRIN_KATANA = register("nichirin_katana",
			new SwordItem(ModToolTiers.NICHIRIN,
					new Item.Properties().attributes(
							SwordItem.createAttributes(ModToolTiers.NICHIRIN, 3, -2.2F))));

	private static Item register(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, DemonSlayerMod.id(name), item);
	}

	/** Chamado só para forçar o carregamento da classe (e dos campos estáticos acima). */
	public static void register() {
		DemonSlayerMod.LOGGER.info("Registrando itens de {}", DemonSlayerMod.MOD_ID);
	}
}
