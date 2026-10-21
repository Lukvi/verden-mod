package com.verden.event;

import com.verden.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/** Cartola de Kitsuji: imunidade a efeitos mentais (lentidão, cegueira, fome, fadiga) do Razor e afins. */
public final class RazorEvents {
    private RazorEvents() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 10 != 0) {
                return;
            }
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (p.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.CARTOLA_KITSUJI)) {
                    p.removeStatusEffect(StatusEffects.SLOWNESS);
                    p.removeStatusEffect(StatusEffects.BLINDNESS);
                    p.removeStatusEffect(StatusEffects.HUNGER);
                    p.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                    p.removeStatusEffect(StatusEffects.NAUSEA);
                }
            }
        });
    }
}
