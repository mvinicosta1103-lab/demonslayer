package com.demonslayer.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Material da lâmina Nichirin (aço do sol). */
public enum ModToolTiers implements Tier {
	NICHIRIN(BlockTags.INCORRECT_FOR_IRON_TOOL, 1800, 7.0F, 3.0F, 18, Ingredient.EMPTY);

	private final TagKey<Block> incorrectBlocksForDrops;
	private final int uses;
	private final float speed;
	private final float attackDamageBonus;
	private final int enchantmentValue;
	private final Ingredient repairIngredient;

	ModToolTiers(TagKey<Block> incorrectBlocksForDrops, int uses, float speed,
				 float attackDamageBonus, int enchantmentValue, Ingredient repairIngredient) {
		this.incorrectBlocksForDrops = incorrectBlocksForDrops;
		this.uses = uses;
		this.speed = speed;
		this.attackDamageBonus = attackDamageBonus;
		this.enchantmentValue = enchantmentValue;
		this.repairIngredient = repairIngredient;
	}

	@Override public int getUses() { return uses; }
	@Override public float getSpeed() { return speed; }
	@Override public float getAttackDamageBonus() { return attackDamageBonus; }
	@Override public TagKey<Block> getIncorrectBlocksForDrops() { return incorrectBlocksForDrops; }
	@Override public int getEnchantmentValue() { return enchantmentValue; }
	@Override public Ingredient getRepairIngredient() { return repairIngredient; }
}
