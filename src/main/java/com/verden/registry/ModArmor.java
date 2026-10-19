package com.verden.registry;

import com.verden.VerdenMod;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/** Materiais de armadura: Legião das Espadas e Pele de Rinoy. */
public final class ModArmor {
    private ModArmor() {}

    public static final RegistryEntry<ArmorMaterial> LEGIAO = material("legiao", 2, 6, 5, 2, 9,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0f, 0.0f, () -> Ingredient.ofItems(Items.IRON_INGOT));

    public static final RegistryEntry<ArmorMaterial> PELE_RINOY = material("pele_rinoy", 2, 5, 4, 2, 12,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0f, 0.05f, () -> Ingredient.ofItems(Items.LEATHER));

    private static RegistryEntry<ArmorMaterial> material(String name, int boots, int chest, int legs, int helmet, int ench,
                                                         RegistryEntry<SoundEvent> sound, float toughness, float kb,
                                                         Supplier<Ingredient> repair) {
        EnumMap<ArmorItem.Type, Integer> def = new EnumMap<>(ArmorItem.Type.class);
        def.put(ArmorItem.Type.BOOTS, boots);
        def.put(ArmorItem.Type.LEGGINGS, legs);
        def.put(ArmorItem.Type.CHESTPLATE, chest);
        def.put(ArmorItem.Type.HELMET, helmet);
        def.put(ArmorItem.Type.BODY, chest);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(VerdenMod.id(name)));
        return Registry.registerReference(Registries.ARMOR_MATERIAL, VerdenMod.id(name),
                new ArmorMaterial(def, ench, sound, repair, layers, toughness, kb));
    }

    public static void register() {
    }
}
