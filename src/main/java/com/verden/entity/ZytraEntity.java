package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Zytra Twin: legionária veloz que avança em investidas relâmpago. */
public class ZytraEntity extends TwinEntity {
    public ZytraEntity(EntityType<? extends ZytraEntity> type, World world) {
        super(type, world, Text.translatable("entity.verden.zytra_twin"), BossBar.Color.YELLOW);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 120)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9)
                .add(EntityAttributes.GENERIC_ARMOR, 4)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40);
    }

    @Override
    protected void special(ServerWorld world) {
        LivingEntity t = this.getTarget();
        if (t == null || this.age % 100 != 0) {
            return;
        }
        double d2 = this.squaredDistanceTo(t);
        if (d2 > 16 && d2 < 196) {
            Vec3d dash = t.getPos().subtract(this.getPos()).normalize().multiply(1.6).add(0, 0.3, 0);
            this.setVelocity(dash);
            this.velocityModified = true;
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1f, 1.4f);
        }
    }
}
