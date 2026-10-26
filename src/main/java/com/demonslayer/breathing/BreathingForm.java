package com.demonslayer.breathing;

import net.minecraft.network.chat.Component;

/**
 * Uma forma de respiração.
 *
 * @param number   número da forma na série (ex.: Water 1, 2, 6)
 * @param cost     fôlego gasto (0-100)
 * @param cooldown recarga em ticks (20 ticks = 1s)
 * @param action   efeito executado no servidor
 */
public record BreathingForm(int number, float cost, int cooldown, FormAction action) {
	public Component name(BreathingStyle style) {
		return Component.translatable("form.demonslayer." + style.id() + "." + number);
	}
}
