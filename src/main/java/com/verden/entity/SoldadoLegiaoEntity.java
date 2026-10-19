package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

/** Soldado da Legião das Espadas: infantaria pesada do reino. */
public class SoldadoLegiaoEntity extends VerdenMonster implements LegionUnit {
    public SoldadoLegiaoEntity(EntityType<? extends SoldadoLegiaoEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 12;
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    /** A Legião caça licantropos e Kunori; ignora o jogador a menos que seja atacada. */
    @Override
    protected void addTargets() {
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LicantropoEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, KunoriEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, AmalgamaKooldEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6)
                .add(EntityAttributes.GENERIC_ARMOR, 6)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32);
    }
}
