package com.verden.event;

import com.verden.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

/** Bônus de conjunto de armadura. */
public final class ModEvents {
    private ModEvents() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 40 != 0) {
                return;
            }
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (fullSet(p, ModItems.LEGIAO_CAPACETE, ModItems.LEGIAO_PEITORAL, ModItems.LEGIAO_CALCAS, ModItems.LEGIAO_BOTAS)) {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 0, true, false, true));
                }
                if (fullSet(p, ModItems.RINOY_CAPUZ, ModItems.RINOY_CASACO, ModItems.RINOY_CALCAS, ModItems.RINOY_BOTAS)) {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 0, true, false, true));
                    p.setFrozenTicks(0);
                }
            }
        });
    }

    private static boolean fullSet(ServerPlayerEntity p, Item head, Item chest, Item legs, Item feet) {
        return p.getEquippedStack(EquipmentSlot.HEAD).isOf(head) && p.getEquippedStack(EquipmentSlot.CHEST).isOf(chest)
                && p.getEquippedStack(EquipmentSlot.LEGS).isOf(legs) && p.getEquippedStack(EquipmentSlot.FEET).isOf(feet);
    }
}
