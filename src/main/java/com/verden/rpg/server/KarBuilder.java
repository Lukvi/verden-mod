package com.verden.rpg.server;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Random;

/**
 * Constrói a ilha de Karamarraw, o navio e a sala do relatório. Coordenadas locais da ilha: x leste, z sul; y em
 * relação ao nível do mar (63). A ilha é levantada do FUNDO do oceano e só toca em água/ar: antes de construir,
 * {@link #oceanoSeguro} confirma que a área é oceano profundo (foi escolhida pelo mapa do mod) e aborta se não for,
 * para nunca alterar terra firme original.
 */
final class KarBuilder {
    private KarBuilder() {}

    static final int KX = -288, KZ = -616, SEA = 63;
    /** Sala do relatório: caixa fechada no céu, sobre as mesmas coordenadas (y alto = nunca colide com terreno). */
    static final int RX = KX, RY = 250, RZ = KZ;
    static final int[][] PATH = {{0, 31}, {0, 14}, {0, -2}, {-6, -10}, {-10, -16}};

    static double sq(double v) { return v * v; }

    /** Altura da ilha acima do mar (≤0 = água). Praia ao sul, mata no centro, montanhas ao norte. */
    static int ih(int x, int z) {
        double r = Math.hypot(x, z * 0.92);
        double h = 9 - r * 0.25 + 26 * Math.exp(-(sq(x / 10.0) + sq((z + 28) / 9.0))) + 3 * Math.exp(-(sq((x + 14) / 7.0) + sq((z + 8) / 7.0)));
        return (int) Math.round(h);
    }

    static boolean nearPath(int x, int z, int rad) {
        for (int i = 0; i + 1 < PATH.length; i++) {
            int x1 = PATH[i][0], z1 = PATH[i][1], x2 = PATH[i + 1][0], z2 = PATH[i + 1][1];
            for (int t = 0; t <= 10; t++) {
                double px = x1 + (x2 - x1) * t / 10.0, pz = z1 + (z2 - z1) * t / 10.0;
                if (Math.hypot(x - px, z - pz) <= rad) return true;
            }
        }
        return false;
    }

    /** Oceano profundo em volta e no centro? (fundo bem abaixo do nível do mar nos pontos amostrados). */
    static boolean oceanoSeguro(ServerWorld w) {
        int[][] pts = {{0, 0}, {45, 0}, {-45, 0}, {0, 45}, {0, -45}, {32, 32}, {-32, 32}, {32, -32}, {-32, -32}, {0, 60}, {0, 100}};
        for (int[] p : pts) {
            w.getChunk((KX + p[0]) >> 4, (KZ + p[1]) >> 4);
            int floor = w.getTopY(Heightmap.Type.OCEAN_FLOOR, KX + p[0], KZ + p[1]);
            if (floor > SEA - 10 || !w.getBlockState(new BlockPos(KX + p[0], SEA - 1, KZ + p[1])).isOf(Blocks.WATER)) return false;
        }
        return true;
    }

    private static void set(ServerWorld w, int x, int y, int z, BlockState s) {
        w.setBlockState(new BlockPos(x, y, z), s, 18);
    }

    static void buildIsland(ServerWorld w) {
        Random rnd = new Random(1234);
        for (int cx = (KX - 44) >> 4; cx <= (KX + 44) >> 4; cx++) for (int cz = (KZ - 44) >> 4; cz <= (KZ + 70) >> 4; cz++) w.getChunk(cx, cz);
        BlockState sand = Blocks.SAND.getDefaultState(), stone = Blocks.STONE.getDefaultState(), dirt = Blocks.DIRT.getDefaultState(),
                grass = Blocks.GRASS_BLOCK.getDefaultState(), moss = Blocks.MOSSY_COBBLESTONE.getDefaultState(), path = Blocks.COARSE_DIRT.getDefaultState();
        for (int x = -40; x <= 40; x++)
            for (int z = -42; z <= 42; z++) {
                int hgt = ih(x, z);
                if (hgt < 1) continue;
                int ax = KX + x, az = KZ + z, top = SEA + hgt;
                int floor = w.getTopY(Heightmap.Type.OCEAN_FLOOR, ax, az) - 1;
                for (int y = floor; y <= top; y++) {
                    BlockState s;
                    if (y == top) s = hgt <= 2 ? sand : hgt >= 24 ? (rnd.nextInt(3) == 0 ? moss : stone) : (nearPath(x, z, 1) ? path : grass);
                    else if (y >= top - 3) s = hgt <= 2 ? sand : hgt >= 24 ? stone : dirt;
                    else s = stone;
                    set(w, ax, y, az, s);
                }
            }
        // mata: árvores (não crescem no caminho nem na praia) e samambaias
        BlockState log = Blocks.JUNGLE_LOG.getDefaultState(), leaf = Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
        for (int x = -34; x <= 34; x++)
            for (int z = -34; z <= 24; z++) {
                int hgt = ih(x, z);
                if (hgt < 3 || hgt > 19 || nearPath(x, z, 2)) continue;
                int ax = KX + x, az = KZ + z, ground = SEA + hgt;
                double dens = Math.abs(x) < 16 && z < 18 ? 0.075 : 0.035;
                if (rnd.nextDouble() < dens) {
                    int th = 5 + rnd.nextInt(5);
                    for (int y = 1; y <= th; y++) set(w, ax, ground + y, az, log);
                    for (int dy = th - 2; dy <= th + 1; dy++) {
                        int rad = dy >= th ? 1 : 2;
                        for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                            if ((Math.abs(dx) + Math.abs(dz) < rad * 2 || rnd.nextBoolean()) && !(dx == 0 && dz == 0 && dy <= th)) set(w, ax + dx, ground + dy, az + dz, leaf);
                    }
                } else if (rnd.nextDouble() < 0.12) set(w, ax, ground + 1, az, Blocks.FERN.getDefaultState());
            }
        // pedras grandes: esconderijos na área do incidente (z ≈ -4 a -22)
        for (int[] b : new int[][]{{-3, -7}, {4, -7}, {-5, -14}, {-14, -12}, {-9, -21}, {2, -15}}) {
            int bx = KX + b[0], bz = KZ + b[1], gy = SEA + ih(b[0], b[1]);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = 1; dy <= (dx == 0 && dz == 0 ? 3 : 2); dy++) set(w, bx + dx, gy + dy, bz + dz, moss);
        }
    }

    /** Navio ancorado ao sul da praia, proa para o norte. Convés em y = mar+1; prancha até a areia. */
    static void buildShip(ServerWorld w) {
        BlockState plank = Blocks.SPRUCE_PLANKS.getDefaultState(), dark = Blocks.DARK_OAK_PLANKS.getDefaultState(), air = Blocks.AIR.getDefaultState(),
                wool = Blocks.WHITE_WOOL.getDefaultState(), mast = Blocks.SPRUCE_LOG.getDefaultState();
        for (int z = 36; z <= 56; z++) {
            int half = z < 40 ? 1 + (z - 36) / 2 : 3;
            for (int x = -half; x <= half; x++) {
                set(w, KX + x, SEA - 1, KZ + z, dark);
                set(w, KX + x, SEA, KZ + z, dark);
                set(w, KX + x, SEA + 1, KZ + z, plank);
                set(w, KX + x, SEA + 2, KZ + z, air);
                set(w, KX + x, SEA + 3, KZ + z, air);
            }
            set(w, KX - half, SEA + 2, KZ + z, dark); set(w, KX + half, SEA + 2, KZ + z, dark);
            if (z % 4 == 0) { set(w, KX - half, SEA + 3, KZ + z, Blocks.OAK_FENCE.getDefaultState()); set(w, KX + half, SEA + 3, KZ + z, Blocks.OAK_FENCE.getDefaultState()); }
        }
        for (int[] m : new int[][]{{0, 43}, {0, 50}}) {
            for (int y = 2; y <= 16; y++) set(w, KX + m[0], SEA + y, KZ + m[1], mast);
            for (int y = 6; y <= 14; y++) for (int x = -2; x <= 2; x++) set(w, KX + x, SEA + y, KZ + m[1] + 1, wool);
        }
        for (int x = -3; x <= 3; x++) for (int y = 2; y <= 4; y++) for (int z = 54; z <= 56; z++) set(w, KX + x, SEA + y, KZ + z, y == 4 ? plank : (x == -3 || x == 3 || z == 56 ? dark : air)); // castelo de popa
        set(w, KX, SEA + 3, KZ + 54, Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        for (int z = 33; z <= 36; z++) for (int x = -1; x <= 0; x++) set(w, KX + x, SEA + 1, KZ + z, plank); // prancha
    }

    /** Sala fechada (19x19x9) com tapete, lanternas e pilares: ambiente escuro e solene para o relatório. */
    static void buildRoom(ServerWorld w) {
        for (int cx = (RX - 12) >> 4; cx <= (RX + 12) >> 4; cx++) for (int cz = (RZ - 12) >> 4; cz <= (RZ + 12) >> 4; cz++) w.getChunk(cx, cz);
        BlockState wall = Blocks.DEEPSLATE_BRICKS.getDefaultState(), floor = Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), air = Blocks.AIR.getDefaultState();
        for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) for (int y = 0; y <= 9; y++) {
            boolean edge = Math.abs(x) == 9 || Math.abs(z) == 9 || y == 0 || y == 9;
            set(w, RX + x, RY + y, RZ + z, y == 0 ? floor : edge ? wall : air);
        }
        for (int z = -7; z <= 7; z++) for (int x = -1; x <= 1; x++) set(w, RX + x, RY + 1, RZ + z, Blocks.RED_CARPET.getDefaultState());
        for (int px : new int[]{-6, 6}) for (int pz : new int[]{-6, -2, 2, 6}) for (int y = 1; y <= 8; y++) set(w, RX + px, RY + y, RZ + pz, Blocks.DEEPSLATE_TILES.getDefaultState());
        BlockState lan = Blocks.SOUL_LANTERN.getDefaultState().with(LanternBlock.HANGING, true);
        for (int[] l : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, -7}, {0, 7}, {0, 0}}) set(w, RX + l[0], RY + 8, RZ + l[1], lan);
        set(w, RX, RY + 1, RZ + 9, air); set(w, RX, RY + 2, RZ + 9, air); // porta (sul)
    }
}
