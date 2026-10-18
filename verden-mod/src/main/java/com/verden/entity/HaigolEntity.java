package com.verden.entity;

import com.verden.magic.SpellUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Haigol Twin: legionário de armadura pesada, com pisões que arremessam inimigos. */
public class HaigolEntity extends TwinEntity {
    public HaigolEntity(EntityType<? extends HaigolEntity> type, World world) {
        super(type, world, Text.translatable("entity.verden.haigol_twin"), BossBar.Color.RED);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12)
                .add(EntityAttributes.GENERIC_ARMOR, 14)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 4)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void special(ServerWorld world) {
        LivingEntity t = this.getTarget();
        if (t == null || this.age % 140 != 0 || this.squaredDistanceTo(t) > 49) {
            return;
        }
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 1.5f, 0.6f);
        world.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2, this.getZ(), 6, 2, 0.2, 2, 0.0);
        for (LivingEntity e : SpellUtil.around(world, this, 5)) {
            if (e instanceof VerdenMonster) {
                continue;
            }
            e.damage(this.getDamageSources().mobAttack(this), 8f);
            Vec3d push = e.getPos().subtract(this.getPos()).normalize().multiply(1.3).add(0, 0.6, 0);
            e.setVelocity(push);
            e.velocityModified = true;
        }
    }
}
