package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/** Licantropo: forte e regenerativo, mas sofre quando está na água (os novos se adaptam). */
public class LicantropoEntity extends VerdenMonster {
    public LicantropoEntity(EntityType<? extends LicantropoEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient && this.isAlive()) {
            if (this.takesWaterDamage() && this.isTouchingWater()) {
                if (this.age % 10 == 0) {
                    this.damage(this.getDamageSources().magic(), 1.5f);
                }
            } else if (this.age % 30 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(2f);
            }
        }
    }

    /** Os licantropos novos (adaptados) não sofrem mais com a água. */
    protected boolean takesWaterDamage() {
        return true;
    }
}
