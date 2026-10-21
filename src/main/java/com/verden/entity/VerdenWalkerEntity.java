package com.verden.entity;

import com.verden.world.gen.RoadNet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

/**
 * Morador andante de Verden: camponeses, guardas, magos, caçadores, mercenários, mercadores, sacerdotes e piratas.
 * Passeiam pelas cidades, seguem as estradas em caravanas e combatem os monstros do livro.
 */
public class VerdenWalkerEntity extends PathAwareEntity {
    public enum Kind {
        CAMPONES("campones", false), CAMPONESA("camponesa", false), VIAJANTE("viajante", false),
        GUARDA("guarda", true), MAGO("mago", true), CACADOR("cacador", true), MERCENARIO("mercenario", true),
        MERCADOR("mercador", false), SACERDOTE("sacerdote", false), PIRATA("pirata", true);

        public final String id;
        public final boolean fighter;

        Kind(String id, boolean fighter) {
            this.id = id;
            this.fighter = fighter;
        }

        public static Kind byIndex(int i) {
            Kind[] v = values();
            return v[Math.floorMod(i, v.length)];
        }
    }

    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(VerdenWalkerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /** Caravana: índice da estrada, posição ao longo dela e sentido (+1/-1). roadIdx < 0 = não segue estrada. */
    private int roadIdx = -1;
    private double roadT = 0;
    private int roadDir = 1;
    private int lineIndex = 0;

    public VerdenWalkerEntity(EntityType<? extends VerdenWalkerEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    public Kind getKind() {
        return Kind.byIndex(this.dataTracker.get(VARIANT));
    }

    public void setKind(Kind k) {
        this.dataTracker.set(VARIANT, k.ordinal());
        equipFor(k);
    }

    /** Põe o personagem numa estrada (caravana). */
    public void followRoad(int idx, double t, int dir) {
        this.roadIdx = idx;
        this.roadT = t;
        this.roadDir = dir;
    }

    public boolean isCaravan() {
        return roadIdx >= 0;
    }

    private void equipFor(Kind k) {
        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        switch (k) {
            case GUARDA -> {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
            case MAGO -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BLAZE_ROD));
            case CACADOR -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            case MERCENARIO -> {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            }
            case PIRATA -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            case SACERDOTE -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
            case MERCADOR -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD));
            default -> { }
        }
        for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            this.setEquipmentDropChance(s, 0f);
        }
        if (k == Kind.GUARDA || k == Kind.MERCENARIO) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40);
            this.setHealth(40f);
        }
    }

    // ------------------------------------------------------------------ IA
    private boolean civil() {
        return !getKind().fighter;
    }

    private boolean melee() {
        Kind k = getKind();
        return k == Kind.GUARDA || k == Kind.MERCENARIO || k == Kind.PIRATA;
    }

    private boolean ranged() {
        Kind k = getKind();
        return k == Kind.MAGO || k == Kind.CACADOR;
    }

    private final class KindTarget<T extends LivingEntity> extends ActiveTargetGoal<T> {
        KindTarget(Class<T> cls) {
            super(VerdenWalkerEntity.this, cls, true);
        }

        @Override
        public boolean canStart() {
            return getKind().fighter && super.canStart();
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.35) {
            @Override
            public boolean canStart() {
                return civil() && super.canStart();
            }
        });
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.15, true) {
            @Override
            public boolean canStart() {
                return melee() && super.canStart();
            }

            @Override
            public boolean shouldContinue() {
                return melee() && super.shouldContinue();
            }
        });
        this.goalSelector.add(2, new CastGoal());
        this.goalSelector.add(5, new RoadGoal());
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.5));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this) {
            @Override
            public boolean canStart() {
                return getKind().fighter && super.canStart();
            }
        });
        this.targetSelector.add(2, new KindTarget<>(LicantropoEntity.class));
        this.targetSelector.add(2, new KindTarget<>(LicantropoAdaptadoEntity.class));
        this.targetSelector.add(2, new KindTarget<>(KunoriEntity.class));
        this.targetSelector.add(3, new KindTarget<>(ZytraEntity.class));
        this.targetSelector.add(3, new KindTarget<>(HaigolEntity.class));
        this.targetSelector.add(3, new KindTarget<>(TentaculoEntity.class));
        this.targetSelector.add(4, new KindTarget<>(AmalgamaKooldEntity.class));
    }

    /** Magos lançam feitiços e caçadores atiram, ambos à distância. */
    private final class CastGoal extends Goal {
        private int cooldown;

        @Override
        public boolean canStart() {
            LivingEntity t = getTarget();
            return ranged() && t != null && t.isAlive() && squaredDistanceTo(t) < 22 * 22;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void start() {
            cooldown = 15;
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) {
                return;
            }
            getLookControl().lookAt(t, 30f, 30f);
            double d = distanceTo(t);
            if (d > 13) {
                getNavigation().startMovingTo(t, 0.9);
            } else {
                getNavigation().stop();
            }
            if (--cooldown <= 0 && getVisibilityCache().canSee(t)) {
                cast(t);
                cooldown = getKind() == Kind.MAGO ? 45 : 30;
            }
        }
    }

    private void cast(LivingEntity t) {
        if (!(getWorld() instanceof ServerWorld sw)) {
            return;
        }
        swingHand(Hand.MAIN_HAND);
        double x0 = getX(), y0 = getEyeY() - 0.2, z0 = getZ();
        double x1 = t.getX(), y1 = t.getBodyY(0.6), z1 = t.getZ();
        net.minecraft.particle.ParticleEffect fx;
        SoundEvent snd;
        if (getKind() == Kind.CACADOR) {
            t.damage(getDamageSources().mobAttack(this), 6f);
            fx = ParticleTypes.CRIT;
            snd = SoundEvents.ENTITY_ARROW_SHOOT;
        } else {
            switch (random.nextInt(3)) {
                case 0 -> {
                    t.damage(getDamageSources().indirectMagic(this, this), 5f);
                    t.setOnFireFor(4);
                    fx = ParticleTypes.FLAME;
                    snd = SoundEvents.ENTITY_BLAZE_SHOOT;
                }
                case 1 -> {
                    t.damage(getDamageSources().indirectMagic(this, this), 4f);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2));
                    fx = ParticleTypes.SNOWFLAKE;
                    snd = SoundEvents.ENTITY_SNOW_GOLEM_SHOOT;
                }
                default -> {
                    t.damage(getDamageSources().indirectMagic(this, this), 3f);
                    t.takeKnockback(1.3, x0 - x1, z0 - z1);
                    fx = ParticleTypes.CLOUD;
                    snd = SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL;
                }
            }
        }
        int n = 12;
        for (int i = 1; i <= n; i++) {
            double f = i / (double) n;
            sw.spawnParticles(fx, x0 + (x1 - x0) * f, y0 + (y1 - y0) * f, z0 + (z1 - z0) * f, 1, 0.03, 0.03, 0.03, 0.0);
        }
        playSound(snd, 1.0f, 0.9f + random.nextFloat() * 0.3f);
    }

    /** Caravanas: seguem a estrada de um extremo ao outro e voltam. */
    private final class RoadGoal extends Goal {
        private int ticks;

        @Override
        public boolean canStart() {
            return roadIdx >= 0 && roadIdx < RoadNet.all().size() && getTarget() == null;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            if (--ticks > 0 && !getNavigation().isIdle()) {
                return;
            }
            ticks = 10;
            RoadNet.Road r = RoadNet.all().get(roadIdx);
            // reprojeta a posição atual na estrada, perto do progresso conhecido
            double best = Double.MAX_VALUE, bt = roadT;
            double lo = Math.max(0, roadT - (roadDir > 0 ? 30 : 70)), hi = Math.min(r.length, roadT + (roadDir > 0 ? 70 : 30));
            for (double s = lo; s <= hi; s += 4) {
                double[] p = r.at(s);
                double dd = Math.hypot(p[0] - getX(), p[1] - getZ());
                if (dd < best) {
                    best = dd;
                    bt = s;
                }
            }
            roadT = bt;
            if (roadDir > 0 && roadT >= r.length - 16) {
                roadDir = -1;
            } else if (roadDir < 0 && roadT <= 16) {
                roadDir = 1;
            }
            double[] a = r.at(Math.max(0, Math.min(r.length, roadT + roadDir * 14)));
            int bx = (int) Math.round(a[0]), bz = (int) Math.round(a[1]);
            if (!getWorld().isChunkLoaded(bx >> 4, bz >> 4)) {
                return;      // não força a geração de chunks distantes no thread do servidor
            }
            int by = getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            getNavigation().startMovingTo(bx + 0.5, by, bz + 0.5, 0.6);
        }
    }

    // ------------------------------------------------------------------ comportamento
    @Override
    protected void mobTick() {
        super.mobTick();
        if (getKind() == Kind.SACERDOTE && !getWorld().isClient && this.age % 80 == 0) {
            List<LivingEntity> near = getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(8),
                    e -> e != this && (e instanceof PlayerEntity || e instanceof VerdenWalkerEntity || e instanceof VerdenNpcEntity)
                            && e.getHealth() < e.getMaxHealth());
            for (LivingEntity e : near) {
                e.heal(4f);
                if (getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.HEART, e.getX(), e.getBodyY(1.0), e.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                }
            }
        }
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("walker.verden." + getKind().id + ".name");
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        if (!getWorld().isClient) {
            String key = "walker.verden." + getKind().id + ".line." + (lineIndex % 3);
            lineIndex++;
            player.sendMessage(Text.empty().append(Text.translatable("walker.verden." + getKind().id + ".name").formatted(Formatting.GOLD))
                    .append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.translatable(key)), false);
        }
        return ActionResult.success(getWorld().isClient);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Kind", getKind().ordinal());
        nbt.putInt("RoadIdx", roadIdx);
        nbt.putDouble("RoadT", roadT);
        nbt.putInt("RoadDir", roadDir);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(VARIANT, nbt.getInt("Kind"));
        roadIdx = nbt.contains("RoadIdx") ? nbt.getInt("RoadIdx") : -1;
        roadT = nbt.getDouble("RoadT");
        roadDir = nbt.getInt("RoadDir") == 0 ? 1 : nbt.getInt("RoadDir");
    }

    /** Distância de despawn: quem vagueia longe do jogador some, mas moradores de cidade são repostos. */
    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return distanceSquared > 96 * 96;
    }

    public BlockPos homeOrPos() {
        return getBlockPos();
    }
}
