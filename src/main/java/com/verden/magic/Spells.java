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
        return p.getInventory().contains(s -> s.isOf(ModItems.TRIQUETRA) || s.isOf(ModItems.GRIMORIO_MORKE));
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
            case HIDRO -> hidro(w, pl);
            case GEO -> geo(w, pl);
            case CURA -> cura(w, pl);
            case LUZ -> luz(w, pl);
            case TELEPORTE -> teleporte(w, pl);
            case DEFESA -> defesa(w, pl);
            case INVISIBILIDADE -> invisibilidade(w, pl);
            case AREIA -> areia(w, pl);
            case AURA -> aura(w, pl);
            case PRAGAS -> pragas(w, pl);
            case THUUM -> thuum(w, pl);
            case HIPNOSE -> hipnose(w, pl);
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
            t.damage(w.getDamageSources().indirectMagic(pl, pl),
                    pl.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isOf(ModItems.CARTOLA_KITSUJI) ? 19f : 14f);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 2));
            w.spawnParticles(ParticleTypes.SQUID_INK, t.getX(), t.getBodyY(0.5), t.getZ(), 30, 0.4, 0.4, 0.4, 0.05);
        }
    }

    // ------------------------------------------------------------------ Grimório de Ednar Peer

    /** Hidrocinese (dom de Veroogh): onda de água que empurra, apaga fogo e refresca. */
    private static void hidro(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED, SoundCategory.PLAYERS, 1f, 0.9f);
        w.spawnParticles(ParticleTypes.SPLASH, pl.getX(), pl.getY() + 1, pl.getZ(), 120, 2.5, 0.6, 2.5, 0.2);
        w.spawnParticles(ParticleTypes.BUBBLE_POP, pl.getX(), pl.getY() + 1, pl.getZ(), 40, 2, 0.5, 2, 0.1);
        pl.extinguish();
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 400, 0));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 400, 0));
        for (LivingEntity e : SpellUtil.around(w, pl, 7)) {
            e.extinguish();
            Vec3d push = e.getPos().subtract(pl.getPos()).normalize().multiply(1.3).add(0, 0.35, 0);
            e.setVelocity(push);
            e.velocityModified = true;
        }
    }

    /** Geocinese: ergue uma muralha de pedra à frente. */
    private static void geo(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.PLAYERS, 1.5f, 0.5f);
        net.minecraft.util.math.Direction f = pl.getHorizontalFacing();
        net.minecraft.util.math.BlockPos base = pl.getBlockPos().offset(f, 3);
        for (int s = -2; s <= 2; s++) {
            for (int h = 0; h < 4; h++) {
                net.minecraft.util.math.BlockPos p = base.offset(f.rotateYClockwise(), s).up(h);
                if (w.getBlockState(p).isReplaceable()) {
                    w.setBlockState(p, net.minecraft.block.Blocks.STONE.getDefaultState());
                    w.spawnParticles(ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }
    }

    /** Dom da Cura: restaura o conjurador e os aliados próximos, e limpa venenos. */
    private static void cura(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1f, 1.4f);
        pl.heal(10f);
        pl.removeStatusEffect(StatusEffects.POISON);
        pl.removeStatusEffect(StatusEffects.WITHER);
        pl.removeStatusEffect(StatusEffects.NAUSEA);
        pl.removeStatusEffect(StatusEffects.WEAKNESS);
        pl.removeStatusEffect(StatusEffects.SLOWNESS);
        w.spawnParticles(ParticleTypes.HEART, pl.getX(), pl.getY() + 1.2, pl.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
        for (LivingEntity e : SpellUtil.around(w, pl, 6)) {
            if (e instanceof PlayerEntity || e instanceof com.verden.entity.VerdenNpcEntity || e instanceof com.verden.entity.VerdenWalkerEntity) {
                e.heal(6f);
                w.spawnParticles(ParticleTypes.HEART, e.getX(), e.getBodyY(1.0), e.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    /** Magia da Luz (Lys): cega e revela os inimigos, e cura um pouco o conjurador. */
    private static void luz(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 2f, 1.5f);
        w.spawnParticles(ParticleTypes.END_ROD, pl.getX(), pl.getY() + 1, pl.getZ(), 80, 3, 1, 3, 0.05);
        pl.heal(4f);
        for (LivingEntity e : SpellUtil.around(w, pl, 9)) {
            if (e instanceof com.verden.entity.RazorKitsujiEntity razor) {
                razor.lightHit(25f);
            } else if (e instanceof net.minecraft.entity.mob.HostileEntity) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            }
        }
    }

    /** Teletransporte: rasga a realidade e reaparece até 12 blocos à frente. */
    private static void teleporte(ServerWorld w, PlayerEntity pl) {
        Vec3d eye = pl.getEyePos();
        Vec3d dir = pl.getRotationVec(1.0f).normalize();
        for (double d = 12; d >= 2; d -= 1) {
            Vec3d p = eye.add(dir.multiply(d));
            net.minecraft.util.math.BlockPos feet = net.minecraft.util.math.BlockPos.ofFloored(p.x, p.y - 1.62, p.z);
            if (w.getBlockState(feet).getCollisionShape(w, feet).isEmpty()
                    && w.getBlockState(feet.up()).getCollisionShape(w, feet.up()).isEmpty()
                    && !w.getBlockState(feet.down()).getCollisionShape(w, feet.down()).isEmpty()
                    || (w.getBlockState(feet).getCollisionShape(w, feet).isEmpty()
                    && w.getBlockState(feet.up()).getCollisionShape(w, feet.up()).isEmpty() && d <= 4)) {
                w.spawnParticles(ParticleTypes.PORTAL, pl.getX(), pl.getY() + 1, pl.getZ(), 60, 0.4, 0.8, 0.4, 0.4);
                pl.requestTeleport(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
                pl.fallDistance = 0;
                w.spawnParticles(ParticleTypes.PORTAL, feet.getX() + 0.5, feet.getY() + 1, feet.getZ() + 0.5, 60, 0.4, 0.8, 0.4, 0.4);
                w.playSound(null, feet, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
                return;
            }
        }
        pl.sendMessage(Text.translatable("message.verden.no_space"), true);
    }

    /** Defesa: barreira arcana (absorção + resistência). */
    private static void defesa(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 1.5f);
        w.spawnParticles(ParticleTypes.ENCHANTED_HIT, pl.getX(), pl.getY() + 1, pl.getZ(), 60, 0.8, 1, 0.8, 0.3);
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 2));
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 1));
    }

    /** Invisibilidade: o mundo esquece o conjurador. */
    private static void invisibilidade(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1f, 1f);
        w.spawnParticles(ParticleTypes.POOF, pl.getX(), pl.getY() + 1, pl.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        pl.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 600, 0));
    }

    /** Controlador da Areia: tempestade de areia que cega e fere quem está perto. */
    private static void areia(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.BLOCK_SAND_BREAK, SoundCategory.PLAYERS, 1.5f, 0.6f);
        w.spawnParticles(new net.minecraft.particle.BlockStateParticleEffect(ParticleTypes.BLOCK, net.minecraft.block.Blocks.SAND.getDefaultState()),
                pl.getX(), pl.getY() + 1, pl.getZ(), 150, 3, 1, 3, 0.3);
        for (LivingEntity e : SpellUtil.around(w, pl, 6)) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
            e.damage(w.getDamageSources().indirectMagic(pl, pl), 3f);
        }
    }

    /** Aura Explosiva: o alvo vira uma bomba viva (e o conjurador sofre um pouco). */
    private static void aura(ServerWorld w, PlayerEntity pl) {
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 20, ParticleTypes.ELECTRIC_SPARK);
        if (t == null) {
            return;
        }
        pl.damage(w.getDamageSources().magic(), 2f);
        w.createExplosion(pl, t.getX(), t.getBodyY(0.5), t.getZ(), 2.5f, net.minecraft.world.World.ExplosionSourceType.NONE);
        t.damage(w.getDamageSources().indirectMagic(pl, pl), 8f);
    }

    /** Senhor das Pragas: peste em volta. */
    private static void pragas(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.PLAYERS, 1f, 0.6f);
        w.spawnParticles(ParticleTypes.WITCH, pl.getX(), pl.getY() + 1, pl.getZ(), 120, 4, 1, 4, 0.05);
        for (LivingEntity e : SpellUtil.around(w, pl, 8)) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 160, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
        }
    }

    /** Thu'um, o Fôlego dos Dragões: cone de fogo à frente. */
    private static void thuum(ServerWorld w, PlayerEntity pl) {
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.2f, 1.2f);
        Vec3d eye = pl.getEyePos();
        Vec3d dir = pl.getRotationVec(1.0f).normalize();
        for (double d = 1; d <= 10; d += 0.5) {
            Vec3d p = eye.add(dir.multiply(d));
            w.spawnParticles(ParticleTypes.DRAGON_BREATH, p.x, p.y, p.z, 4, d * 0.06, d * 0.06, d * 0.06, 0.01);
            w.spawnParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, d * 0.05, d * 0.05, d * 0.05, 0.02);
        }
        for (LivingEntity e : SpellUtil.around(w, pl, 10)) {
            Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(eye).normalize();
            if (to.dotProduct(dir) > 0.6) {
                e.setOnFireFor(6);
                e.damage(w.getDamageSources().onFire(), 8f);
            }
        }
    }

    /** Hipnose: o alvo perde a vontade de lutar. */
    private static void hipnose(ServerWorld w, PlayerEntity pl) {
        LivingEntity t = SpellUtil.raycastLiving(w, pl, 16, ParticleTypes.ENCHANT);
        if (t == null) {
            return;
        }
        w.playSound(null, pl.getBlockPos(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1f, 1.3f);
        if (t instanceof net.minecraft.entity.mob.MobEntity m) {
            m.setTarget(null);
            m.getNavigation().stop();
        }
        t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 4));
        t.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
        t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 2));
        w.spawnParticles(ParticleTypes.ENCHANT, t.getX(), t.getBodyY(1.0), t.getZ(), 40, 0.4, 0.5, 0.4, 0.5);
    }
}
