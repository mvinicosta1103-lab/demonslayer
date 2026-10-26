package com.demonslayer.client;

import com.demonslayer.breathing.BreathData;
import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.BreathingStyle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Barra de fôlego no canto inferior esquerdo + estilo/forma selecionados. */
public final class BreathHud {
	private static final int WIDTH = 100;
	private static final int HEIGHT = 7;

	private BreathHud() {}

	public static void render(GuiGraphics g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) return;

		ClientBreathState.displayedBreath = Mth.lerp(0.25f, ClientBreathState.displayedBreath, ClientBreathState.breath);

		BreathingStyle style = ClientBreathState.style();
		BreathingForm form = ClientBreathState.form();

		int x = 10;
		int y = g.guiHeight() - 24;
		int styleColor = 0xFF000000 | style.color();
		boolean exhausted = ClientBreathState.exhausted > 0;
		int barColor = exhausted ? 0xFF666666 : styleColor;

		// título: "Respiração da Água - Forma 1: Corte da Superfície (15)"
		String label = style.displayName().getString() + "  |  " + form.name(style).getString()
				+ " (" + Math.round(form.cost()) + ")";
		g.drawString(mc.font, label, x, y - 11, exhausted ? 0xFFAAAAAA : styleColor, true);

		// moldura (branca quando concentrando)
		int frame = ClientBreathState.concentrating ? 0xFFFFFFFF : 0xAA000000;
		g.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, frame);
		g.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF202020);

		int filled = Math.round(WIDTH * (ClientBreathState.displayedBreath / BreathData.MAX_BREATH));
		g.fill(x, y, x + filled, y + HEIGHT, barColor);

		// marca de custo da forma selecionada (linha branca fina na barra)
		int costX = x + Math.round(WIDTH * (form.cost() / BreathData.MAX_BREATH));
		g.fill(costX, y, costX + 1, y + HEIGHT, 0xCCFFFFFF);

		// recarga da forma
		if (ClientBreathState.cooldown > 0 && form.cooldown() > 0) {
			int cd = Math.round(WIDTH * Math.min(1f, ClientBreathState.cooldown / (float) form.cooldown()));
			g.fill(x, y + HEIGHT + 2, x + cd, y + HEIGHT + 4, 0xFFFFFFFF);
		}
	}
}
