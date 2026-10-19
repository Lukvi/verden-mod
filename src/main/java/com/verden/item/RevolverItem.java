package com.verden.item;

import com.verden.magic.SpellUtil;
import com.verden.registry.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Revólver Cubus: tiro instantâneo, consome Munição .45. */
public class RevolverItem extends Item {
    public RevolverItem(Settings settings) {
        super(settings);
    }

    private static ItemStack findAmmo(PlayerEntity p) {
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(ModItems.MUNICAO_INCENDIARIA) || s.isOf(ModItems.MUNICAO_PERFURANTE)) {
                return s;
            }
        }
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(ModItems.MUNICAO_45)) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack gun = player.getStackInHand(hand);
        ItemStack ammo = findAmmo(player);
        if (ammo.isEmpty() && !player.isCreative()) {
            if (!world.isClient) {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1f, 1.5f);
                player.sendMessage(Text.translatable("message.verden.no_ammo"), true);
            }
            return TypedActionResult.fail(gun);
        }
        if (!world.isClient) {
            boolean fire = ammo.isOf(ModItems.MUNICAO_INCENDIARIA);
            boolean pierce = ammo.isOf(ModItems.MUNICAO_PERFURANTE);
            if (!player.isCreative()) {
                ammo.decrement(1);
            }
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.5f, 0.6f);
            LivingEntity t = SpellUtil.raycastLiving(world, player, 48, fire ? ParticleTypes.FLAME : ParticleTypes.SMOKE);
            if (t != null) {
                if (pierce) {
                    t.damage(world.getDamageSources().indirectMagic(player, player), 13f); // ignora armadura
                } else {
                    t.damage(world.getDamageSources().playerAttack(player), fire ? 9f : 11f);
                }
                if (fire) {
                    t.setOnFireFor(5);
                }
            }
            player.getItemCooldownManager().set(this, 15);
            gun.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return TypedActionResult.success(gun, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.revolver").formatted(Formatting.GRAY));
    }
}
