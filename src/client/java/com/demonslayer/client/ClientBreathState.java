package com.demonslayer.client;

import com.demonslayer.breathing.BreathData;
import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.BreathingStyle;
import com.demonslayer.network.BreathSyncPayload;
import net.minecraft.util.Mth;

import java.util.List;

/** Último estado de respiração recebido do servidor (usado só para desenhar o HUD). */
public final class ClientBreathState {
	public static float breath = BreathData.MAX_BREATH;
	public static float displayedBreath = BreathData.MAX_BREATH;
	public static int style = 0;
	public static int form = 0;
	public static int cooldown = 0;
	public static int exhausted = 0;
	public static boolean concentrating = false;

	private ClientBreathState() {}

	public static void apply(BreathSyncPayload p) {
		breath = p.breath();
		style = p.style();
		form = p.form();
		cooldown = p.cooldown();
		exhausted = p.exhausted();
		concentrating = p.concentrating();
	}

	public static BreathingStyle style() {
		return BreathingStyle.byIndex(style);
	}

	public static BreathingForm form() {
		List<BreathingForm> forms = style().forms();
		return forms.get(Mth.clamp(form, 0, forms.size() - 1));
	}
}
