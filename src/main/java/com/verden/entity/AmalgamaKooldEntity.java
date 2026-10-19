package com.verden.entity;

import com.verden.magic.SpellUtil;
import com.verden.registry.ModEntities;
import com.verden.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Amálgama de Koold: chefe gigante. O núcleo no peito é vulnerável ao fogo
 * (dano triplo); qualquer outro dano é bastante reduzido.
 * Fase 2 (<50% de vida): invoca tentáculos. Fase 3 (<25%): fica furioso.
 */
public class AmalgamaKooldEntity extends VerdenMonster {
    private final ServerBossBar bossBar = new ServerBossBar(
            Text.translatable("entity.verden.amalgama_koold"),
            BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);

    public AmalgamaKooldEntity(EntityType<? extends AmalgamaKooldEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 500;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 400)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18)
                .add(EntityAttributes.GENERIC_ARMOR, 8)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 2.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            amount *= 3f; // o núcleo é fraco ao fogo
        } else if (!source.isOf(DamageTypes.GENERIC_KILL)) {
            amount *= 0.4f;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        float ratio = this.getHealth() / this.getMaxHealth();
        bossBar.setPercent(ratio);
        bossBar.setColor(ratio < 0.25f ? BossBar.Color.RED : ratio < 0.5f ? BossBar.Color.PINK : BossBar.Color.PURPLE);

        if (!(this.getWorld() instanceof ServerWorld sw)) {
            return;
        }

        // Tema do chefe (toca a cada ~34 s enquanto há alvo; o raio de som é grande)
        if (this.getTarget() != null && this.age % 680 == 1) {
            sw.playSound(null, this.getBlockPos(), ModSounds.BOSS_MUSIC, SoundCategory.RECORDS, 5f, 1f);
        }

        // Fase 2: tentáculos
        if (ratio < 0.5f && this.age % 200 == 0) {
            for (int i = 0; i < 2; i++) {
                TentaculoEntity t = ModEntities.TENTACULO.create(sw);
                if (t != null) {
                    t.refreshPositionAndAngles(this.getX() + this.random.nextInt(13) - 6, this.getY(),
                            this.getZ() + this.random.nextInt(13) - 6, this.random.nextFloat() * 360f, 0f);
                    sw.spawnEntity(t);
                }
            }
        }

        // Pisão em área (mais frequente na fase 3)
        int period = ratio < 0.25f ? 80 : 160;
        if (this.getTarget() != null && this.age % period == 0) {
            sw.playSound(null, this.getBlockPos(), ModSounds.ROAR, SoundCategory.HOSTILE, 4f, 1f);
            sw.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2, this.getZ(), 8, 3, 0.2, 3, 0.0);
            for (LivingEntity e : SpellUtil.around(sw, this, 12)) {
                if (e instanceof VerdenMonster || e instanceof LegionUnit) {
                    continue;
                }
                e.damage(this.getDamageSources().mobAttack(this), 9f);
                Vec3d push = e.getPos().subtract(this.getPos()).normalize().multiply(1.2).add(0, 0.6, 0);
                e.setVelocity(push);
                e.velocityModified = true;
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ROAR;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LYCAN_DEATH;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        bossBar.clearPlayers();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
