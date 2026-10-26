package com.demonslayer.breathing;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/** /breathing style <water|flame|thunder|wind|stone>  e  /breathing fill  (nível de op 2). */
public final class BreathingCommands {
	private BreathingCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("breathing")
						.requires(src -> src.hasPermission(2))
						.then(Commands.literal("style")
								.then(Commands.argument("name", StringArgumentType.word())
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
												Arrays.stream(BreathingStyle.values()).map(BreathingStyle::id), builder))
										.executes(ctx -> setStyle(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
						.then(Commands.literal("fill")
								.executes(ctx -> fill(ctx.getSource())))));
	}

	private static int setStyle(CommandSourceStack source, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		BreathingStyle style = BreathingStyle.byId(name);
		if (style == null) {
			source.sendFailure(Component.literal("Estilo desconhecido: " + name));
			return 0;
		}
		BreathData d = BreathManager.get(player);
		d.style = style.ordinal();
		d.form = 0;
		BreathManager.sync(player, d);
		source.sendSuccess(() -> Component.translatable("style.demonslayer." + style.id()), false);
		return 1;
	}

	private static int fill(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		BreathManager.restore(source.getPlayerOrException());
		return 1;
	}
}
