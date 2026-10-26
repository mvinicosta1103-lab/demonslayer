package com.demonslayer.breathing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Estado de respiração de um jogador.
 * Só breath/style/form são salvos no disco; o resto é estado temporário.
 */
public class BreathData {
	public static final float MAX_BREATH = 100f;

	public static final Codec<BreathData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.FLOAT.optionalFieldOf("breath", MAX_BREATH).forGetter(d -> d.breath),
			Codec.INT.optionalFieldOf("style", 0).forGetter(d -> d.style),
			Codec.INT.optionalFieldOf("form", 0).forGetter(d -> d.form)
	).apply(i, BreathData::new));

	// persistente
	public float breath = MAX_BREATH;
	public int style = 0; // por enquanto começa em Water; o sistema de classes vai decidir isso depois
	public int form = 0;

	// temporário
	public boolean concentrating = false;
	public int cooldown = 0;     // ticks até poder usar outra forma
	public int exhausted = 0;    // ticks restantes de exaustão
	public int interrupted = 0;  // ticks sem poder se concentrar (levou dano)

	public BreathData() {}

	public BreathData(float breath, int style, int form) {
		this.breath = breath;
		this.style = style;
		this.form = form;
	}

	public BreathingStyle style() {
		return BreathingStyle.byIndex(style);
	}

	public BreathingForm currentForm() {
		List<BreathingForm> forms = style().forms();
		return forms.get(Mth.clamp(form, 0, forms.size() - 1));
	}
}
