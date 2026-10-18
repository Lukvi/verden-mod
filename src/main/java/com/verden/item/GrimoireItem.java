package com.verden.item;

import com.verden.magic.Power;
import com.verden.magic.Spells;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Grimório: clique direito conjura o poder selecionado; agachado + clique direito troca de poder. */
public class GrimoireItem extends Item {
    private static final String KEY = "verden_power";

    public GrimoireItem(Settings settings) {
        super(settings);
    }

    public static Power getPower(ItemStack stack) {
        NbtComponent c = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return Power.byIndex(c.copyNbt().getInt(KEY));
    }

    public static void setPower(ItemStack stack, Power p) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt(KEY, p.ordinal()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            Power next = getPower(stack).next(Spells.hasTriquetra(player));
            setPower(stack, next);
            if (!world.isClient) {
                player.sendMessage(Text.translatable("message.verden.power_selected", next.displayName()), true);
            }
            return TypedActionResult.success(stack, world.isClient);
        }
        if (!world.isClient) {
            Power p = getPower(stack);
            Spells.cast(p, (ServerWorld) world, player);
            player.getItemCooldownManager().set(this, p.cooldown);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.power", getPower(stack).displayName()).formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("tooltip.verden.grimorio").formatted(Formatting.GRAY));
    }
}
