package com.demonslayer.client;

import com.demonslayer.network.BreathingActionPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class BreathingKeys {
	private static final String CATEGORY = "key.categories.demonslayer";

	public static final KeyMapping CONCENTRATE = key("concentrate", GLFW.GLFW_KEY_R);
	public static final KeyMapping USE_FORM = key("use_form", GLFW.GLFW_KEY_G);
	public static final KeyMapping NEXT_FORM = key("next_form", GLFW.GLFW_KEY_V);
	public static final KeyMapping PREV_FORM = key("prev_form", GLFW.GLFW_KEY_Z);
	public static final KeyMapping NEXT_STYLE = key("next_style", GLFW.GLFW_KEY_B);

	private static boolean wasConcentrating = false;

	private BreathingKeys() {}

	private static KeyMapping key(String name, int glfwKey) {
		return KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.demonslayer." + name, InputConstants.Type.KEYSYM, glfwKey, CATEGORY));
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(BreathingKeys::tick);
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null) {
			wasConcentrating = false;
			return;
		}

		boolean typing = mc.screen != null;

		// Segurar R = respirar fundo. Só manda pacote quando o estado muda.
		boolean down = CONCENTRATE.isDown() && !typing;
		if (down != wasConcentrating) {
			wasConcentrating = down;
			send(down ? BreathingActionPayload.START_CONCENTRATE : BreathingActionPayload.STOP_CONCENTRATE);
		}

		while (USE_FORM.consumeClick()) { if (!typing) send(BreathingActionPayload.USE_FORM); }
		while (NEXT_FORM.consumeClick()) { if (!typing) send(BreathingActionPayload.NEXT_FORM); }
		while (PREV_FORM.consumeClick()) { if (!typing) send(BreathingActionPayload.PREV_FORM); }
		while (NEXT_STYLE.consumeClick()) { if (!typing) send(BreathingActionPayload.NEXT_STYLE); }
	}

	private static void send(int action) {
		ClientPlayNetworking.send(new BreathingActionPayload(action));
	}
}
