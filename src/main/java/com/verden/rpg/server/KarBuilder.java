package com.verden.rpg.server;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Random;

/**
 * Karamarraw REAL. A ilha (≈4,8 km × 1,2 km, com uma serra colada à costa norte) já existe no terreno do mod (mapa de Verden);
 * aqui NÃO se constrói terreno. Só se acrescenta, sobre o que existe: navio com prancha, suprimentos na praia, marcos na trilha,
 * mata densa ao longo da trilha e pedras de esconderijo. Todo bloco colocado exige ar/água no destino e chão firme embaixo,
 * então o terreno original nunca é substituído.
 *
 * Coordenadas locais (x leste, z para o INTERIOR/sul), origem na primeira coluna de terra da costa norte em x = {@link #LX}.
 */
final class KarBuilder {
    private KarBuilder() {}

    static final int LX = 3000;
    static int SEA = 63;
    /** z mundial da linha da costa (local z = 0). */
    static int cz = Integer.MIN_VALUE;
    /** Sala do relatório: caixa fechada no céu (acima de qualquer terreno), nunca toca o terreno. */
    static final int RX = LX, RY = 250, RZ = 1000;

    static final int[][] TRAIL = {{0, 6}, {2, 45}, {14, 85}, {-4, 118}, {-18, 146}};
    static final int[] NARBER_HOME = {-30, 172};
    static final int[][] NARBER_PATH = {{-48, 196}, {-62, 226}, {-70, 262}, {-60, 300}};

    static int wx(double x) { return (int) Math.floor(LX + x); }

    static int wz(double z) { return (int) Math.floor(cz + z); }

    /** Descobre a costa norte percorrendo o terreno real. Resultado em cache. */
    static void locate(ServerWorld w) {
        if (cz != Integer.MIN_VALUE) return;
        SEA = w.getSeaLevel();
        for (int z = 1250; z < 1800; z++) {
            w.getChunk(LX >> 4, z >> 4);
            if (w.getTopY(Heightmap.Type.OCEAN_FLOOR, LX, z) > SEA) { cz = z; return; }
        }
        cz = 1500; // fallback (não deveria ocorrer: o mapa tem terra aqui)
    }

    static int top(ServerWorld w, double lx, double lz) {
        int x = wx(lx), z = wz(lz);
        w.getChunk(x >> 4, z >> 4);
        return w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    /** Coloca só onde há ar/água (nunca sobrescreve terreno ou construções existentes). */
    private static boolean put(ServerWorld w, int x, int y, int z, BlockState s) {
        BlockPos p = new BlockPos(x, y, z);
        BlockState cur = w.getBlockState(p);
        if (!(cur.isAir() || cur.isOf(Blocks.WATER) || cur.isIn(BlockTags.REPLACEABLE))) return false;
        w.setBlockState(p, s, 18);
        return true;
    }

    private static boolean nearTrail(int x, int z, double rad) {
        for (int i = 0; i + 1 < TRAIL.length; i++)
            for (int t = 0; t <= 12; t++) {
                double px = TRAIL[i][0] + (TRAIL[i + 1][0] - TRAIL[i][0]) * t / 12.0, pz = TRAIL[i][1] + (TRAIL[i + 1][1] - TRAIL[i][1]) * t / 12.0;
                if (Math.hypot(x - px, z - pz) <= rad) return true;
            }
        return Math.hypot(x - 0, z - 0) <= rad;
    }

    static void buildAll(ServerWorld w) {
        locate(w);
        buildShip(w);
        buildBeach(w);
        buildGrove(w);
    }

    /** Navio ancorado ao norte da praia (proa ao norte); convés em y = mar (pés em mar+1), prancha até a areia. */
    static void buildShip(ServerWorld w) {
        locate(w);
        BlockState plank = Blocks.SPRUCE_PLANKS.getDefaultState(), dark = Blocks.DARK_OAK_PLANKS.getDefaultState(), wool = Blocks.WHITE_WOOL.getDefaultState(), mast = Blocks.SPRUCE_LOG.getDefaultState();
        for (int z = -34; z <= -14; z++) {
            int half = z < -29 ? 1 + (z + 34) / 2 : 3;
            for (int x = -half; x <= half; x++) {
                put(w, wx(x), SEA - 2, wz(z), dark);
                put(w, wx(x), SEA - 1, wz(z), dark);
                put(w, wx(x), SEA, wz(z), plank);
            }
            put(w, wx(-half), SEA + 1, wz(z), dark);
            put(w, wx(half), SEA + 1, wz(z), dark);
            if (z % 4 == 0) { put(w, wx(-half), SEA + 2, wz(z), Blocks.OAK_FENCE.getDefaultState()); put(w, wx(half), SEA + 2, wz(z), Blocks.OAK_FENCE.getDefaultState()); }
        }
        for (int[] m : new int[][]{{0, -27}, {0, -21}}) {
            for (int y = 1; y <= 15; y++) put(w, wx(m[0]), SEA + y, wz(m[1]), mast);
            for (int y = 5; y <= 13; y++) for (int x = -2; x <= 2; x++) put(w, wx(x), SEA + y, wz(m[1]) + 1, wool);
        }
        put(w, wx(0), SEA + 4, wz(-16), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, false));
        for (int z = -13; z <= 0; z++) for (int x = 0; x <= 1; x++) put(w, wx(x), SEA, wz(z), plank); // prancha até a areia
    }

    /** Suprimentos da expedição na praia: barris e baús (as atividades dos NPCs giram em torno deles). */
    static void buildBeach(ServerWorld w) {
        locate(w);
        int[][] crates = {{-6, 5}, {-5, 5}, {-6, 6}, {5, 6}, {6, 6}, {5, 7}, {-9, 9}, {9, 10}};
        for (int[] c : crates) {
            int y = top(w, c[0], c[1]);
            put(w, wx(c[0]), y, wz(c[1]), (c[0] + c[1]) % 2 == 0 ? Blocks.BARREL.getDefaultState() : Blocks.CHEST.getDefaultState());
        }
    }

    /** Mata densa ao longo da trilha (árvores só sobre terra/areia, nunca na trilha), marcos de pedra com tocha e pedras de esconderijo. */
    static void buildGrove(ServerWorld w) {
        locate(w);
        Random rnd = new Random(77);
        BlockState log = Blocks.JUNGLE_LOG.getDefaultState(), leaf = Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
        for (int x = -46; x <= 46; x++)
            for (int z = 28; z <= 175; z++) {
                if (nearTrail(x, z, 5) || Math.hypot(x - NARBER_HOME[0], z - NARBER_HOME[1]) < 6) continue;
                double dens = Math.abs(x - trailX(z)) < 22 ? 0.07 : 0.035;
                if (rnd.nextDouble() >= dens) continue;
                int g = top(w, x, z);
                BlockState below = w.getBlockState(new BlockPos(wx(x), g - 1, wz(z)));
                if (!(below.isIn(BlockTags.DIRT) || below.isOf(Blocks.SAND) || below.isOf(Blocks.STONE) || below.isOf(Blocks.GRASS_BLOCK))) continue;
                if (!w.getBlockState(new BlockPos(wx(x), g, wz(z))).isAir()) continue;
                int th = 6 + rnd.nextInt(6);
                for (int y = 0; y < th; y++) put(w, wx(x), g + y, wz(z), log);
                for (int dy = th - 3; dy <= th + 1; dy++) {
                    int rad = dy >= th ? 1 : 2;
                    for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                        if ((Math.abs(dx) + Math.abs(dz) < rad * 2 || rnd.nextBoolean()) && !(dx == 0 && dz == 0 && dy < th)) put(w, wx(x) + dx, g + dy, wz(z) + dz, leaf);
                }
            }
        // marcos da trilha: coluna de pedra com tocha (orientam o jogador)
        for (int[] t : TRAIL) {
            int g = top(w, t[0] + 2, t[1]);
            put(w, wx(t[0] + 2), g, wz(t[1]), Blocks.COBBLESTONE.getDefaultState());
            put(w, wx(t[0] + 2), g + 1, wz(t[1]), Blocks.COBBLESTONE.getDefaultState());
            put(w, wx(t[0] + 2), g + 2, wz(t[1]), Blocks.TORCH.getDefaultState());
        }
        // pedras de esconderijo perto do ponto de observação e da clareira
        for (int[] b : new int[][]{{-12, 112}, {6, 122}, {-24, 138}, {-8, 146}, {-36, 156}, {-20, 128}}) {
            int g = top(w, b[0], b[1]);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = 0; dy < (dx == 0 && dz == 0 ? 3 : 2); dy++) put(w, wx(b[0]) + dx, g + dy, wz(b[1]) + dz, Blocks.MOSSY_COBBLESTONE.getDefaultState());
        }
    }

    static double trailX(int z) {
        for (int i = 0; i + 1 < TRAIL.length; i++) if (z >= TRAIL[i][1] && z <= TRAIL[i + 1][1]) return TRAIL[i][0] + (TRAIL[i + 1][0] - TRAIL[i][0]) * (z - TRAIL[i][1]) / (double) (TRAIL[i + 1][1] - TRAIL[i][1]);
        return z < TRAIL[0][1] ? TRAIL[0][0] : TRAIL[TRAIL.length - 1][0];
    }

    /** Sala fechada (19x19x9) com tapete, lanternas e pilares: ambiente escuro e solene para o relatório. */
    static void buildRoom(ServerWorld w) {
        for (int cx = (RX - 12) >> 4; cx <= (RX + 12) >> 4; cx++) for (int c2 = (RZ - 12) >> 4; c2 <= (RZ + 12) >> 4; c2++) w.getChunk(cx, c2);
        BlockState wall = Blocks.DEEPSLATE_BRICKS.getDefaultState(), floor = Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), air = Blocks.AIR.getDefaultState();
        for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) for (int y = 0; y <= 9; y++) {
            boolean edge = Math.abs(x) == 9 || Math.abs(z) == 9 || y == 0 || y == 9;
            w.setBlockState(new BlockPos(RX + x, RY + y, RZ + z), y == 0 ? floor : edge ? wall : air, 18);
        }
        for (int z = -7; z <= 7; z++) for (int x = -1; x <= 1; x++) w.setBlockState(new BlockPos(RX + x, RY + 1, RZ + z), Blocks.RED_CARPET.getDefaultState(), 18);
        for (int px : new int[]{-6, 6}) for (int pz : new int[]{-6, -2, 2, 6}) for (int y = 1; y <= 8; y++) w.setBlockState(new BlockPos(RX + px, RY + y, RZ + pz), Blocks.DEEPSLATE_TILES.getDefaultState(), 18);
        BlockState lan = Blocks.SOUL_LANTERN.getDefaultState().with(LanternBlock.HANGING, true);
        for (int[] l : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, -7}, {0, 7}, {0, 0}}) w.setBlockState(new BlockPos(RX + l[0], RY + 8, RZ + l[1]), lan, 18);
        w.setBlockState(new BlockPos(RX, RY + 1, RZ + 9), air, 18);
        w.setBlockState(new BlockPos(RX, RY + 2, RZ + 9), air, 18);
    }
}
