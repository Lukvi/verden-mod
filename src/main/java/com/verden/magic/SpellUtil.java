package com.verden.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Utilidades de mira (hitscan) usadas por magias e pelo revólver. */
public final class SpellUtil {
    private SpellUtil() {}

    /** Primeiro ser vivo atingido por um raio a partir dos olhos do jogador, parando em blocos sólidos. */
    public static LivingEntity raycastLiving(World world, PlayerEntity player, double range, ParticleEffect trail) {
        Vec3d eye = player.getEyePos();
        Vec3d dir = player.getRotationVec(1.0f).normalize();
        for (double d = 1.0; d <= range; d += 0.5) {
            Vec3d p = eye.add(dir.multiply(d));
            BlockPos bp = BlockPos.ofFloored(p);
            if (!world.getBlockState(bp).getCollisionShape(world, bp).isEmpty()) {
                return null;
            }
            if (trail != null && world instanceof ServerWorld sw) {
                sw.spawnParticles(trail, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            Box box = new Box(p.x - 0.4, p.y - 0.4, p.z - 0.4, p.x + 0.4, p.y + 0.4, p.z + 0.4);
            List<LivingEntity> hits = world.getEntitiesByClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive());
            if (!hits.isEmpty()) {
                return hits.get(0);
            }
        }
        return null;
    }

    /** Seres vivos num raio ao redor do jogador (exceto ele mesmo). */
    public static List<LivingEntity> around(World world, Entity center, double radius) {
        Box box = center.getBoundingBox().expand(radius);
        List<LivingEntity> list = new ArrayList<>(world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != center && e.isAlive() && e.squaredDistanceTo(center) <= radius * radius));
        return list;
    }
}
