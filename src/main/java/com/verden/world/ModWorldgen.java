package com.verden.world;

import com.verden.VerdenMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;

/** Geração de mundo: acampamentos da Legião das Espadas (placed feature em data/verden/worldgen). */
public final class ModWorldgen {
    private ModWorldgen() {}

    public static final LegionCampFeature LEGION_CAMP = Registry.register(Registries.FEATURE,
            VerdenMod.id("acampamento_legiao"), new LegionCampFeature(DefaultFeatureConfig.CODEC));

    public static void register() {
        Registry.register(Registries.CHUNK_GENERATOR, VerdenMod.id("verden"), com.verden.world.gen.VerdenChunkGenerator.CODEC);
        Registry.register(Registries.BIOME_SOURCE, VerdenMod.id("verden"), com.verden.world.gen.VerdenBiomeSource.CODEC);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.SURFACE_STRUCTURES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, VerdenMod.id("acampamento_legiao")));
    }
}
