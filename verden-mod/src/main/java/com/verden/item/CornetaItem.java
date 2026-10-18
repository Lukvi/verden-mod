package com.verden.item;

import com.verden.entity.RinoyEntity;
import com.verden.registry.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Corneta de Lucy: chama Rinoy, a montaria gigante de Koold. */
public class CornetaItem extends Item {
    public CornetaItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.PLAYERS, 1.5f, 0.6f);
            RinoyEntity rinoy = ModEntities.RINOY.create(sw);
            if (rinoy != null) {
                rinoy.refreshPositionAndAngles(player.getX() + 2, player.getY(), player.getZ(), player.getYaw(), 0f);
                rinoy.setTame(true);
                rinoy.setPersistent();
                sw.spawnEntity(rinoy);
            }
            player.getItemCooldownManager().set(this, 600);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.corneta").formatted(Formatting.AQUA));
    }
}
