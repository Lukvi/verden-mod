package com.verden.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.verden.world.build.SiteBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Gerador de chunks do mundo "Verden": terreno vindo do mapa do livro (1 pixel = 8 blocos). */
public class VerdenChunkGenerator extends ChunkGenerator {
    public static final MapCodec<VerdenChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(VerdenChunkGenerator::new)));

    private static final int MIN_Y = Terrain.MIN_Y;
    private static final BlockState[] BADLANDS_BANDS = {
            Blocks.TERRACOTTA.getDefaultState(), Blocks.ORANGE_TERRACOTTA.getDefaultState(), Blocks.YELLOW_TERRACOTTA.getDefaultState(),
            Blocks.TERRACOTTA.getDefaultState(), Blocks.BROWN_TERRACOTTA.getDefaultState(), Blocks.WHITE_TERRACOTTA.getDefaultState(),
            Blocks.TERRACOTTA.getDefaultState(), Blocks.RED_TERRACOTTA.getDefaultState(), Blocks.ORANGE_TERRACOTTA.getDefaultState(),
            Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState()};

    private final Terrain terrain = new Terrain();
    private final FastNoise caves = new FastNoise(0xca7e1L);
    private final FastNoise caves2 = new FastNoise(0xca7e2L);
    private final FastNoise surfaceNoise = new FastNoise(0x5af3L);

    public VerdenChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public Terrain terrain() {
        return terrain;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    /** Sem estruturas vanilla (vilas, saques etc.): Verden tem as suas próprias construções. */
    @Override
    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry,
                                                                           NoiseConfig noiseConfig, long seed) {
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, Stream.<RegistryEntry<StructureSet>>empty());
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess,
                      StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        ChunkPos chunkPos = region.getCenterPos();
        RegistryEntry<Biome> biome = region.getBiome(chunkPos.getStartPos().withY(region.getTopY() - 1));
        ChunkRandom random = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        random.setPopulationSeed(region.getSeed(), chunkPos.getStartX(), chunkPos.getStartZ());
        SpawnHelper.populateEntities(region, biome, chunkPos, random);
    }

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return Terrain.SEA;
    }

    @Override
    public int getMinimumY() {
        return MIN_Y;
    }

    @Override
    public int getSpawnHeight(HeightLimitView world) {
        return 72;
    }

    // ------------------------------------------------------------------ geração do terreno
    @Override
    public CompletableFuture<Chunk> fillFromNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        int startX = pos.getStartX(), startZ = pos.getStartZ();
        Heightmap oceanFloor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkSection[] sections = chunk.getSectionArray();
        BlockState stone = Blocks.STONE.getDefaultState();
        BlockState deepslate = Blocks.DEEPSLATE.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState lava = Blocks.LAVA.getDefaultState();
        BlockState bedrock = Blocks.BEDROCK.getDefaultState();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int bx = startX + lx, bz = startZ + lz;
                Terrain.Column c = terrain.column(bx, bz);
                BlockState[] surf = surfaceBlocks(c, bx, bz);
                int top = c.solid;
                int fill = surf.length - 1;      // surf[0] = topo, 1..n = camadas abaixo
                for (int y = MIN_Y; y <= top; y++) {
                    BlockState s;
                    int depth = top - y;
                    if (y == MIN_Y || (y < MIN_Y + 5 && hash(bx, y, bz) % 5 > (y - MIN_Y))) {
                        s = bedrock;
                    } else if (depth <= fill) {
                        s = depth == 0 && c.surface == Region.FROZEN_PEAKS ? peakTop(y, bx, bz) : surf[depth];
                    } else {
                        s = y < (hash(bx, y, bz) & 7) - 8 ? deepslate : stone;
                        if (y > MIN_Y + 4 && depth >= 7 && isCave(bx, y, bz, top, c)) {
                            s = y < MIN_Y + 10 ? lava : air;
                        }
                    }
                    if (s != air) {
                        sections[chunk.getSectionIndex(y)].setBlockState(lx, y & 15, lz, s, false);
                    }
                }
                oceanFloor.trackUpdate(lx, top, lz, stone);
                worldSurface.trackUpdate(lx, top, lz, stone);
                if (c.water > top) {
                    for (int y = top + 1; y <= c.water; y++) {
                        sections[chunk.getSectionIndex(y)].setBlockState(lx, y & 15, lz, water, false);
                    }
                    worldSurface.trackUpdate(lx, c.water, lz, water);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private int hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }

    private boolean isCave(int x, int y, int z, int top, Terrain.Column c) {
        if (y > top - 7) {
            return false;
        }
        double n1 = caves.noise(x * 0.016, y * 0.026, z * 0.016);
        double n2 = caves2.noise(x * 0.016 + 77, y * 0.026, z * 0.016 - 31);
        if (Math.abs(n1) < 0.05 && Math.abs(n2) < 0.05) {
            return true;
        }
        return y < 36 && caves.noise(x * 0.009 + 50, y * 0.015, z * 0.009 - 50) > 0.6;
    }

    private BlockState peakTop(int y, int x, int z) {
        double n = surfaceNoise.noise(x * 0.05, z * 0.05);
        if (y > 175 + n * 10) {
            return n > 0.15 ? Blocks.PACKED_ICE.getDefaultState() : Blocks.SNOW_BLOCK.getDefaultState();
        }
        if (y > 125 + n * 12) {
            return Blocks.SNOW_BLOCK.getDefaultState();
        }
        return n > 0.2 ? Blocks.GRAVEL.getDefaultState() : Blocks.STONE.getDefaultState();
    }

    /** Camadas de superfície: [0] topo, [1..] abaixo. */
    private BlockState[] surfaceBlocks(Terrain.Column c, int x, int z) {
        BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        BlockState dirt = Blocks.DIRT.getDefaultState();
        BlockState sand = Blocks.SAND.getDefaultState();
        BlockState gravel = Blocks.GRAVEL.getDefaultState();
        BlockState sandstone = Blocks.SANDSTONE.getDefaultState();
        BlockState stone = Blocks.STONE.getDefaultState();
        boolean underwater = c.water > c.solid;
        Region r = c.surface;
        if (underwater) {
            double n = surfaceNoise.noise(x * 0.04, z * 0.04);
            BlockState bed = r.isCold() || r == Region.COLD_OCEAN || r == Region.DEEP_COLD_OCEAN ? (n > 0.0 ? gravel : sand)
                    : (n > 0.45 ? Blocks.CLAY.getDefaultState() : sand);
            if (c.solid < Terrain.SEA - 24 && n > 0.1) {
                bed = gravel;
            }
            if (r == Region.RIVER || r == Region.FROZEN_RIVER) {
                bed = n > 0.2 ? Blocks.CLAY.getDefaultState() : (n < -0.3 ? gravel : sand);
            }
            return new BlockState[]{bed, bed, bed, sandstone.equals(bed) ? bed : dirt};
        }
        return switch (r) {
            case BEACH, SNOWY_BEACH -> new BlockState[]{sand, sand, sand, sand, sandstone};
            case DESERT -> new BlockState[]{sand, sand, sand, sand, sandstone, sandstone, sandstone};
            case BADLANDS -> {
                BlockState red = Blocks.RED_SAND.getDefaultState();
                BlockState[] a = new BlockState[9];
                a[0] = red;
                for (int i = 1; i < a.length; i++) {
                    a[i] = BADLANDS_BANDS[(c.solid - i + 400) % BADLANDS_BANDS.length];
                }
                yield a;
            }
            case SWAMP -> {
                double n = surfaceNoise.noise(x * 0.06, z * 0.06);
                yield new BlockState[]{n > 0.3 ? Blocks.MUD.getDefaultState() : grass, dirt, dirt, dirt};
            }
            case STONY_SHORE, STONY_PEAKS -> new BlockState[]{stone, stone, stone, stone};
            case FROZEN_PEAKS -> new BlockState[]{stone, stone, stone};
            case TAIGA, SNOWY_TAIGA, DARK_FOREST -> {
                double n = surfaceNoise.noise(x * 0.07, z * 0.07);
                yield new BlockState[]{n > 0.35 ? Blocks.PODZOL.getDefaultState() : grass, dirt, dirt, dirt};
            }
            case SAVANNA -> new BlockState[]{surfaceNoise.noise(x * 0.08, z * 0.08) > 0.4 ? Blocks.COARSE_DIRT.getDefaultState() : grass, dirt, dirt, dirt};
            case RIVER, FROZEN_RIVER -> new BlockState[]{sand, sand, sand};
            default -> new BlockState[]{grass, dirt, dirt, dirt};
        };
    }

    // ------------------------------------------------------------------ alturas
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        Terrain.Column c = terrain.column(x, z);
        if (heightmap == Heightmap.Type.OCEAN_FLOOR || heightmap == Heightmap.Type.OCEAN_FLOOR_WG) {
            return c.solid + 1;
        }
        return Math.max(c.solid, c.water) + 1;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        Terrain.Column c = terrain.column(x, z);
        BlockState[] states = new BlockState[world.getHeight()];
        int bottom = world.getBottomY();
        for (int i = 0; i < states.length; i++) {
            int y = bottom + i;
            states[i] = y <= c.solid ? Blocks.STONE.getDefaultState() : (y <= c.water ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState());
        }
        return new VerticalBlockSample(bottom, states);
    }

    @Override
    public void appendDebugHoverText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        Terrain.Column c = terrain.column(pos.getX(), pos.getZ());
        text.add("Verden: " + c.biome + " solid=" + c.solid + " agua=" + c.water);
    }

    // ------------------------------------------------------------------ construções (cidades etc.)
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        super.generateFeatures(world, chunk, structureAccessor);
        SiteBuilder.placeInChunk(world, chunk, this);
    }
}
