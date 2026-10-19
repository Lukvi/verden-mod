package com.verden.magic;

import com.verden.registry.ModItems;
import com.verden.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/** Implementação dos poderes de Verden. Todas rodam no servidor. */
public final class Spells {
    private Spells() {}

    public static boolean hasTriquetra(PlayerEntity p) {
        return p.getInventory().contains(s -> s.isOf(ModItems.TRIQUETRA));
    }

    public static void cast(Power power, ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), ModSounds.SPELL, SoundCategory.PLAYERS, 0.8f, 0.8f + power.ordinal() * 0.07f);
        switch (power) {
            case CHAMAS -> chamas(w, pl);
            case VENTO -> vento(w, pl);
            case TELECINESE -> telecinese(w, pl);
            case GELO -> gelo(w, pl);
            case NEVOA -> nevoa(w, pl);
            case CRONO -> crono(w, pl);
            case NEGRA -> negra(w, pl);
            case METAMORFOSE -> metamorfose(w, pl);
        }
    }

    /** Noite: forma felina - mais rápida, ágil, enxerga no escuro e cai sem dano. */
    private static void metamorfose(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_CAT_AMBIENT, SoundCategory.PLAYERS, 1f, 1.2f);
        w.spawnParticles(ParticleTypes.POOF, pl.getX(), pl.getY() + 1, pl.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 2));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 2));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 600, 0));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 600, 0));
    }

    /** Luke / Hiraku: raio de chamas. Usa dano de fogo (o Núcleo de Koold sofre triplo). */
    private static void chamas(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1f, 0.8f);
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 16, ParticleTypes.FLAME);
        if (t != null) {
            t.setOnFireFor(6);
            t.damage(w.getDamageSources().onFire(), 6f);
            w.spawnParticles(ParticleTypes.LAVA, t.getX(), t.getBodyY(0.5), t.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
        }
    }

    /** Liam: rajada de vento que empurra todos ao redor. */
    private static void vento(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
        w.spawnParticles(ParticleTypes.CLOUD, pl.getX(), pl.getY() + 1, pl.getZ(), 60, 2.5, 0.5, 2.5, 0.15);
        for (LivingEntity e : SpellUtil.around(w, pl, 6)) {
            Vec3d push = e.getPos().subtract(pl.getPos()).normalize().multiply(1.8).add(0, 0.5, 0);
            e.setVelocity(push);
            e.velocityModified = true;
        }
    }

    /** Luke: ergue o alvo no ar. */
    private static void telecinese(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1f, 1.2f);
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 20, ParticleTypes.ENCHANT);
        if (t != null) {
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 3));
            t.setVelocity(0, 1.0, 0);
            t.velocityModified = true;
        }
    }

    /** Lucy: raio de gelo que congela e desacelera. */
    private static void gelo(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1f, 0.6f);
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 16, ParticleTypes.SNOWFLAKE);
        if (t != null) {
            t.setFrozenTicks(300);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            t.damage(w.getDamageSources().freeze(), 4f);
        }
    }

    /** Vanessa / Le Fay: Névoa Carmesim - esconde o conjurador e enfraquece quem está perto. */
    private static void nevoa(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1f, 0.5f);
        w.spawnParticles(ParticleTypes.CRIMSON_SPORE, pl.getX(), pl.getY() + 1, pl.getZ(), 120, 3, 1, 3, 0.02);
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 200, 0));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
        for (LivingEntity e : SpellUtil.around(w, pl, 6)) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
        }
    }

    /** Rose: Cronocinese da Matéria - o tempo praticamente para ao redor. */
    private static void crono(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1f, 0.5f);
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, pl.getX(), pl.getY() + 1, pl.getZ(), 100, 3, 1, 3, 0.1);
        for (LivingEntity e : SpellUtil.around(w, pl, 10)) {
            e.setVelocity(Vec3d.ZERO);
            e.velocityModified = true;
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 9));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 4));
        }
    }

    /** Magia Negra (Triquetra): poder enorme, com custo de vida. */
    private static void negra(ServerWorld w, PlayerEntity pl) {
        if (!hasTriquetra(pl)) {
            pl.sendMessage(Text.translatable("message.verden.need_triquetra"), true);
            return;
        }
        if (pl.getHealth() <= 6f) {
            pl.sendMessage(Text.translatable("message.verden.too_weak"), true);
            return;
        }
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1f, 0.7f);
        pl.damage(w.getDamageSources().magic(), 4f);
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 24, ParticleTypes.SOUL_FIRE_FLAME);
        if (t != null) {
            t.damage(w.getDamageSources().indirectMagic(pl, pl), 14f);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 2));
            w.spawnParticles(ParticleTypes.SQUID_INK, t.getX(), t.getBodyY(0.5), t.getZ(), 30, 0.4, 0.4, 0.4, 0.05);
        }
    }
}
