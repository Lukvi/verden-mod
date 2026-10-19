package com.verden.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Lança de Gelo (Lucy): cada golpe congela e retarda o alvo. */
public class FrostSpearItem extends SwordItem {
    public FrostSpearItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setFrozenTicks(target.getMinFreezeDamageTicks() + 120);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.lanca_gelo").formatted(Formatting.AQUA));
    }
}
