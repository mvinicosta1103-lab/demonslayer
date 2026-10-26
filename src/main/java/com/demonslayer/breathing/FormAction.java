package com.demonslayer.breathing;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** O que uma forma faz quando é executada (sempre no servidor). */
@FunctionalInterface
public interface FormAction {
	void execute(ServerPlayer player, ServerLevel level);
}
