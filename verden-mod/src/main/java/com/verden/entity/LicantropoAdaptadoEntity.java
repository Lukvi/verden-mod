package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

/** Licantropo Adaptado: a nova geração, mais forte e imune à fraqueza da água. */
public class LicantropoAdaptadoEntity extends LicantropoEntity {
    public LicantropoAdaptadoEntity(EntityType<? extends LicantropoAdaptadoEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 35;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 55)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 36)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected boolean takesWaterDamage() {
        return false;
    }
}
