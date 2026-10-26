package com.demonslayer.breathing;

import com.demonslayer.breathing.styles.FlameBreathing;
import com.demonslayer.breathing.styles.StoneBreathing;
import com.demonslayer.breathing.styles.ThunderBreathing;
import com.demonslayer.breathing.styles.WaterBreathing;
import com.demonslayer.breathing.styles.WindBreathing;
import net.minecraft.network.chat.Component;

import java.util.List;

public enum BreathingStyle {
	WATER("water", 0x3FA9F5, WaterBreathing.forms()),
	FLAME("flame", 0xFF6A1A, FlameBreathing.forms()),
	THUNDER("thunder", 0xFFE033, ThunderBreathing.forms()),
	WIND("wind", 0x7FE0A0, WindBreathing.forms()),
	STONE("stone", 0xA89B8F, StoneBreathing.forms());

	private final String id;
	private final int color;
	private final List<BreathingForm> forms;

	BreathingStyle(String id, int color, List<BreathingForm> forms) {
		this.id = id;
		this.color = color;
		this.forms = forms;
	}

	public String id() { return id; }
	public int color() { return color; }
	public List<BreathingForm> forms() { return forms; }

	public Component displayName() {
		return Component.translatable("style.demonslayer." + id);
	}

	public static BreathingStyle byIndex(int index) {
		BreathingStyle[] all = values();
		return all[Math.floorMod(index, all.length)];
	}

	public static BreathingStyle byId(String id) {
		for (BreathingStyle s : values()) {
			if (s.id.equalsIgnoreCase(id)) return s;
		}
		return null;
	}
}
