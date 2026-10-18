package com.verden.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Condolyus: pequena criatura de fogo, companheira de Noite e Milly.
 * Alimente com maçã para domesticar; ela segue você e cospe metal derretido nos inimigos.
 */
public class CondolyusEntity extends TameableEntity {
    public CondolyusEntity(EntityType<? extends CondolyusEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 14)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new FollowOwnerSimpleGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!this.isTamed() && stack.isOf(Items.APPLE)) {
            if (!this.getWorld().isClient) {
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.getWorld().sendEntityStatus(this, (byte) 7);
                } else {
                    this.getWorld().sendEntityStatus(this, (byte) 6);
                }
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld() instanceof ServerWorld sw && this.isAlive() && this.age % 30 == 0) {
            LivingEntity t = this.getTarget();
            if (t != null && t.isAlive() && this.squaredDistanceTo(t) < 64 && this.canSee(t)) {
                t.setOnFireFor(4);
                t.damage(this.getDamageSources().onFire(), 3f);
                sw.spawnParticles(ParticleTypes.LAVA, t.getX(), t.getBodyY(0.5), t.getZ(), 8, 0.2, 0.3, 0.2, 0.05);
                sw.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getEyeY(), this.getZ(), 5, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    /** Segue o dono sem depender da API de seguir da versão. */
    private static class FollowOwnerSimpleGoal extends Goal {
        private final CondolyusEntity pet;

        FollowOwnerSimpleGoal(CondolyusEntity pet) {
            this.pet = pet;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity owner = pet.getOwner();
            return pet.isTamed() && owner != null && pet.squaredDistanceTo(owner) > 36;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity owner = pet.getOwner();
            return pet.isTamed() && owner != null && pet.squaredDistanceTo(owner) > 9;
        }

        @Override
        public void tick() {
            LivingEntity owner = pet.getOwner();
            if (owner == null) {
                return;
            }
            if (pet.squaredDistanceTo(owner) > 400) {
                pet.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), pet.getYaw(), 0f);
                pet.getNavigation().stop();
            } else {
                pet.getNavigation().startMovingTo(owner, 1.2);
            }
        }
    }
}
