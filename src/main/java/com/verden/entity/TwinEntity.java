package com.verden.entity;

import com.verden.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Base dos irmãos Twin da Legião das Espadas. "Juntos, conseguimos": enquanto o irmão
 * está vivo por perto, cada um ganha Força e Resistência.
 */
public abstract class TwinEntity extends VerdenMonster implements LegionUnit {
    private final ServerBossBar bossBar;

    protected TwinEntity(EntityType<? extends HostileEntity> type, World world, Text name, BossBar.Color color) {
        super(type, world);
        this.bossBar = new ServerBossBar(name, color, BossBar.Style.PROGRESS);
        this.experiencePoints = 150;
        this.setPersistent();
    }

    @Override
    protected void addTargets() {
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LicantropoEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, KunoriEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, AmalgamaKooldEntity.class, true));
    }

    /** Habilidade própria de cada irmão, chamada enquanto há um alvo. */
    protected abstract void special(ServerWorld world);

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (!(this.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        if (this.age % 20 == 0) {
            boolean together = !sw.getEntitiesByClass(TwinEntity.class, this.getBoundingBox().expand(40),
                    e -> e != this && e.isAlive()).isEmpty();
            if (together) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0));
            }
        }
        if (this.getTarget() != null && this.age % 600 == 5) {
            sw.playSound(null, this.getBlockPos(), ModSounds.HORN, SoundCategory.HOSTILE, 3f, 1f);
        }
        if (this.getTarget() != null) {
            special(sw);
        }
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
