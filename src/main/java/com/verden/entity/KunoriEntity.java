package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Encapuzado Kunori: guerreiro encapuzado que lança chamas no alvo à distância. */
public class KunoriEntity extends VerdenMonster {
    public KunoriEntity(EntityType<? extends KunoriEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 15;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld() instanceof ServerWorld sw && this.isAlive() && this.age % 100 == 0) {
            LivingEntity t = this.getTarget();
            if (t != null && this.canSee(t) && this.squaredDistanceTo(t) < 144) {
                t.setOnFireFor(4);
                t.damage(this.getDamageSources().magic(), 4f);
                sw.spawnParticles(ParticleTypes.FLAME, t.getX(), t.getBodyY(0.5), t.getZ(), 15, 0.3, 0.4, 0.3, 0.05);
            }
        }
    }
}
