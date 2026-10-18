package com.verden.magic;

import net.minecraft.text.Text;

/** Poderes do mundo de Verden, usados pelo Grimório. Novos poderes entram sempre no fim (compatibilidade com saves). */
public enum Power {
    CHAMAS("chamas", 40),
    VENTO("vento", 30),
    TELECINESE("telecinese", 35),
    GELO("gelo", 45),
    NEVOA("nevoa", 60),
    CRONO("crono", 80),
    NEGRA("negra", 200),
    METAMORFOSE("metamorfose", 600);

    public final String key;
    public final int cooldown;

    Power(String key, int cooldown) {
        this.key = key;
        this.cooldown = cooldown;
    }

    public Text displayName() {
        return Text.translatable("power.verden." + key);
    }

    public static Power byIndex(int i) {
        Power[] v = values();
        return (i >= 0 && i < v.length) ? v[i] : CHAMAS;
    }

    /** Próximo poder do ciclo. A Magia Negra só entra se o jogador tiver o Amuleto da Triquetra. */
    public Power next(boolean allowDark) {
        Power[] v = values();
        for (int i = 1; i <= v.length; i++) {
            Power p = v[(ordinal() + i) % v.length];
            if (p != NEGRA || allowDark) {
                return p;
            }
        }
        return this;
    }
}
