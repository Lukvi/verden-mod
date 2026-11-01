package com.verden.world.obras;

import com.verden.entity.NpcRole;
import com.verden.entity.VerdenWalkerEntity.Kind;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.Terrain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

import java.util.Random;

import static com.verden.world.obras.Blueprint.Mob;

/** Lugares acrescentados pelo Guia de Cartografia (arena, presídio, vila militar, pilares) e pontos de interesse de estrada. */
public final class Novos {
    private Novos() {}

    private static BlockState d(Block b) {
        return Bk.d(b);
    }

    private static void walker(Blueprint b, int x, int y, int z, Kind k) {
        b.spawn(x, y, z, Mob.WALKER, k.ordinal());
    }

    // ------------------------------------------------------------------ Arena Chronyth
    public static void arena(Blueprint b, Site s, Random r) {
        int cx = s.x(), cz = s.z(), g = s.level();
        Lay.clearDisc(b, cx, cz, 38, g, 40);
        b.disc(cx, g - 2, cz, 37, d(Blocks.SANDSTONE));
        b.disc(cx, g - 1, cz, 37, d(Blocks.SANDSTONE));
        b.disc(cx, g, cz, 36, d(Blocks.SMOOTH_SANDSTONE));
        b.disc(cx, g, cz, 16, d(Blocks.SAND));
        // arquibancada em degraus (anéis cada vez mais altos)
        for (int rr = 17; rr <= 33; rr++) {
            int h = 1 + (rr - 17) / 2;
            b.ring(cx, g + 1, g + h, cz, rr, d(rr % 2 == 0 ? Blocks.CUT_SANDSTONE : Blocks.SMOOTH_SANDSTONE));
        }
        // mureta do fosso e grade
        b.ring(cx, g + 1, g + 4, cz, 16, d(Blocks.STONE_BRICKS));
        b.ring(cx, g + 5, g + 5, cz, 16, d(Blocks.IRON_BARS));
        // muralha externa com colunas e estandartes
        for (int rr = 34; rr <= 35; rr++) {
            b.ring(cx, g + 1, g + 12, cz, rr, d(Blocks.STONE_BRICKS));
        }
        b.ring(cx, g + 13, g + 13, cz, 34, d(Blocks.CUT_SANDSTONE_SLAB));
        b.ring(cx, g + 13, g + 13, cz, 35, d(Blocks.CUT_SANDSTONE_SLAB));
        for (int i = 0; i < 24; i++) {
            double a = 2 * Math.PI * i / 24;
            int x = cx + (int) Math.round(Math.cos(a) * 36), z = cz + (int) Math.round(Math.sin(a) * 36);
            b.fill(x, g + 1, z, x, g + 15, z, Bk.pillar(Blocks.QUARTZ_PILLAR, Direction.Axis.Y));
            b.set(x, g + 16, z, d(Blocks.LANTERN));
        }
        // quatro portões em túnel
        for (int sd = -1; sd <= 1; sd += 2) {
            b.clear(cx - 2, g + 1, cz + sd * 17, cx + 2, g + 5, cz + sd * 37);
            b.fill(cx - 3, g + 6, cz + sd * 34, cx + 3, g + 6, cz + sd * 36, d(Blocks.CHISELED_SANDSTONE));
            b.clear(cx + sd * 17, g + 1, cz - 2, cx + sd * 37, g + 5, cz + 2);
            b.fill(cx + sd * 34, g + 6, cz - 3, cx + sd * 36, g + 6, cz + 3, d(Blocks.CHISELED_SANDSTONE));
            b.set(cx + 3, g + 1, cz + sd * 30, d(Blocks.TORCH));
        }
        // centro: pedestal e alvos de treino
        b.fill(cx - 1, g + 1, cz - 1, cx + 1, g + 1, cz + 1, d(Blocks.SMOOTH_STONE));
        b.set(cx, g + 2, cz, d(Blocks.CHISELED_STONE_BRICKS));
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + Math.PI * i / 2;
            int x = cx + (int) Math.round(Math.cos(a) * 11), z = cz + (int) Math.round(Math.sin(a) * 11);
            b.set(x, g + 1, z, d(Blocks.HAY_BLOCK));
            b.set(x, g + 2, z, d(Blocks.TARGET));
        }
        // nicho com baú
        b.clear(cx + 3, g + 1, cz - 27, cx + 3, g + 2, cz - 27);
        b.chest(cx + 3, g + 1, cz - 27, Direction.WEST, "templo");
        b.spawn(cx, g + 3, cz + 5, Mob.NPC, NpcRole.ARMEIRO.ordinal());
        for (int i = 0; i < 4; i++) {
            double a = 2 * Math.PI * i / 4;
            walker(b, cx + (int) Math.round(Math.cos(a) * 7), g + 1, cz + (int) Math.round(Math.sin(a) * 7), Kind.MERCENARIO);
        }
        walker(b, cx, g + 1, cz - 30, Kind.GUARDA);
        walker(b, cx, g + 1, cz + 30, Kind.GUARDA);
        walker(b, cx - 30, g + 1, cz, Kind.GUARDA);
        walker(b, cx + 30, g + 1, cz, Kind.GUARDA);
    }

    // ------------------------------------------------------------------ Presídio de Motry
    public static void presidio(Blueprint b, Site s, Random r) {
        int cx = s.x(), cz = s.z(), g = s.level();
        int H = 26;
        Lay.clearDisc(b, cx, cz, 38, g, 40);
        b.fill(cx - H - 1, g, cz - H - 1, cx + H + 1, g, cz + H + 1, d(Blocks.COBBLESTONE));
        b.fill(cx - H, g, cz - H, cx + H, g, cz + H, d(Blocks.GRAVEL));
        // muralha com ameias
        b.walls(cx - H, g + 1, cz - H, cx + H, g + 9, cz + H, d(Blocks.STONE_BRICKS));
        b.walls(cx - H, g + 10, cz - H, cx + H, g + 10, cz + H, d(Blocks.STONE_BRICK_SLAB));
        for (int i = -H; i <= H; i += 2) {
            b.set(cx + i, g + 11, cz - H, d(Blocks.STONE_BRICK_WALL));
            b.set(cx + i, g + 11, cz + H, d(Blocks.STONE_BRICK_WALL));
            b.set(cx - H, g + 11, cz + i, d(Blocks.STONE_BRICK_WALL));
            b.set(cx + H, g + 11, cz + i, d(Blocks.STONE_BRICK_WALL));
        }
        // portão ao sul com grade levantada
        b.clear(cx - 2, g + 1, cz + H, cx + 2, g + 6, cz + H);
        b.fill(cx - 2, g + 5, cz + H, cx + 2, g + 6, cz + H, d(Blocks.IRON_BARS));
        b.set(cx - 3, g + 4, cz + H + 1, Bk.wallTorch(Direction.SOUTH));
        b.set(cx + 3, g + 4, cz + H + 1, Bk.wallTorch(Direction.SOUTH));
        // torres de canto
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                int tx = cx + sx * H, tz = cz + sz * H;
                Parts.roundTower(b, tx, tz, g, 4, 14, d(Blocks.STONE_BRICKS), d(Blocks.STONE_BRICKS));
                b.clear(tx, g + 1, tz - sz * 4, tx, g + 2, tz - sz * 4);
                b.set(tx, g + 15, tz, d(Blocks.CAMPFIRE));
            }
        }
        // bloco de celas (norte)
        int bx0 = cx - 12, bx1 = cx + 12, bz0 = cz - 20, bz1 = cz - 4;
        b.hollow(bx0, g, bz0, bx1, g + 7, bz1, d(Blocks.STONE_BRICKS));
        b.clear(bx0 + 1, g + 1, bz0 + 1, bx1 - 1, g + 6, bz1 - 1);
        b.fill(bx0, g + 8, bz0, bx1, g + 8, bz1, d(Blocks.STONE_BRICK_SLAB));
        b.clear(cx - 1, g + 1, bz1, cx + 1, g + 3, bz1);
        for (int i = 0; i < 5; i++) {
            int xi = bx0 + 2 + i * 5;
            if (i < 4) {
                b.fill(xi + 4, g + 1, bz0 + 1, xi + 4, g + 5, bz0 + 5, d(Blocks.STONE_BRICKS));
            }
            b.fill(xi, g + 1, bz0 + 6, xi + 3, g + 4, bz0 + 6, d(Blocks.IRON_BARS));
            b.clear(xi + 1, g + 1, bz0 + 6, xi + 1, g + 2, bz0 + 6);
            b.set(xi + 1, g + 1, bz0 + 6, Bk.door(Blocks.IRON_DOOR, Direction.SOUTH, false));
            b.set(xi + 1, g + 2, bz0 + 6, Bk.door(Blocks.IRON_DOOR, Direction.SOUTH, true));
            b.set(xi + 2, g + 1, bz0 + 2, Bk.bedPart(Blocks.GRAY_BED, Direction.SOUTH, false));
            b.set(xi + 2, g + 1, bz0 + 3, Bk.bedPart(Blocks.GRAY_BED, Direction.SOUTH, true));
            if (i % 2 == 0) {
                walker(b, xi + 1, g + 1, bz0 + 4, Kind.VIAJANTE);
            }
            b.set(xi + 1, g + 6, bz0 + 3, Bk.lantern(true));
        }
        for (int x = bx0 + 4; x <= bx1 - 4; x += 6) {
            b.set(x, g + 7, bz1 - 3, Bk.lantern(true));
        }
        b.chest(bx1 - 2, g + 1, bz1 - 2, Direction.WEST, "biblioteca");
        // sala do diretor (sudeste)
        int dx0 = cx + 6, dx1 = cx + 20, dz0 = cz + 4, dz1 = cz + 14;
        b.hollow(dx0, g, dz0, dx1, g + 6, dz1, d(Blocks.DEEPSLATE_BRICKS));
        b.clear(dx0 + 1, g + 1, dz0 + 1, dx1 - 1, g + 5, dz1 - 1);
        b.clear(dx0 + 6, g + 1, dz0, dx0 + 8, g + 2, dz0);
        b.fill(dx0 + 1, g + 1, dz1 - 1, dx1 - 1, g + 3, dz1 - 1, d(Blocks.BOOKSHELF));
        b.set(dx0 + 3, g + 1, dz0 + 3, d(Blocks.LECTERN));
        b.chest(dx1 - 2, g + 1, dz0 + 2, Direction.WEST, "castelo");
        b.set(dx0 + 2, g + 1, dz1 - 3, Bk.bedPart(Blocks.RED_BED, Direction.NORTH, true));
        b.set(dx0 + 2, g + 1, dz1 - 2, Bk.bedPart(Blocks.RED_BED, Direction.NORTH, false));
        b.set(dx0 + 10, g + 5, dz0 + 5, Bk.lantern(true));
        walker(b, dx0 + 5, g + 1, dz0 + 5, Kind.GUARDA);
        // pátio: poço, alvos e guardas
        Parts.well(b, cx - 15, cz + 12, g, Pal.norte());
        for (int i = 0; i < 6; i++) {
            walker(b, cx - 18 + i * 4, g + 1, cz + 2 + (i % 2) * 6, Kind.GUARDA);
        }
        walker(b, cx - 1, g + 1, cz + H - 2, Kind.GUARDA);
        walker(b, cx + 1, g + 1, cz + H - 2, Kind.GUARDA);
    }

    // ------------------------------------------------------------------ Grandeza: vila militar de Jørd
    private static void barracks(Blueprint b, Pal p, int x, int z, int w, int dd, int g, boolean doorSouth) {
        b.hollow(x, g, z, x + w - 1, g + 5, z + dd - 1, d(p.wall));
        b.clear(x + 1, g + 1, z + 1, x + w - 2, g + 5, z + dd - 2);
        b.fill(x, g + 1, z, x, g + 5, z, d(p.corner));
        b.fill(x + w - 1, g + 1, z, x + w - 1, g + 5, z, d(p.corner));
        b.fill(x, g + 1, z + dd - 1, x, g + 5, z + dd - 1, d(p.corner));
        b.fill(x + w - 1, g + 1, z + dd - 1, x + w - 1, g + 5, z + dd - 1, d(p.corner));
        Parts.gable(b, p, x, z, w, dd, g + 6, true);
        int doorZ = doorSouth ? z + dd - 1 : z;
        b.clear(x + w / 2, g + 1, doorZ, x + w / 2 + 1, g + 2, doorZ);
        for (int i = x + 2; i <= x + w - 3; i += 3) {
            b.set(i, g + 1, z + 1, Bk.bedPart(Blocks.RED_BED, Direction.SOUTH, false));
            b.set(i, g + 1, z + 2, Bk.bedPart(Blocks.RED_BED, Direction.SOUTH, true));
            b.set(i, g + 1, z + dd - 3, Bk.bedPart(Blocks.RED_BED, Direction.NORTH, true));
            b.set(i, g + 1, z + dd - 2, Bk.bedPart(Blocks.RED_BED, Direction.NORTH, false));
            b.set(i, g + 3, z, Bk.pane(Blocks.GLASS_PANE, true));
            b.set(i, g + 3, z + dd - 1, Bk.pane(Blocks.GLASS_PANE, true));
        }
        b.set(x + w / 2, g + 5, z + dd / 2, Bk.lantern(true));
        b.chest(x + 1, g + 1, z + dd / 2, Direction.EAST, "casa");
    }

    public static void vilaMilitar(Blueprint b, Site s, Random r) {
        int cx = s.x(), cz = s.z(), g = s.level(), R = s.radius();
        Pal p = Pal.sul();
        Lay.clearDisc(b, cx, cz, R + 2, g, 30);
        b.disc(cx, g, cz, R - 2, d(Blocks.COARSE_DIRT));
        b.disc(cx, g, cz, 14, d(Blocks.GRAVEL));
        b.fill(cx - R + 4, g, cz - 1, cx + R - 4, g, cz + 1, d(p.path));
        b.fill(cx - 1, g, cz - R + 4, cx + 1, g, cz + R - 4, d(p.path));
        // paliçada de toras com dois portões (norte e sul)
        b.ring(cx, g + 1, g + 6, cz, R - 1, d(Blocks.SPRUCE_LOG));
        b.clear(cx - 3, g + 1, cz + R - 3, cx + 3, g + 6, cz + R + 1);
        b.clear(cx - 3, g + 1, cz - R - 1, cx + 3, g + 6, cz - R + 3);
        b.fill(cx - 3, g + 6, cz + R - 2, cx + 3, g + 7, cz + R, d(Blocks.SPRUCE_LOG));
        b.fill(cx - 3, g + 6, cz - R, cx + 3, g + 7, cz - R + 2, d(Blocks.SPRUCE_LOG));
        for (int sd = -1; sd <= 1; sd += 2) {
            b.set(cx - 4, g + 4, cz + sd * (R - 1) + (sd > 0 ? 1 : -1), Bk.wallTorch(sd > 0 ? Direction.SOUTH : Direction.NORTH));
            b.set(cx + 4, g + 4, cz + sd * (R - 1) + (sd > 0 ? 1 : -1), Bk.wallTorch(sd > 0 ? Direction.SOUTH : Direction.NORTH));
        }
        // torres de vigia nos quatro cantos
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                Parts.roundTower(b, cx + sx * 35, cz + sz * 35, g, 3, 12, d(Blocks.SPRUCE_PLANKS), d(Blocks.SPRUCE_FENCE));
                b.clear(cx + sx * 35, g + 1, cz + sz * 35 - sz * 3, cx + sx * 35, g + 2, cz + sz * 35 - sz * 3);
            }
        }
        // quartéis
        barracks(b, p, cx - 40, cz - 30, 15, 8, g, true);
        barracks(b, p, cx + 25, cz - 30, 15, 8, g, true);
        barracks(b, p, cx - 40, cz + 22, 15, 8, g, false);
        barracks(b, p, cx + 25, cz + 22, 15, 8, g, false);
        // armaria, forja, refeitório e intendência voltados para a rua
        int[] c = Parts.shop(b, p, r, cx - 22, cz - 12, 10, 8, g, Direction.SOUTH, Parts.Shop.ARMEIRO);
        b.spawn(c[0], g + 1, c[1], Mob.NPC, NpcRole.ARMEIRO.ordinal());
        c = Parts.shop(b, p, r, cx + 12, cz - 12, 10, 8, g, Direction.SOUTH, Parts.Shop.FERREIRO);
        b.spawn(c[0], g + 1, c[1], Mob.NPC, NpcRole.FERREIRO.ordinal());
        c = Parts.shop(b, p, r, cx - 22, cz + 5, 10, 8, g, Direction.NORTH, Parts.Shop.TAVERNA);
        b.spawn(c[0], g + 1, c[1], Mob.NPC, NpcRole.TAVERNEIRO.ordinal());
        c = Parts.shop(b, p, r, cx + 12, cz + 5, 10, 8, g, Direction.NORTH, Parts.Shop.MERCEARIA);
        b.spawn(c[0], g + 1, c[1], Mob.NPC, NpcRole.MERCEIRO.ordinal());
        // campo de treino e mastro
        for (int i = -3; i <= 3; i += 2) {
            b.set(cx + i * 2, g + 1, cz + 9, d(Blocks.HAY_BLOCK));
            b.set(cx + i * 2, g + 2, cz + 9, d(Blocks.TARGET));
        }
        Parts.banner(b, cx, g, cz, p, 10);
        for (int k = -R + 10; k <= R - 10; k += 10) {
            if (Math.abs(k) > 12) {
                Parts.lamp(b, cx + k, g, cz + 3, p);
                Parts.lamp(b, cx + 3, g, cz + k, p);
            }
        }
        for (int i = 0; i < 8; i++) {
            double a = 2 * Math.PI * i / 8;
            walker(b, cx + (int) Math.round(Math.cos(a) * 9), g + 1, cz + (int) Math.round(Math.sin(a) * 9), i % 4 == 3 ? Kind.MERCENARIO : Kind.GUARDA);
        }
        walker(b, cx - 1, g + 1, cz + R - 4, Kind.GUARDA);
        walker(b, cx + 1, g + 1, cz - R + 4, Kind.GUARDA);
    }

    // ------------------------------------------------------------------ Pilares de pedra e corais (ao sul de Maariz)
    public static void pilares(Blueprint b, Site s, Terrain t, Random r) {
        int cx = s.x(), cz = s.z();
        Block[] stone = {Blocks.STONE, Blocks.ANDESITE, Blocks.CALCITE, Blocks.SMOOTH_BASALT};
        Block[] coral = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};
        Block[] fans = {Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN};
        int n = 11;
        for (int i = 0; i < n; i++) {
            double a = r.nextDouble() * Math.PI * 2, dist = i == 0 ? 0 : 6 + r.nextDouble() * 15;
            int px = cx + (int) Math.round(Math.cos(a) * dist), pz = cz + (int) Math.round(Math.sin(a) * dist);
            int floor = t.column(px, pz).solid;
            int top = Terrain.SEA + 5 + r.nextInt(i == 0 ? 20 : 13);
            int base = 2 + r.nextInt(2);
            Block body = stone[r.nextInt(stone.length)];
            for (int y = floor - 1; y <= top; y++) {
                int rad = Math.max(1, base - (y - floor) / 9);
                if (y > top - 2) {
                    rad = Math.max(0, rad - 1);
                }
                b.disc(px, y, pz, rad, d(y > top - 3 ? Blocks.CALCITE : body));
            }
            // corais ao redor da base
            for (int k = 0; k < 9; k++) {
                double ca = r.nextDouble() * Math.PI * 2, cd = base + 1 + r.nextDouble() * 4;
                int kx = px + (int) Math.round(Math.cos(ca) * cd), kz = pz + (int) Math.round(Math.sin(ca) * cd);
                int kf = t.column(kx, kz).solid;
                int h = 1 + r.nextInt(3);
                int ci = r.nextInt(coral.length);
                b.fill(kx, kf, kz, kx, kf + h - 1, kz, d(coral[ci]));
                if (kf + h <= Terrain.SEA - 1) {
                    b.set(kx, kf + h, kz, d(fans[ci]));
                }
            }
        }
        // lanterna do mar escondida no pilar central
        int f0 = t.column(cx, cz).solid;
        b.set(cx, f0 + 3, cz, d(Blocks.SEA_LANTERN));
    }

    // ------------------------------------------------------------------ pontos de interesse de estrada (pequenos, raio <= 8)
    public static void estalagem(Blueprint b, int cx, int cz, int g, Random r) {
        Pal p = Pal.planicie();
        Lay.clearDisc(b, cx, cz, 12, g, 14);
        b.disc(cx, g, cz, 8, d(Blocks.COARSE_DIRT));
        int[] c = Parts.shop(b, p, r, cx - 5, cz - 4, 10, 8, g, Direction.SOUTH, Parts.Shop.TAVERNA);
        b.spawn(c[0], g + 1, c[1], Mob.NPC, NpcRole.TAVERNEIRO.ordinal());
        Parts.well(b, cx + 6, cz + 5, g, p);
        Parts.lamp(b, cx - 6, g, cz + 5, p);
        b.set(cx + 5, g + 1, cz + 7, d(Blocks.HAY_BLOCK));
        b.set(cx - 5, g + 1, cz + 7, d(Blocks.BARREL));
        b.chest(cx + 4, g + 1, cz - 3, Direction.WEST, "biblioteca");
        walker(b, cx, g + 1, cz + 6, Kind.VIAJANTE);
        walker(b, cx + 2, g + 1, cz + 6, r.nextBoolean() ? Kind.MERCADOR : Kind.CACADOR);
    }

    public static void torreVigia(Blueprint b, int cx, int cz, int g, Random r) {
        Lay.clearDisc(b, cx, cz, 9, g, 30);
        b.disc(cx, g - 1, cz, 6, d(Blocks.COBBLESTONE));
        Parts.roundTower(b, cx, cz, g, 4, 16, d(Blocks.STONE_BRICKS), d(Blocks.STONE_BRICKS));
        b.clear(cx, g + 1, cz + 4, cx, g + 2, cz + 4);
        b.fill(cx - 3, g + 1, cz, cx - 3, g + 15, cz, Bk.ladder(Direction.EAST));
        b.clear(cx - 3, g + 16, cz, cx - 3, g + 16, cz);
        b.set(cx, g + 17, cz, d(Blocks.CAMPFIRE));
        b.set(cx, g + 6, cz, Bk.lantern(true));
        b.chest(cx + 2, g + 1, cz - 1, Direction.WEST, "castelo");
        walker(b, cx, g + 1, cz + 1, Kind.GUARDA);
        walker(b, cx + 2, g + 1, cz + 6, Kind.GUARDA);
    }

    public static void santuario(Blueprint b, int cx, int cz, int g, Random r) {
        Lay.clearDisc(b, cx, cz, 9, g, 14);
        b.disc(cx, g, cz, 7, d(Blocks.STONE_BRICKS));
        b.fill(cx - 3, g, cz - 3, cx + 3, g, cz + 3, d(Blocks.POLISHED_ANDESITE));
        for (int sx : new int[]{-3, 3}) {
            for (int sz : new int[]{-3, 3}) {
                b.fill(cx + sx, g + 1, cz + sz, cx + sx, g + 5, cz + sz, Bk.pillar(Blocks.QUARTZ_PILLAR, Direction.Axis.Y));
            }
        }
        b.fill(cx - 4, g + 6, cz - 4, cx + 4, g + 6, cz + 4, d(Blocks.QUARTZ_SLAB));
        b.fill(cx - 2, g + 7, cz - 2, cx + 2, g + 7, cz + 2, d(Blocks.QUARTZ_SLAB));
        b.set(cx, g + 1, cz - 2, d(Blocks.CHISELED_QUARTZ_BLOCK));
        b.set(cx, g + 2, cz - 2, d(Blocks.LECTERN));
        b.set(cx, g + 5, cz, Bk.lantern(true));
        b.chest(cx + 2, g + 1, cz - 2, Direction.WEST, "biblioteca");
        for (int i = 0; i < 4; i++) {
            b.set(cx - 5 + i * 3, g + 1, cz + 6, d(i % 2 == 0 ? Blocks.ALLIUM : Blocks.AZURE_BLUET));
        }
        walker(b, cx, g + 1, cz + 1, Kind.SACERDOTE);
    }

    public static void postoAbandonado(Blueprint b, int cx, int cz, int g, Random r) {
        Block[] stones = {Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.MOSSY_COBBLESTONE};
        Lay.clearDisc(b, cx, cz, 9, g, 12);
        b.disc(cx, g, cz, 7, d(Blocks.MOSSY_COBBLESTONE));
        for (int x = -5; x <= 5; x++) {
            for (int z = -4; z <= 4; z++) {
                boolean edge = Math.abs(x) == 5 || Math.abs(z) == 4;
                if (edge && r.nextInt(4) != 0) {
                    int h = 1 + r.nextInt(4);
                    b.fill(cx + x, g + 1, cz + z, cx + x, g + h, cz + z, d(stones[r.nextInt(stones.length)]));
                }
            }
        }
        b.clear(cx - 1, g + 1, cz + 4, cx + 1, g + 3, cz + 4);
        b.set(cx, g + 1, cz, d(Blocks.CAMPFIRE));
        b.chest(cx + 3, g + 1, cz - 2, Direction.WEST, "ruina");
        b.set(cx - 3, g + 1, cz + 2, d(Blocks.COBWEB));
        b.set(cx + 2, g + 2, cz - 3, d(Blocks.COBWEB));
        b.spawn(cx - 2, g + 1, cz - 1, Mob.LICANTROPO, 0);
        b.spawn(cx + 2, g + 1, cz + 1, Mob.LICANTROPO, 0);
    }
}
