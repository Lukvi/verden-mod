package com.verden.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/** Bússola de Verden: mostra coordenadas e aponta (com uma seta) para o ponto de partida do mundo. */
public class VerdenCompassItem extends Item {
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    public VerdenCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            BlockPos home = world.getSpawnPos();
            double dx = home.getX() + 0.5 - player.getX();
            double dz = home.getZ() + 0.5 - player.getZ();
            float target = (float) (MathHelper.atan2(dz, dx) * 57.29577951308232) - 90f;
            float rel = MathHelper.wrapDegrees(target - player.getYaw());
            int idx = Math.floorMod(Math.round(rel / 45f), 8);
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            player.sendMessage(Text.translatable("message.verden.bussola",
                    player.getBlockX(), player.getBlockY(), player.getBlockZ(), ARROWS[idx], dist), true);
        }
        return TypedActionResult.success(player.getStackInHand(hand), world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.bussola").formatted(Formatting.GRAY));
    }
}
