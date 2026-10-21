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

    /** Cache por thread: chave (x,z) -> região. O bioma não depende de y, mas o jogo pergunta por cada seção vertical. */
    private static final class Memo {
        final long[] keys = new long[512];
        final byte[] vals = new byte[512];
        final boolean[] used = new boolean[512];
    }

    private static final ThreadLocal<Memo> MEMO = ThreadLocal.withInitial(Memo::new);

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        int slot = (int) ((key ^ (key >>> 21) ^ (key >>> 41)) & 511);
        Memo m = MEMO.get();
        int ord;
        if (m.used[slot] && m.keys[slot] == key) {
            ord = m.vals[slot];
        } else {
            ord = terrain.biomeAt(x * 4 + 2, z * 4 + 2).ordinal();
            m.keys[slot] = key;
            m.vals[slot] = (byte) ord;
            m.used[slot] = true;
        }
        return entries.get(Math.min(ord, entries.size() - 1));
    }
}
