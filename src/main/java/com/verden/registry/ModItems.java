package com.verden.registry;

import com.verden.VerdenMod;
import com.verden.entity.NpcRole;
import com.verden.item.BackpackItem;
import com.verden.item.CornetaItem;
import com.verden.item.FlameKatanaItem;
import com.verden.item.FrostSpearItem;
import com.verden.item.LanternItem;
import com.verden.item.NpcSpawnItem;
import com.verden.item.VerdenCompassItem;
import com.verden.item.WarmthElixirItem;
import net.minecraft.item.ArmorItem;
import com.verden.item.CromusItem;
import com.verden.item.GrimoireItem;
import com.verden.item.RevolverItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public final class ModItems {
    private ModItems() {}

    public static final Item GRIMORIO = reg("grimorio",
            new GrimoireItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

    public static final Item CROMUS = reg("cromus",
            new CromusItem(ToolMaterials.NETHERITE, new Item.Settings().rarity(Rarity.EPIC).fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 5, -2.0f))));

    public static final Item BERSERKER = reg("berserker",
            new BowItem(new Item.Settings().maxDamage(900).rarity(Rarity.RARE)));

    public static final Item REVOLVER_CUBUS = reg("revolver_cubus",
            new RevolverItem(new Item.Settings().maxDamage(600).rarity(Rarity.RARE)));

    public static final Item MUNICAO_45 = reg("municao_45", new Item(new Item.Settings()));

    public static final Item CORNETA_LUCY = reg("corneta_lucy",
            new CornetaItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

    public static final Item MNEUMONIO = reg("mneumonio", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    public static final Item TRIQUETRA = reg("amuleto_triquetra",
            new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

    public static final Item COROA_KUNORI = reg("coroa_espinhos_kunori",
            new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    public static final Item MACHADO_HIRAKU = reg("machado_duplo_hiraku",
            new AxeItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(MiningToolItem.createAttributeModifiers(ToolMaterials.DIAMOND, 7, -3.0f))));

    public static final Item ESPADA_LIAM = reg("espada_curva_liam",
            new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 4, -2.0f))));

    public static final Item ESPADA_LEGIAO = reg("espada_legiao",
            new SwordItem(ToolMaterials.IRON, new Item.Settings().rarity(Rarity.UNCOMMON)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 3, -2.4f))));

    public static final Item ADAGA_ZYTRA = reg("adaga_zytra",
            new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 2, -1.6f))));

    public static final Item ESPADA_HAIGOL = reg("espada_haigol",
            new SwordItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 6, -2.8f))));

    public static final Item INSIGNIA_LEGIAO = reg("insignia_legiao", new Item(new Item.Settings()));

    public static final Item POCAO_RECUPERACAO = reg("pocao_recuperacao",
            new Item(new Item.Settings().maxCount(16).food(new FoodComponent.Builder()
                    .nutrition(2).saturationModifier(0.3f).alwaysEdible()
                    .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1), 1.0f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1), 1.0f)
                    .build())));

    public static final Item OVO_LICANTROPO_ADAPTADO = reg("licantropo_adaptado_spawn_egg",
            new SpawnEggItem(ModEntities.LICANTROPO_ADAPTADO, 0x2d262a, 0xff2828, new Item.Settings()));
    public static final Item OVO_SOLDADO = reg("soldado_legiao_spawn_egg",
            new SpawnEggItem(ModEntities.SOLDADO_LEGIAO, 0x6e737d, 0xa01e28, new Item.Settings()));
    public static final Item OVO_ZYTRA = reg("zytra_twin_spawn_egg",
            new SpawnEggItem(ModEntities.ZYTRA, 0x8c91a0, 0xffd250, new Item.Settings()));
    public static final Item OVO_HAIGOL = reg("haigol_twin_spawn_egg",
            new SpawnEggItem(ModEntities.HAIGOL, 0x32343e, 0xffd250, new Item.Settings()));
    public static final Item OVO_CONDOLYUS = reg("condolyus_spawn_egg",
            new SpawnEggItem(ModEntities.CONDOLYUS, 0xe66e1e, 0xffd23c, new Item.Settings()));

    public static final Item OVO_LICANTROPO = reg("licantropo_spawn_egg",
            new SpawnEggItem(ModEntities.LICANTROPO, 0x4a3a2a, 0xa02020, new Item.Settings()));
    public static final Item OVO_KUNORI = reg("encapuzado_kunori_spawn_egg",
            new SpawnEggItem(ModEntities.KUNORI, 0x201020, 0xc03030, new Item.Settings()));
    public static final Item OVO_TENTACULO = reg("tentaculo_spawn_egg",
            new SpawnEggItem(ModEntities.TENTACULO, 0x501040, 0x9a40a0, new Item.Settings()));
    public static final Item OVO_AMALGAMA = reg("amalgama_koold_spawn_egg",
            new SpawnEggItem(ModEntities.AMALGAMA, 0x30103a, 0xff5020, new Item.Settings()));
    public static final Item OVO_RINOY = reg("rinoy_spawn_egg",
            new SpawnEggItem(ModEntities.RINOY, 0x2a2420, 0xd8e0e8, new Item.Settings()));

    // ---- Moedas e materiais ----
    public static final Item MONTER = reg("monter", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item COPTA = reg("copta_flereniana", new Item(new Item.Settings()));
    public static final Item GARRA_LICANTROPO = reg("garra_licantropo", new Item(new Item.Settings()));

    // ---- Utilidades ----
    public static final Item BUSSOLA = reg("bussola_verden", new VerdenCompassItem(new Item.Settings().maxCount(1)));
    public static final Item LANTERNA_FAUNO = reg("lanterna_fauno", new LanternItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item MOCHILA_COURO = reg("mochila_couro", new BackpackItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item RACAO_VIAGEM = reg("racao_viagem",
            new Item(new Item.Settings().maxCount(32).food(new FoodComponent.Builder()
                    .nutrition(8).saturationModifier(0.9f).build())));
    public static final Item POCAO_VISAO_NOTURNA = reg("pocao_visao_noturna",
            new Item(new Item.Settings().maxCount(16).food(new FoodComponent.Builder()
                    .nutrition(1).saturationModifier(0.1f).alwaysEdible()
                    .statusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600, 0), 1.0f).build())));
    public static final Item ELIXIR_CALOR = reg("elixir_calor",
            new WarmthElixirItem(new Item.Settings().maxCount(16).food(new FoodComponent.Builder()
                    .nutrition(1).saturationModifier(0.1f).alwaysEdible().build())));

    // ---- Armas por afinidade ----
    public static final Item LANCA_GELO = reg("lanca_gelo",
            new FrostSpearItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 4, -2.4f))));
    public static final Item KATANA_CHAMAS = reg("katana_chamas",
            new FlameKatanaItem(ToolMaterials.DIAMOND, new Item.Settings().rarity(Rarity.RARE).fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 4, -2.0f))));
    public static final Item MUNICAO_INCENDIARIA = reg("municao_incendiaria", new Item(new Item.Settings()));
    public static final Item MUNICAO_PERFURANTE = reg("municao_perfurante", new Item(new Item.Settings()));

    // ---- Armaduras ----
    public static final Item LEGIAO_CAPACETE = armor("legiao_capacete", ModArmor.LEGIAO, ArmorItem.Type.HELMET, 24);
    public static final Item LEGIAO_PEITORAL = armor("legiao_peitoral", ModArmor.LEGIAO, ArmorItem.Type.CHESTPLATE, 24);
    public static final Item LEGIAO_CALCAS = armor("legiao_calcas", ModArmor.LEGIAO, ArmorItem.Type.LEGGINGS, 24);
    public static final Item LEGIAO_BOTAS = armor("legiao_botas", ModArmor.LEGIAO, ArmorItem.Type.BOOTS, 24);
    public static final Item RINOY_CAPUZ = armor("rinoy_capuz", ModArmor.PELE_RINOY, ArmorItem.Type.HELMET, 18);
    public static final Item RINOY_CASACO = armor("rinoy_casaco", ModArmor.PELE_RINOY, ArmorItem.Type.CHESTPLATE, 18);
    public static final Item RINOY_CALCAS = armor("rinoy_calcas", ModArmor.PELE_RINOY, ArmorItem.Type.LEGGINGS, 18);
    public static final Item RINOY_BOTAS = armor("rinoy_botas", ModArmor.PELE_RINOY, ArmorItem.Type.BOOTS, 18);

    // ---- Ovos de NPC (um por personagem) ----
    public static final Item[] NPC_EGGS = createNpcEggs();

    private static Item[] createNpcEggs() {
        NpcRole[] roles = NpcRole.values();
        Item[] eggs = new Item[roles.length];
        for (int i = 0; i < roles.length; i++) {
            eggs[i] = reg("npc_" + roles[i].id, new NpcSpawnItem(roles[i], new Item.Settings()));
        }
        return eggs;
    }

    private static Item armor(String name, net.minecraft.registry.entry.RegistryEntry<net.minecraft.item.ArmorMaterial> mat,
                              ArmorItem.Type type, int durability) {
        return reg(name, new ArmorItem(mat, type, new Item.Settings().maxDamage(type.getMaxDamage(durability))));
    }

    private static Item reg(String name, Item item) {
        return Registry.register(Registries.ITEM, VerdenMod.id(name), item);
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, VerdenMod.id("main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(GRIMORIO))
                        .displayName(Text.translatable("itemGroup.verden"))
                        .entries((ctx, entries) -> {
                            entries.add(GRIMORIO);
                            entries.add(CROMUS);
                            entries.add(BERSERKER);
                            entries.add(REVOLVER_CUBUS);
                            entries.add(MUNICAO_45);
                            entries.add(CORNETA_LUCY);
                            entries.add(MNEUMONIO);
                            entries.add(TRIQUETRA);
                            entries.add(COROA_KUNORI);
                            entries.add(MACHADO_HIRAKU);
                            entries.add(ESPADA_LIAM);
                            entries.add(ESPADA_LEGIAO);
                            entries.add(ADAGA_ZYTRA);
                            entries.add(ESPADA_HAIGOL);
                            entries.add(INSIGNIA_LEGIAO);
                            entries.add(POCAO_RECUPERACAO);
                            entries.add(MONTER);
                            entries.add(COPTA);
                            entries.add(GARRA_LICANTROPO);
                            entries.add(BUSSOLA);
                            entries.add(LANTERNA_FAUNO);
                            entries.add(MOCHILA_COURO);
                            entries.add(RACAO_VIAGEM);
                            entries.add(POCAO_VISAO_NOTURNA);
                            entries.add(ELIXIR_CALOR);
                            entries.add(LANCA_GELO);
                            entries.add(KATANA_CHAMAS);
                            entries.add(MUNICAO_INCENDIARIA);
                            entries.add(MUNICAO_PERFURANTE);
                            entries.add(LEGIAO_CAPACETE);
                            entries.add(LEGIAO_PEITORAL);
                            entries.add(LEGIAO_CALCAS);
                            entries.add(LEGIAO_BOTAS);
                            entries.add(RINOY_CAPUZ);
                            entries.add(RINOY_CASACO);
                            entries.add(RINOY_CALCAS);
                            entries.add(RINOY_BOTAS);
                            for (Item egg : NPC_EGGS) {
                                entries.add(egg);
                            }
                            entries.add(OVO_LICANTROPO_ADAPTADO);
                            entries.add(OVO_SOLDADO);
                            entries.add(OVO_ZYTRA);
                            entries.add(OVO_HAIGOL);
                            entries.add(OVO_CONDOLYUS);
                            entries.add(OVO_LICANTROPO);
                            entries.add(OVO_KUNORI);
                            entries.add(OVO_TENTACULO);
                            entries.add(OVO_AMALGAMA);
                            entries.add(OVO_RINOY);
                        })
                        .build());
    }
}
