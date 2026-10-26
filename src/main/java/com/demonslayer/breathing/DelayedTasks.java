package com.demonslayer.breathing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Agenda ações para daqui a N ticks do servidor (usado em formas com vários golpes). */
public final class DelayedTasks {
	private static final class Task {
		int remaining;
		final Runnable action;

		Task(int remaining, Runnable action) {
			this.remaining = remaining;
			this.action = action;
		}
	}

	private static final List<Task> TASKS = new ArrayList<>();

	private DelayedTasks() {}

	/** delay 0 ou 1 = no próximo tick. */
	public static void schedule(int delay, Runnable action) {
		TASKS.add(new Task(Math.max(1, delay), action));
	}

	public static void tick() {
		if (TASKS.isEmpty()) return;
		List<Task> due = new ArrayList<>();
		Iterator<Task> it = TASKS.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (--t.remaining <= 0) {
				due.add(t);
				it.remove();
			}
		}
		// executa depois do loop, assim as tarefas podem agendar novas tarefas
		for (Task t : due) {
			try {
				t.action.run();
			} catch (Exception e) {
				com.demonslayer.DemonSlayerMod.LOGGER.error("Erro em tarefa agendada", e);
			}
		}
	}

	public static void clear() {
		TASKS.clear();
	}
}
