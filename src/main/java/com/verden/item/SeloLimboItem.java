package com.verden.item;

import com.verden.entity.RazorKitsujiEntity;
import com.verden.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/** Selo do Limbo: rompe o véu e invoca Razor Kitsuji no bloco clicado. Só um por vez por região. */
public class SeloLimboItem extends Item {
    public SeloLimboItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World w = ctx.getWorld();
        if (!(w instanceof ServerWorld sw)) {
            return ActionResult.SUCCESS;
        }
        BlockPos pos = ctx.getBlockPos().offset(ctx.getSide());
        PlayerEntity p = ctx.getPlayer();
        if (!sw.getEntitiesByClass(RazorKitsujiEntity.class, new net.minecraft.util.math.Box(pos).expand(120), e -> true).isEmpty()) {
            if (p != null) {
                p.sendMessage(Text.translatable("message.verden.razor_present"), true);
            }
            return ActionResult.FAIL;
        }
        RazorKitsujiEntity r = ModEntities.RAZOR.create(sw);
        if (r == null) {
            return ActionResult.FAIL;
        }
        r.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p != null ? p.getYaw() + 180f : 0f, 0f);
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(sw);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            bolt.setCosmetic(true);
            sw.spawnEntity(bolt);
        }
        sw.spawnParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 200, 2, 2, 2, 0.4);
        sw.spawnEntity(r);
        if (p != null) {
            p.sendMessage(Text.translatable("message.verden.razor_summoned").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), false);
            if (!p.isCreative()) {
                ctx.getStack().decrement(1);
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.selo_limbo").formatted(Formatting.DARK_PURPLE));
    }
}
