package com.verden.entity;

import com.verden.magic.SpellUtil;
import com.verden.registry.ModEntities;
import com.verden.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * Razor Kitsuji, a Sombra do Nada: entidade do Limbo, o vilão de "Prelúdio e Agouro".
 * Fase 1 (elegante): Durma, Ji + Kan (gravidade), raios negros. Fase 2 (Vestígio): garra de sombras, almas do Limbo,
 * licantropos e o Colapso do Vazio. Passiva: projéteis o atravessam; fraco à luz (Magia da Luz / Lys).
 */
public class RazorKitsujiEntity extends VerdenMonster {
    private static final TrackedData<Integer> PHASE = DataTracker.registerData(RazorKitsujiEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private enum Act { NONE, DURMA, JI, RAIO, GARRA, COLAPSO, TRANSICAO }

    private final ServerBossBar bossBar = new ServerBossBar(Text.translatable("boss.verden.razor.1"),
            BossBar.Color.PURPLE, BossBar.Style.NOTCHED_12);

    private Act act = Act.NONE;
    private int actTicks = 0;
    private int cooldown = 60;
    private int colapsoCd = 400;
    private int limboTicks = 0;
    private int noRegen = 0;
    private Vec3d center = Vec3d.ZERO;

    public RazorKitsujiEntity(EntityType<? extends RazorKitsujiEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 1500;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 600)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12)
                .add(EntityAttributes.GENERIC_ARMOR, 6)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PHASE, 1);
    }

    public int getPhase() {
        return this.dataTracker.get(PHASE);
    }

    // ------------------------------------------------------------------ dano
    /** Chamado pela Magia da Luz: queima a essência e bloqueia a regeneração. */
    public void lightHit(float dmg) {
        this.noRegen = 300;
        if (getPhase() == 1) {
            float room = getHealth() - getMaxHealth() * 0.5f - 1f;
            if (room <= 0f) {
                if (getWorld() instanceof ServerWorld sw0) {
                    startTransition(sw0);
                }
                return;
            }
            dmg = Math.min(dmg, room);
        }
        super.damage(this.getDamageSources().magic(), dmg);
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.END_ROD, getX(), getBodyY(0.5), getZ(), 40, 0.4, 0.8, 0.4, 0.1);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!(getWorld() instanceof ServerWorld sw)) {
            return super.damage(source, amount);
        }
        if (act == Act.TRANSICAO) {
            return false;
        }
        // Intangibilidade espectral: projéteis o atravessam
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            sw.spawnParticles(ParticleTypes.SMOKE, getX(), getBodyY(0.6), getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.5f, 1.6f);
            return false;
        }
        Entity att = source.getAttacker();
        // Coração exposto (fase 2): golpes de frente valem mais; pelas costas, menos
        if (getPhase() == 2 && att != null) {
            Vec3d to = att.getPos().subtract(getPos()).normalize();
            double dot = to.dotProduct(Vec3d.fromPolar(0, getYaw()).normalize());
            amount *= dot > 0.35 ? 1.6f : 0.8f;
        }
        // Escudo de almas do Limbo
        if (limboTicks > 0) {
            amount *= 0.3f;
            if (att instanceof LivingEntity le && !(att instanceof VerdenMonster)) {
                le.damage(getDamageSources().magic(), 4f);
                sw.spawnParticles(ParticleTypes.SOUL, att.getX(), att.getBodyY(0.5), att.getZ(), 6, 0.2, 0.3, 0.2, 0.02);
            }
        } else if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            amount *= 0.75f;
        }
        float half = getMaxHealth() * 0.5f;
        if (getPhase() == 1 && getHealth() - amount <= half) {
            super.damage(source, Math.max(0f, getHealth() - half - 1f));
            startTransition(sw);
            return true;
        }
        return super.damage(source, amount);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean ok = super.tryAttack(target);
        if (ok && target instanceof PlayerEntity p && random.nextInt(4) == 0 && getWorld() instanceof ServerWorld sw) {
            // Execute de drenagem: crava a mão no peito e rouba vida
            p.damage(getDamageSources().magic(), 6f);
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1));
            heal(14f);
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getBodyY(0.7), p.getZ(), 25, 0.3, 0.4, 0.3, 0.05);
            p.sendMessage(Text.translatable("boss.verden.razor.drain").formatted(net.minecraft.util.Formatting.DARK_PURPLE), true);
        }
        return ok;
    }

    // ------------------------------------------------------------------ IA
    private void startTransition(ServerWorld sw) {
        act = Act.TRANSICAO;
        actTicks = 100;
        limboTicks = 0;
        getNavigation().stop();
        setNoGravity(true);
        sw.playSound(null, getBlockPos(), ModSounds.ROAR, SoundCategory.HOSTILE, 5f, 0.5f);
        for (LivingEntity e : SpellUtil.around(sw, this, 40)) {
            if (e instanceof PlayerEntity p) {
                p.sendMessage(Text.translatable("boss.verden.razor.phase2").formatted(net.minecraft.util.Formatting.DARK_RED), false);
            }
        }
    }

    private void setAct(Act a, int ticks) {
        act = a;
        actTicks = ticks;
    }

    private List<PlayerEntity> playersNear(ServerWorld sw, double r) {
        return sw.getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(r), p -> p.isAlive() && !p.isCreative() && !p.isSpectator());
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!(getWorld() instanceof ServerWorld sw)) {
            return;
        }
        float ratio = getHealth() / getMaxHealth();
        bossBar.setPercent(ratio);
        bossBar.setName(Text.translatable("boss.verden.razor." + getPhase()));
        bossBar.setColor(getPhase() == 2 ? BossBar.Color.RED : BossBar.Color.PURPLE);

        if (noRegen > 0) {
            noRegen--;
        } else if (age % 40 == 0 && getHealth() < getMaxHealth() && getTarget() == null) {
            heal(6f);
        }
        if (limboTicks > 0) {
            limboTicks--;
            if (age % 3 == 0) {
                for (int i = 0; i < 6; i++) {
                    double a = age * 0.4 + i * Math.PI / 3;
                    sw.spawnParticles(ParticleTypes.SOUL, getX() + Math.cos(a) * 1.4, getY() + 0.3 + (i % 3) * 0.6, getZ() + Math.sin(a) * 1.4, 1, 0, 0, 0, 0);
                }
            }
        }
        if (getPhase() == 2 && age % 4 == 0) {
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getBodyY(0.65), getZ(), 1, 0.2, 0.2, 0.2, 0.0);
        }
        LivingEntity target = getTarget();
        if (target != null && age % 680 == 1) {
            sw.playSound(null, getBlockPos(), ModSounds.BOSS_MUSIC, SoundCategory.RECORDS, 5f, 0.9f);
        }
        // Aura do Vazio
        if (target != null && age % 20 == 0) {
            for (PlayerEntity p : playersNear(sw, 14)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 0));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 50, 0));
            }
        }
        if (colapsoCd > 0) {
            colapsoCd--;
        }
        if (act != Act.NONE) {
            runAct(sw);
            return;
        }
        if (target == null || !target.isAlive()) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        chooseAct(sw, target);
    }

    private void chooseAct(ServerWorld sw, LivingEntity t) {
        double d = distanceTo(t);
        int phase = getPhase();
        int roll = random.nextInt(100);
        if (phase == 2 && colapsoCd <= 0) {
            beginColapso(sw);
            return;
        }
        if (phase == 2 && d < 6 && roll < 30) {
            setAct(Act.GARRA, 14);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.HOSTILE, 1.2f, 0.6f);
            return;
        }
        if (phase == 2 && roll < 45 && limboTicks <= 0) {
            limboTicks = 140;
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2f, 0.5f);
            cooldown = 40;
            return;
        }
        if (phase == 2 && roll < 60) {
            summonLycans(sw, t);
            cooldown = 80;
            return;
        }
        if (roll < 35) {
            setAct(Act.DURMA, 12);
            vanishBehind(sw, t);
        } else if (roll < 65) {
            setAct(Act.JI, 70);
            center = getPos().add(0, 2.5, 0);
            sw.playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.HOSTILE, 3f, 0.5f);
            for (PlayerEntity p : playersNear(sw, 40)) {
                p.sendMessage(Text.literal("Ji.").formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.ITALIC), true);
            }
        } else {
            setAct(Act.RAIO, 26);
        }
    }

    private void vanishBehind(ServerWorld sw, LivingEntity t) {
        sw.spawnParticles(ParticleTypes.PORTAL, getX(), getBodyY(0.5), getZ(), 40, 0.4, 0.8, 0.4, 0.3);
        Vec3d back = t.getPos().subtract(t.getRotationVec(1.0f).multiply(1, 0, 1).normalize().multiply(1.7));
        refreshPositionAndAngles(back.x, t.getY(), back.z, t.getYaw(), 0f);
        sw.spawnParticles(ParticleTypes.PORTAL, getX(), getBodyY(0.5), getZ(), 40, 0.4, 0.8, 0.4, 0.3);
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1f, 0.6f);
        if (t instanceof PlayerEntity p) {
            p.sendMessage(Text.literal("«Durma.»").formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.ITALIC), true);
        }
        t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 25, 6));
        t.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0));
    }

    private void summonLycans(ServerWorld sw, LivingEntity t) {
        int have = sw.getEntitiesByClass(LicantropoEntity.class, getBoundingBox().expand(30), e -> true).size();
        for (int i = 0; i < 3 && have + i < 6; i++) {
            LicantropoEntity l = ModEntities.LICANTROPO.create(sw);
            if (l != null) {
                double a = random.nextDouble() * Math.PI * 2;
                l.refreshPositionAndAngles(getX() + Math.cos(a) * 4, getY(), getZ() + Math.sin(a) * 4, random.nextFloat() * 360f, 0f);
                sw.spawnEntity(l);
                l.setTarget(t);
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, l.getX(), l.getY() + 1, l.getZ(), 20, 0.3, 0.6, 0.3, 0.05);
            }
        }
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 2f, 0.6f);
    }

    private void beginColapso(ServerWorld sw) {
        setAct(Act.COLAPSO, 140);
        center = getPos();
        setNoGravity(true);
        refreshPositionAndAngles(getX(), getY() + 6, getZ(), getYaw(), 0f);
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.HOSTILE, 5f, 0.5f);
        for (PlayerEntity p : playersNear(sw, 60)) {
            p.sendMessage(Text.translatable("boss.verden.razor.colapso").formatted(net.minecraft.util.Formatting.DARK_RED, net.minecraft.util.Formatting.BOLD), true);
        }
    }

    private void pull(ServerWorld sw, Vec3d to, double radius, double strength) {
        for (LivingEntity e : SpellUtil.around(sw, this, radius)) {
            if (e instanceof VerdenMonster || e instanceof LegionUnit) {
                continue;
            }
            Vec3d v = to.subtract(e.getPos().add(0, e.getHeight() / 2, 0));
            double len = Math.max(1.0, v.length());
            e.addVelocity(v.normalize().multiply(strength * Math.min(2.0, 12.0 / len)));
            e.velocityModified = true;
        }
    }

    private void runAct(ServerWorld sw) {
        LivingEntity t = getTarget();
        actTicks--;
        switch (act) {
            case TRANSICAO -> {
                setVelocity(0, 0.02, 0);
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getBodyY(0.5), getZ(), 6, 0.5, 0.9, 0.5, 0.1);
                sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, getX(), getBodyY(0.5), getZ(), 10, 0.8, 1.0, 0.8, 0.5);
                if (actTicks <= 0) {
                    this.dataTracker.set(PHASE, 2);
                    setNoGravity(false);
                    heal(getMaxHealth() * 0.15f);
                    getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(16);
                    getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.36);
                    sw.createExplosion(this, getX(), getBodyY(0.5), getZ(), 3f, World.ExplosionSourceType.NONE);
                    setAct(Act.NONE, 0);
                    cooldown = 30;
                }
            }
            case DURMA -> {
                if (actTicks == 4 && t != null && distanceTo(t) < 3.5) {
                    t.damage(getDamageSources().mobAttack(this), 12f);
                    sw.spawnParticles(ParticleTypes.CRIT, t.getX(), t.getBodyY(0.6), t.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                }
                if (actTicks <= 0) {
                    setAct(Act.NONE, 0);
                    cooldown = 50 + random.nextInt(30);
                }
            }
            case JI -> {
                int done = 70 - actTicks;
                for (int i = 0; i < 8; i++) {
                    double a = done * 0.5 + i * Math.PI / 4, r = 4.0 * (1 - done / 75.0) + 0.3;
                    sw.spawnParticles(ParticleTypes.SQUID_INK, center.x + Math.cos(a) * r, center.y + Math.sin(done * 0.3 + i) * r * 0.5, center.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                }
                sw.spawnParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 8, 0.4, 0.4, 0.4, 0.8);
                pull(sw, center, 22, 0.09);
                if (actTicks <= 0) {
                    kan(sw);
                    setAct(Act.NONE, 0);
                    cooldown = 70 + random.nextInt(40);
                }
            }
            case RAIO -> {
                if (t != null && (actTicks == 24 || actTicks == 16 || actTicks == 8)) {
                    getLookControl().lookAt(t, 60f, 60f);
                    Vec3d from = new Vec3d(getX(), getEyeY() - 0.2, getZ());
                    Vec3d to = new Vec3d(t.getX(), t.getBodyY(0.6), t.getZ());
                    for (int i = 1; i <= 16; i++) {
                        Vec3d p = from.lerp(to, i / 16.0);
                        sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
                        sw.spawnParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
                    }
                    t.damage(getDamageSources().indirectMagic(this, this), 7f);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
                    sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.8f);
                }
                if (actTicks <= 0) {
                    setAct(Act.NONE, 0);
                    cooldown = 45 + random.nextInt(25);
                }
            }
            case GARRA -> {
                if (actTicks > 4) {
                    sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX() + getRotationVec(1f).x * 2, getY() + 1.6, getZ() + getRotationVec(1f).z * 2, 3, 0.4, 0.4, 0.4, 0.02);
                } else if (actTicks == 4) {
                    Vec3d f = Vec3d.fromPolar(0, getYaw()).normalize();
                    sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX() + f.x * 2.5, getY() + 1.2, getZ() + f.z * 2.5, 6, 1.5, 0.4, 1.5, 0.0);
                    for (LivingEntity e : SpellUtil.around(sw, this, 6.5)) {
                        if (e instanceof VerdenMonster || e instanceof LegionUnit) {
                            continue;
                        }
                        Vec3d to = e.getPos().subtract(getPos()).normalize();
                        if (to.dotProduct(f) > 0.2) {
                            e.damage(getDamageSources().mobAttack(this), 14f);
                            e.setVelocity(to.multiply(1.5).add(0, 0.5, 0));
                            e.velocityModified = true;
                        }
                    }
                }
                if (actTicks <= 0) {
                    setAct(Act.NONE, 0);
                    cooldown = 40 + random.nextInt(20);
                }
            }
            case COLAPSO -> {
                setVelocity(Vec3d.ZERO);
                int done = 140 - actTicks;
                for (int i = 0; i < 10; i++) {
                    double a = done * 0.35 + i * Math.PI / 5, r = 12.0 * (1 - done / 150.0) + 0.5;
                    sw.spawnParticles(ParticleTypes.SQUID_INK, getX() + Math.cos(a) * r, getY() - 3 + Math.sin(done * 0.2 + i) * r * 0.4, getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
                }
                sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() - 2, getZ(), 15, 1.0, 1.0, 1.0, 1.0);
                pull(sw, getPos().add(0, -3, 0), 34, 0.07);
                if (actTicks <= 0) {
                    implode(sw);
                    setNoGravity(false);
                    setAct(Act.NONE, 0);
                    colapsoCd = 520;
                    cooldown = 80;
                }
            }
            default -> setAct(Act.NONE, 0);
        }
    }

    private void kan(ServerWorld sw) {
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 4f, 0.5f);
        sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 2, 1, 1, 1, 0.0);
        sw.spawnParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 120, 3, 1.5, 3, 0.6);
        for (LivingEntity e : SpellUtil.around(sw, this, 16)) {
            if (e instanceof VerdenMonster || e instanceof LegionUnit) {
                continue;
            }
            e.damage(getDamageSources().indirectMagic(this, this), 10f);
            Vec3d push = e.getPos().subtract(center).normalize().multiply(2.4).add(0, 0.7, 0);
            e.setVelocity(push);
            e.velocityModified = true;
        }
    }

    /** Colapso do Vazio: quem estiver à vista leva dano brutal; quem se esconde atrás de blocos sobrevive. */
    private void implode(ServerWorld sw) {
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 6f, 0.3f);
        sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() - 3, getZ(), 6, 3, 2, 3, 0.0);
        sw.spawnParticles(ParticleTypes.SQUID_INK, getX(), getY() - 2, getZ(), 300, 6, 3, 6, 0.8);
        Vec3d from = new Vec3d(getX(), getY() + 1.0, getZ());
        for (LivingEntity e : SpellUtil.around(sw, this, 45)) {
            if (e instanceof VerdenMonster || e instanceof LegionUnit) {
                continue;
            }
            Vec3d to = new Vec3d(e.getX(), e.getBodyY(0.5), e.getZ());
            boolean covered = sw.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() != HitResult.Type.MISS;
            if (!covered) {
                e.damage(getDamageSources().indirectMagic(this, this), 28f);
                e.setVelocity(e.getPos().subtract(getPos()).normalize().multiply(2.0).add(0, 0.6, 0));
                e.velocityModified = true;
            } else if (e instanceof PlayerEntity p) {
                p.sendMessage(Text.translatable("boss.verden.razor.covered"), true);
            }
        }
    }

    // ------------------------------------------------------------------ resto
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

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Phase", getPhase());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(PHASE, Math.max(1, nbt.getInt("Phase")));
        if (getPhase() == 2) {
            getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(16);
        }
    }
}
