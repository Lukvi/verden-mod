package com.verden.entity;

import com.verden.registry.ModItems;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

/** Personagens da história que aparecem como NPCs. Cada um tem falas e, alguns, uma loja. */
public enum NpcRole {
    LUKE("luke", 4),
    LUCY("lucy", 4),
    NOITE("noite", 4),
    MILLY("milly", 4),
    LIAM("liam", 4),
    HIRAKU("hiraku", 4),
    ROSE("rose", 4),
    VANESSA("vanessa", 4),
    KONGE("konge", 4),
    MENETRYX("menetryx", 4),
    RASHAD("rashad", 4),
    YANN("yann", 4),
    THALEN("thalen", 4),
    TAVERNEIRO("taverneiro", 4),
    RAONI("raoni", 4),
    FERREIRO("ferreiro", 4),
    ALQUIMISTA("alquimista", 4),
    ARMEIRO("armeiro", 4),
    MERCEIRO("merceiro", 4);

    public final String id;
    public final int lines;

    NpcRole(String id, int lines) {
        this.id = id;
        this.lines = lines;
    }

    public static NpcRole byIndex(int i) {
        NpcRole[] v = values();
        return v[Math.floorMod(i, v.length)];
    }

    public boolean isMerchant() {
        return switch (this) {
            case LUKE, NOITE, LIAM, KONGE, RAONI -> false;
            default -> true;
        };
    }

    // ---- Loja ----
    private static void deal(TradeOfferList l, ItemConvertible pay, int pc, ItemConvertible sell, int sc) {
        l.add(new TradeOffer(new TradedItem(pay, pc), new ItemStack(sell, sc), 12, 4, 0.05f));
    }

    public void fillTrades(TradeOfferList l) {
        ItemConvertible c = ModItems.COPTA, m = ModItems.MONTER;
        switch (this) {
            case MILLY -> {
                deal(l, c, 3, Items.ARROW, 16);
                deal(l, c, 4, ModItems.POCAO_VISAO_NOTURNA, 1);
                deal(l, c, 2, Items.BREAD, 4);
            }
            case HIRAKU -> {
                deal(l, m, 5, ModItems.KATANA_CHAMAS, 1);
                deal(l, c, 4, ModItems.POCAO_RECUPERACAO, 1);
            }
            case ROSE -> {
                deal(l, c, 6, ModItems.MUNICAO_45, 6);
                deal(l, m, 2, ModItems.MUNICAO_INCENDIARIA, 6);
                deal(l, m, 3, ModItems.MUNICAO_PERFURANTE, 6);
                deal(l, m, 8, ModItems.REVOLVER_CUBUS, 1);
            }
            case VANESSA -> {
                deal(l, c, 3, ModItems.POCAO_RECUPERACAO, 1);
                deal(l, c, 5, ModItems.ELIXIR_CALOR, 1);
            }
            case LUCY -> {
                deal(l, m, 5, ModItems.LANCA_GELO, 1);
                deal(l, m, 3, ModItems.RINOY_CAPUZ, 1);
                deal(l, m, 5, ModItems.RINOY_CASACO, 1);
                deal(l, m, 4, ModItems.RINOY_CALCAS, 1);
                deal(l, m, 3, ModItems.RINOY_BOTAS, 1);
            }
            case MENETRYX -> {
                // Recompensas da Legião
                deal(l, ModItems.GARRA_LICANTROPO, 4, m, 1);
                deal(l, ModItems.INSIGNIA_LEGIAO, 3, c, 4);
                deal(l, m, 3, ModItems.LEGIAO_CAPACETE, 1);
                deal(l, m, 5, ModItems.LEGIAO_PEITORAL, 1);
                deal(l, m, 4, ModItems.LEGIAO_CALCAS, 1);
                deal(l, m, 3, ModItems.LEGIAO_BOTAS, 1);
            }
            case YANN -> {
                deal(l, m, 2, ModItems.ESPADA_LEGIAO, 1);
                deal(l, ModItems.MNEUMONIO, 2, m, 1);
                deal(l, ModItems.GARRA_LICANTROPO, 6, ModItems.POCAO_RECUPERACAO, 2);
            }
            case RASHAD -> {
                deal(l, m, 1, c, 9);
                deal(l, c, 9, m, 1);
                deal(l, m, 2, ModItems.BUSSOLA, 1);
                deal(l, m, 3, ModItems.LANTERNA_FAUNO, 1);
                deal(l, m, 4, ModItems.MOCHILA_COURO, 1);
                deal(l, c, 2, ModItems.RACAO_VIAGEM, 2);
                deal(l, ModItems.MNEUMONIO, 1, m, 1);
            }
            case THALEN -> {
                deal(l, ModItems.MNEUMONIO, 2, m, 1);
                deal(l, c, 6, ModItems.ELIXIR_CALOR, 1);
                deal(l, c, 5, ModItems.POCAO_RECUPERACAO, 1);
                deal(l, m, 3, ModItems.LANTERNA_FAUNO, 1);
            }
            case TAVERNEIRO -> {
                deal(l, c, 1, Items.BREAD, 3);
                deal(l, c, 2, Items.COOKED_BEEF, 2);
                deal(l, c, 3, ModItems.RACAO_VIAGEM, 2);
                deal(l, c, 4, Items.GOLDEN_CARROT, 2);
            }
            case FERREIRO -> {
                deal(l, c, 8, Items.IRON_SWORD, 1);
                deal(l, c, 6, Items.IRON_PICKAXE, 1);
                deal(l, c, 12, Items.IRON_CHESTPLATE, 1);
                deal(l, c, 5, Items.SHIELD, 1);
                deal(l, Items.IRON_INGOT, 8, c, 4);
                deal(l, m, 6, Items.DIAMOND_SWORD, 1);
            }
            case ALQUIMISTA -> {
                deal(l, c, 3, ModItems.POCAO_RECUPERACAO, 1);
                deal(l, c, 4, ModItems.POCAO_VISAO_NOTURNA, 1);
                deal(l, c, 5, ModItems.ELIXIR_CALOR, 1);
                deal(l, c, 6, Items.EXPERIENCE_BOTTLE, 3);
                deal(l, m, 2, Items.ENDER_PEARL, 1);
            }
            case ARMEIRO -> {
                deal(l, c, 6, Items.BOW, 1);
                deal(l, c, 2, Items.ARROW, 16);
                deal(l, c, 10, Items.CROSSBOW, 1);
                deal(l, m, 3, ModItems.ESPADA_LEGIAO, 1);
                deal(l, c, 7, Items.CHAINMAIL_CHESTPLATE, 1);
            }
            case MERCEIRO -> {
                deal(l, c, 1, Items.BREAD, 4);
                deal(l, c, 2, Items.COOKED_PORKCHOP, 3);
                deal(l, c, 2, Items.TORCH, 16);
                deal(l, c, 3, Items.LANTERN, 1);
                deal(l, c, 4, Items.SADDLE, 1);
                deal(l, m, 1, ModItems.MOCHILA_COURO, 1);
            }
            default -> {
            }
        }
    }
}
