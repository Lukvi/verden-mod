package com.verden.registry;

import com.verden.VerdenMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

/** Sons próprios do mod (arquivos .ogg gerados por tools/sound_gen.py). */
public final class ModSounds {
    private ModSounds() {}

    public static final SoundEvent GROWL = reg("licantropo.growl");
    public static final SoundEvent LYCAN_HURT = reg("licantropo.hurt");
    public static final SoundEvent LYCAN_DEATH = reg("licantropo.death");
    public static final SoundEvent ROAR = reg("amalgama.roar");
    public static final SoundEvent BOSS_MUSIC = reg("music.amalgama");
    public static final SoundEvent HORN = reg("legiao.horn");
    public static final SoundEvent SPELL = reg("magia.cast");
    public static final SoundEvent COIN = reg("moeda.tilintar");
    public static final SoundEvent NPC_TALK = reg("npc.falar");

    private static SoundEvent reg(String name) {
        return Registry.register(Registries.SOUND_EVENT, VerdenMod.id(name), SoundEvent.of(VerdenMod.id(name)));
    }

    public static void register() {
        // o carregamento da classe já registra os eventos
    }
}
