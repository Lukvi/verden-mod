package com.verden.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Cromus, a espada de Noite: clique direito lança um corte de energia à frente. */
public class CromusItem extends SwordItem {
    public CromusItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
            ServerWorld sw = (ServerWorld) world;
            Vec3d eye = player.getEyePos();
            Vec3d dir = player.getRotationVec(1f).normalize();
            Set<LivingEntity> hit = new HashSet<>();
            for (int d = 2; d <= 7; d++) {
                Vec3d p = eye.add(dir.multiply(d));
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                Box box = new Box(p.x - 1.3, p.y - 1.3, p.z - 1.3, p.x + 1.3, p.y + 1.3, p.z + 1.3);
                hit.addAll(sw.getEntitiesByClass(LivingEntity.class, box, e -> e != player && e.isAlive()));
            }
            for (LivingEntity e : hit) {
                e.damage(sw.getDamageSources().playerAttack(player), 9f);
            }
            sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1f, 0.7f);
            player.getItemCooldownManager().set(this, 40);
            stack.damage(2, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.verden.cromus").formatted(Formatting.DARK_PURPLE));
    }
}
