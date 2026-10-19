package com.verden.entity;

import com.verden.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

/**
 * NPC genérico da história. O personagem é definido pelo "variant" (ver {@link NpcRole}):
 * clique direito para conversar; quem tem loja abre o comércio.
 */
public class VerdenNpcEntity extends MerchantEntity {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(VerdenNpcEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int lineIndex = 0;

    public VerdenNpcEntity(EntityType<? extends VerdenNpcEntity> type, World world) {
        super(type, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    public int getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public void setVariant(int v) {
        this.dataTracker.set(VARIANT, v);
    }

    public NpcRole getRole() {
        return NpcRole.byIndex(getVariant());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new TradeWithPlayerGoal(this));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.5));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8f));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("npc.verden." + getRole().id + ".name");
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity p && !p.isCreative()) {
            return false; // os moradores de Verden não são alvo do jogador
        }
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        if (!this.getWorld().isClient) {
            NpcRole role = getRole();
            String key = "npc.verden." + role.id + ".line." + (lineIndex % role.lines);
            lineIndex++;
            player.sendMessage(Text.empty().append(Text.translatable("npc.verden." + role.id + ".name").formatted(Formatting.GOLD))
                    .append(Text.literal(": ").formatted(Formatting.GRAY))
                    .append(Text.translatable(key)), false);
            this.getWorld().playSound(null, this.getBlockPos(), ModSounds.NPC_TALK, SoundCategory.NEUTRAL, 0.8f, 0.9f + this.random.nextFloat() * 0.3f);
            if (role.isMerchant() && !this.hasCustomer() && !this.isBaby()) {
                player.sendMessage(Text.translatable("message.verden.npc_trade_hint").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), true);
                this.setCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    @Override
    protected void fillRecipes() {
        getRole().fillTrades(this.getOffers());
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (this.getWorld() instanceof ServerWorld) {
            this.playSound(ModSounds.COIN, 1f, 1f);
        }
    }

    @Override
    public SoundEvent getYesSound() {
        return ModSounds.COIN;
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", getVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setVariant(nbt.getInt("Variant"));
    }
}
