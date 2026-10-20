package com.verden.world.gen;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.stream.Stream;

/**
 * Fonte de biomas do mundo "Verden": o bioma de cada ponto vem do mapa.
 * O JSON do world_preset lista os biomas na mesma ordem de {@link Region}.
 */
public class VerdenBiomeSource extends BiomeSource {
    public static final MapCodec<VerdenBiomeSource> CODEC = Biome.REGISTRY_CODEC.listOf().fieldOf("biomes")
            .xmap(VerdenBiomeSource::new, s -> s.entries);

    private final List<RegistryEntry<Biome>> entries;
    private final Terrain terrain = new Terrain();

    public VerdenBiomeSource(List<RegistryEntry<Biome>> entries) {
        this.entries = entries;
    }

    @Override
    protected MapCodec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return entries.stream();
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        Region r = terrain.biomeAt(x * 4 + 2, z * 4 + 2);
        int i = Math.min(r.ordinal(), entries.size() - 1);
        return entries.get(i);
    }
}
