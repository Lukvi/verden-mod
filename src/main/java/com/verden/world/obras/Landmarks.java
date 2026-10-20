package com.verden.world.obras;

import com.verden.entity.NpcRole;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.Terrain;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

import java.util.Random;

import static com.verden.world.obras.Blueprint.Mob;

/** Marcos de cada lugar do mapa de Verden. Todos desenham em volta de (cx, cz) com o chão em g. */
public final class Landmarks {
    private Landmarks() {}

    private static net.minecraft.block.BlockState d(Block b) {
        return Bk.d(b);
    }

    // ------------------------------------------------------------------ Veroogh: Partenon
    public static void partenon(Blueprint b, int cx, int cz, int g, Pal p) {
        Lay.clearDisc(b, cx, cz, 34, g, 40);
        b.fill(cx - 24, g, cz - 15, cx + 24, g, cz + 15, d(Blocks.SMOOTH_QUARTZ));
        b.fill(cx - 24, g + 1, cz - 15, cx + 24, g + 1, cz + 15, d(Blocks.QUARTZ_BLOCK));
        b.fill(cx - 22, g + 2, cz - 13, cx + 22, g + 2, cz + 13, d(Blocks.QUARTZ_BLOCK));
        b.fill(cx - 20, g + 3, cz - 11, cx + 20, g + 3, cz + 11, d(Blocks.SMOOTH_QUARTZ));
        int fl = g + 3;
        // escadarias leste e oeste
        for (int sd = -1; sd <= 1; sd += 2) {
            Direction up = sd > 0 ? Direction.WEST : Direction.EAST;
            for (int i = 0; i < 3; i++) {
                int x = cx + sd * (25 - i * 2 - 0);
                // degraus largos
                b.fill(cx + sd * (25 - i * 2), g + 1 + i, cz - 4, cx + sd * (24 - i * 2), g + 1 + i, cz + 4, Bk.stairs(Blocks.QUARTZ_STAIRS, up, false));
            }
        }
        // peristilo
        for (int x = -18; x <= 18; x += 4) {
            for (int sz : new int[]{-9, 9}) {
                b.fill(cx + x, fl + 1, cz + sz, cx + x, fl + 10, cz + sz, Bk.pillar(Blocks.QUARTZ_PILLAR, Direction.Axis.Y));
                b.set(cx + x, fl + 1, cz + sz, d(Blocks.CHISELED_QUARTZ_BLOCK));
                b.set(cx + x, fl + 10, cz + sz, d(Blocks.CHISELED_QUARTZ_BLOCK));
            }
        }
        for (int z = -5; z <= 5; z += 4) {
            for (int sx : new int[]{-18, 18}) {
                b.fill(cx + sx, fl + 1, cz + z, cx + sx, fl + 10, cz + z, Bk.pillar(Blocks.QUARTZ_PILLAR, Direction.Axis.Y));
            }
        }
        // arquitrave, friso e teto
        b.fill(cx - 19, fl + 11, cz - 10, cx + 19, fl + 11, cz + 10, d(Blocks.QUARTZ_BLOCK));
        b.fill(cx - 19, fl + 12, cz - 10, cx + 19, fl + 12, cz + 10, d(Blocks.PURPLE_CONCRETE));
        Parts.gable(b, p, cx - 19, cz - 10, 39, 21, fl + 13, true);
        // cela
        b.walls(cx - 12, fl + 1, cz - 5, cx + 12, fl + 10, cz + 5, d(Blocks.QUARTZ_BRICKS));
        b.clear(cx - 11, fl + 1, cz - 4, cx + 11, fl + 10, cz + 4);
        b.clear(cx + 12, fl + 1, cz - 1, cx + 12, fl + 4, cz + 1);
        b.clear(cx - 12, fl + 1, cz - 1, cx - 12, fl + 4, cz + 1);
        for (int x = -10; x <= 10; x += 4) {
            b.set(cx + x, fl + 6, cz - 5, Bk.pane(Blocks.PURPLE_STAINED_GLASS_PANE, true));
            b.set(cx + x, fl + 6, cz + 5, Bk.pane(Blocks.PURPLE_STAINED_GLASS_PANE, true));
            b.set(cx + x, fl + 10, cz, Bk.lantern(true));
        }
        // tapete e trono
        b.fill(cx - 9, fl + 1, cz, cx + 11, fl + 1, cz, d(Blocks.PURPLE_CARPET));
        b.fill(cx - 11, fl + 1, cz - 2, cx - 9, fl + 1, cz + 2, d(Blocks.GOLD_BLOCK));
        b.set(cx - 10, fl + 2, cz, Bk.stairs(Blocks.QUARTZ_STAIRS, Direction.EAST, false));
        b.set(cx - 11, fl + 2, cz, d(Blocks.QUARTZ_PILLAR));
        b.set(cx - 11, fl + 3, cz, d(Blocks.QUARTZ_PILLAR));
        b.set(cx - 11, fl + 4, cz, Bk.banner(Blocks.PURPLE_BANNER, 0));
        for (int z : new int[]{-3, 3}) {
            b.set(cx - 11, fl + 4, cz + z, Bk.wallBanner(Blocks.PURPLE_WALL_BANNER, Direction.EAST));
        }
        b.chest(cx - 11, fl + 1, cz - 4, Direction.SOUTH, "castelo");
        b.chest(cx - 11, fl + 1, cz + 4, Direction.NORTH, "castelo");
        b.spawn(cx - 8, fl + 1, cz, Mob.NPC, NpcRole.KONGE.ordinal());
        b.spawn(cx - 6, fl + 1, cz + 2, Mob.NPC, NpcRole.MENETRYX.ordinal());
        for (int x = -3; x <= 9; x += 4) {
            b.spawn(cx + x, fl + 1, cz - 2, Mob.SOLDADO, 0);
            b.spawn(cx + x, fl + 1, cz + 2, Mob.SOLDADO, 0);
        }
        // torres de canto com cone roxo
        for (int sx : new int[]{-30, 30}) {
            for (int sz : new int[]{-19, 19}) {
                Parts.roundTower(b, cx + sx, cz + sz, g, 4, 22, d(Blocks.QUARTZ_BRICKS), d(Blocks.QUARTZ_BLOCK));
                Parts.cone(b, cx + sx, g + 23, cz + sz, 5, d(Blocks.PURPLE_GLAZED_TERRACOTTA == null ? Blocks.PURPLE_CONCRETE : Blocks.PURPLE_CONCRETE));
                b.set(cx + sx, g + 1, cz + sz + (sz > 0 ? -4 : 4), Bk.AIR);
                b.set(cx + sx, g + 1, cz + sz, Bk.AIR);
            }
        }
        // fontes
        for (int sd = -1; sd <= 1; sd += 2) {
            b.fill(cx + sd * 30 - 1, g, cz - 1, cx + sd * 30 + 1, g + 1, cz + 1, d(Blocks.QUARTZ_BLOCK));
            b.set(cx + sd * 30, g + 1, cz, d(Blocks.WATER));
        }
    }

    // ------------------------------------------------------------------ fortaleza: Nagdaar (e variantes)
    public static void fortress(Blueprint b, int cx, int cz, int g, Pal p, int half, int keepH, Block carpet, Block banner, Block roof, String loot, Mob guard) {
        Lay.clearDisc(b, cx, cz, half + 10, g, keepH + 20);
        b.fill(cx - half, g, cz - half, cx + half, g, cz + half, d(p.path2));
        // muralha interna
        b.walls(cx - half, g - 5, cz - half, cx + half, g + 12, cz + half, p.wallS());
        b.walls(cx - half + 1, g + 1, cz - half + 1, cx + half - 1, g + 12, cz + half - 1, p.wallS());
        b.walls(cx - half, g + 13, cz - half, cx + half, g + 13, cz + half, d(p.foundation));
        for (int i = -half; i <= half; i += 2) {
            b.set(cx + i, g + 14, cz - half, d(p.foundation));
            b.set(cx + i, g + 14, cz + half, d(p.foundation));
            b.set(cx - half, g + 14, cz + i, d(p.foundation));
            b.set(cx + half, g + 14, cz + i, d(p.foundation));
        }
        // portão sul
        b.clear(cx - 3, g + 1, cz + half - 1, cx + 3, g + 8, cz + half);
        b.fill(cx - 3, g + 9, cz + half - 1, cx + 3, g + 9, cz + half, d(p.corner));
        for (int sd : new int[]{-1, 1}) {
            b.set(cx + sd * 4, g + 5, cz + half + 1, Bk.wallBanner(banner, Direction.SOUTH));
        }
        // torres de canto
        for (int sx : new int[]{-half, half}) {
            for (int sz : new int[]{-half, half}) {
                Parts.roundTower(b, cx + sx, cz + sz, g - 4, 5, 24, p.wallS(), d(p.foundation));
                Parts.cone(b, cx + sx, g + 21, cz + sz, 6, d(roof));
            }
        }
        // torre de menagem
        int kh = 9;
        b.fill(cx - kh, g, cz - kh, cx + kh, g, cz + kh, d(p.floor));
        b.walls(cx - kh, g + 1, cz - kh, cx + kh, g + keepH, cz + kh, p.wallS());
        b.clear(cx - kh + 1, g + 1, cz - kh + 1, cx + kh - 1, g + keepH, cz + kh - 1);
        for (int sx : new int[]{-kh, kh}) {
            for (int sz : new int[]{-kh, kh}) {
                b.fill(cx + sx, g + 1, cz + sz, cx + sx, g + keepH + 2, cz + sz, Bk.pillar(p.corner, Direction.Axis.Y));
            }
        }
        b.fill(cx - kh, g + keepH + 1, cz - kh, cx + kh, g + keepH + 1, cz + kh, d(p.floor));
        Parts.pyramid(b, cx, g + keepH + 2, cz, kh + 1, 8, d(roof));
        for (int y = g + 8; y < g + keepH; y += 8) {
            b.fill(cx - kh + 1, y, cz - kh + 1, cx + kh - 1, y, cz + kh - 1, d(p.floor));
            b.clear(cx + kh - 2, y, cz + kh - 2, cx + kh - 2, y, cz + kh - 2);
        }
        b.fill(cx + kh - 2, g + 1, cz + kh - 2, cx + kh - 2, g + keepH, cz + kh - 2, Bk.ladder(Direction.WEST));
        b.clear(cx - 1, g + 1, cz + kh, cx + 1, g + 4, cz + kh);
        for (int y = g + 4; y < g + keepH; y += 8) {
            for (int i = -6; i <= 6; i += 4) {
                b.set(cx + i, y, cz - kh, Bk.pane(Blocks.GLASS_PANE, true));
            }
        }
        // salão do trono
        b.fill(cx - 1, g + 1, cz - 7, cx + 1, g + 1, cz + kh, d(carpet));
        b.fill(cx - 2, g + 1, cz - 8, cx + 2, g + 1, cz - 8, d(Blocks.GOLD_BLOCK));
        b.set(cx, g + 2, cz - 8, Bk.stairs(p.roofStairs, Direction.SOUTH, false));
        b.set(cx, g + 3, cz - 9, Bk.banner(banner, 0));
        b.set(cx - 5, g + 6, cz, Bk.lantern(true));
        b.set(cx + 5, g + 6, cz, Bk.lantern(true));
        b.chest(cx - kh + 1, g + 1, cz - kh + 1, Direction.EAST, loot);
        b.chest(cx + kh - 1, g + 1, cz - kh + 1, Direction.WEST, loot);
        if (guard != null) {
            for (int sd : new int[]{-3, 3}) {
                b.spawn(cx + sd, g + 1, cz + 2, guard, 0);
                b.spawn(cx + sd, g + 1, cz - 5, guard, 0);
            }
            b.spawn(cx, g + 1, cz + half - 3, guard, 0);
        }
        // estandartes no pátio
        for (int k = -half + 6; k <= half - 6; k += 8) {
            if (Math.abs(k) > 12) {
                Parts.banner(b, cx + k, g, cz + half - 6, p, 5);
                Parts.banner(b, cx + k, g, cz - half + 6, p, 5);
            }
        }
    }

    // ------------------------------------------------------------------ Jørd: salão das guildas
    public static void guildhall(Blueprint b, int cx, int cz, int g, Pal p) {
        Lay.clearDisc(b, cx, cz, 36, g, 50);
        b.disc(cx, g, cz, 30, d(p.path2));
        // fonte central
        b.disc(cx, g, cz, 3, d(Blocks.STONE_BRICKS));
        b.disc(cx, g + 1, cz, 2, d(Blocks.STONE_BRICKS));
        b.set(cx, g + 1, cz, d(Blocks.WATER));
        b.fill(cx, g + 2, cz, cx, g + 4, cz, d(Blocks.STONE_BRICK_WALL));
        // salão
        int x1 = cx - 17, x2 = cx + 17, z1 = cz - 26, z2 = cz - 8;
        b.fill(x1, g, z1, x2, g, z2, d(p.floor));
        b.walls(x1, g + 1, z1, x2, g + 9, z2, p.wallS());
        b.walls(x1, g + 1, z1, x2, g + 1, z2, d(p.foundation));
        b.clear(x1 + 1, g + 1, z1 + 1, x2 - 1, g + 9, z2 - 1);
        for (int x = x1; x <= x2; x += 6) {
            b.fill(x, g + 1, z1, x, g + 9, z1, Bk.pillar(p.corner, Direction.Axis.Y));
            b.fill(x, g + 1, z2, x, g + 9, z2, Bk.pillar(p.corner, Direction.Axis.Y));
            if (x > x1 && x < x2) {
                b.fill(x - 2, g + 3, z1, x - 1, g + 6, z1, Bk.pane(p.glass, true));
                b.fill(x - 2, g + 3, z2, x - 1, g + 6, z2, Bk.pane(p.glass, true));
            }
        }
        b.fill(x1, g + 9, z1, x2, g + 9, z2, d(p.floor));
        Parts.gable(b, p, x1, z1, 35, 19, g + 10, true);
        b.clear(cx - 2, g + 1, z2, cx + 2, g + 5, z2);
        b.fill(cx - 2, g + 6, z2, cx + 2, g + 6, z2, d(p.corner));
        for (int sd : new int[]{-4, 4}) {
            b.set(cx + sd, g + 4, z2 + 1, Bk.wallBanner(p.banner == Blocks.PURPLE_BANNER ? Blocks.PURPLE_WALL_BANNER : Blocks.RED_WALL_BANNER, Direction.SOUTH));
        }
        // mesas das guildas
        for (int x = x1 + 4; x <= x2 - 4; x += 5) {
            for (int z : new int[]{z1 + 5, z1 + 11}) {
                b.fill(x, g + 1, z, x + 2, g + 1, z, d(Blocks.SPRUCE_PLANKS));
                b.fill(x, g + 2, z, x + 2, g + 2, z, Bk.slab(Blocks.SPRUCE_SLAB, false));
                b.set(x, g + 1, z + 1, Bk.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, false));
                b.set(x + 2, g + 1, z + 1, Bk.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, false));
            }
            b.set(x + 1, g + 8, z1 + 8, Bk.lantern(true));
        }
        b.chest(x1 + 1, g + 1, z1 + 1, Direction.SOUTH, "castelo");
        b.chest(x2 - 1, g + 1, z1 + 1, Direction.SOUTH, "castelo");
        b.spawn(cx - 6, g + 1, z1 + 8, Mob.NPC, NpcRole.RASHAD.ordinal());
        b.spawn(cx + 6, g + 1, z1 + 8, Mob.NPC, NpcRole.THALEN.ordinal());
        b.spawn(cx, g + 1, z1 + 14, Mob.NPC, NpcRole.NOITE.ordinal());
        // campanário
        int bx = cx + 22, bz = cz - 22;
        b.fill(bx - 3, g, bz - 3, bx + 3, g, bz + 3, d(p.floor));
        b.walls(bx - 3, g + 1, bz - 3, bx + 3, g + 30, bz + 3, p.wallS());
        b.clear(bx - 2, g + 1, bz - 2, bx + 2, g + 30, bz + 2);
        for (int sx : new int[]{-3, 3}) {
            for (int sz : new int[]{-3, 3}) {
                b.fill(bx + sx, g + 1, bz + sz, bx + sx, g + 31, bz + sz, Bk.pillar(p.corner, Direction.Axis.Y));
            }
        }
        b.fill(bx - 2, g + 1, bz + 2, bx - 2, g + 26, bz + 2, Bk.ladder(Direction.NORTH));
        b.clear(bx - 2, g + 1, bz + 3, bx - 2, g + 3, bz + 3);
        b.fill(bx - 2, g + 20, bz - 2, bx + 2, g + 20, bz + 2, d(p.floor));
        b.clear(bx - 2, g + 20, bz + 2, bx - 2, g + 20, bz + 2);
        b.clear(bx - 2, g + 21, bz - 2, bx + 2, g + 26, bz + 2);
        for (int sx : new int[]{-3, 3}) {
            b.clear(bx + sx, g + 22, bz - 1, bx + sx, g + 25, bz + 1);
        }
        for (int sz : new int[]{-3, 3}) {
            b.clear(bx - 1, g + 22, bz + sz, bx + 1, g + 25, bz + sz);
        }
        b.set(bx, g + 23, bz, d(Blocks.BELL));
        b.fill(bx - 2, g + 27, bz - 2, bx + 2, g + 27, bz + 2, d(p.floor));
        Parts.pyramid(b, bx, g + 28, bz, 4, 5, d(p.roofBlock));
        b.set(bx, g + 33, bz, Bk.banner(p.banner, 0));
        // estandartes ao longo da praça
        for (int k = -24; k <= 24; k += 12) {
            Parts.banner(b, cx + k, g, cz + 12, p, 6);
        }
    }

    // ------------------------------------------------------------------ Flerin: palácio com cúpulas
    public static void palace(Blueprint b, int cx, int cz, int g, Pal p) {
        Lay.clearDisc(b, cx, cz, 44, g, 50);
        int half = 36;
        b.fill(cx - half, g, cz - half, cx + half, g, cz + half, d(Blocks.SMOOTH_SANDSTONE));
        b.walls(cx - half, g - 4, cz - half, cx + half, g + 11, cz + half, d(Blocks.SANDSTONE));
        b.walls(cx - half + 1, g + 1, cz - half + 1, cx + half - 1, g + 11, cz + half - 1, d(Blocks.SANDSTONE));
        for (int i = -half; i <= half; i += 2) {
            b.set(cx + i, g + 12, cz - half, d(Blocks.CUT_SANDSTONE));
            b.set(cx + i, g + 12, cz + half, d(Blocks.CUT_SANDSTONE));
            b.set(cx - half, g + 12, cz + i, d(Blocks.CUT_SANDSTONE));
            b.set(cx + half, g + 12, cz + i, d(Blocks.CUT_SANDSTONE));
        }
        b.clear(cx - 4, g + 1, cz + half - 1, cx + 4, g + 9, cz + half);
        b.fill(cx - 4, g + 10, cz + half - 1, cx + 4, g + 10, cz + half, d(Blocks.CHISELED_SANDSTONE));
        // jardim com espelho d'água
        b.fill(cx - 4, g, cz + 8, cx + 4, g, cz + 28, d(Blocks.WATER));
        b.fill(cx - 5, g, cz + 8, cx - 5, g, cz + 28, d(Blocks.CUT_SANDSTONE));
        b.fill(cx + 5, g, cz + 8, cx + 5, g, cz + 28, d(Blocks.CUT_SANDSTONE));
        for (int z = 10; z <= 28; z += 6) {
            Parts.lamp(b, cx - 7, g, cz + z, p);
            Parts.lamp(b, cx + 7, g, cz + z, p);
        }
        // minaretes nos cantos
        for (int sx : new int[]{-half, half}) {
            for (int sz : new int[]{-half, half}) {
                Parts.roundTower(b, cx + sx, cz + sz, g - 3, 3, 30, d(Blocks.SANDSTONE), d(Blocks.CUT_SANDSTONE));
                Parts.dome(b, cx + sx, g + 28, cz + sz, 3, d(Blocks.ORANGE_TERRACOTTA));
                b.set(cx + sx, g + 32, cz + sz, d(Blocks.GOLD_BLOCK));
            }
        }
        // corpo do palácio
        int w = 14;
        b.fill(cx - w, g, cz - w, cx + w, g, cz + w, d(Blocks.CUT_SANDSTONE));
        b.walls(cx - w, g + 1, cz - w, cx + w, g + 14, cz + w, d(Blocks.SMOOTH_SANDSTONE));
        b.clear(cx - w + 1, g + 1, cz - w + 1, cx + w - 1, g + 24, cz + w - 1);
        b.fill(cx - w, g + 15, cz - w, cx + w, g + 15, cz + w, d(Blocks.CUT_SANDSTONE));
        b.clear(cx - 1, g + 1, cz + w, cx + 1, g + 5, cz + w);
        for (int sx : new int[]{-w, w}) {
            for (int sz : new int[]{-w, w}) {
                b.fill(cx + sx, g + 1, cz + sz, cx + sx, g + 15, cz + sz, Bk.pillar(Blocks.CHISELED_SANDSTONE == null ? Blocks.SANDSTONE : Blocks.CUT_SANDSTONE, Direction.Axis.Y));
            }
        }
        for (int i = -10; i <= 10; i += 5) {
            b.fill(cx + i, g + 3, cz - w, cx + i, g + 8, cz - w, Bk.pane(Blocks.ORANGE_STAINED_GLASS_PANE, true));
            b.fill(cx - w, g + 3, cz + i, cx - w, g + 8, cz + i, Bk.pane(Blocks.ORANGE_STAINED_GLASS_PANE, false));
            b.fill(cx + w, g + 3, cz + i, cx + w, g + 8, cz + i, Bk.pane(Blocks.ORANGE_STAINED_GLASS_PANE, false));
        }
        // cúpula central
        Parts.dome(b, cx, g + 16, cz, 12, d(Blocks.WHITE_TERRACOTTA));
        Parts.dome(b, cx, g + 17, cz, 11, d(Blocks.AIR));
        b.disc(cx, g + 16, cz, 11, d(Blocks.AIR));
        b.disc(cx, g + 15, cz, 12, d(Blocks.CUT_SANDSTONE));
        b.set(cx, g + 29, cz, d(Blocks.GOLD_BLOCK));
        b.fill(cx, g + 30, cz, cx, g + 32, cz, d(Blocks.LIGHTNING_ROD));
        // interior
        b.fill(cx - 2, g + 1, cz - 12, cx + 2, g + 1, cz + w - 1, d(Blocks.ORANGE_CARPET));
        b.fill(cx - 3, g + 1, cz - 13, cx + 3, g + 1, cz - 11, d(Blocks.GOLD_BLOCK));
        b.set(cx, g + 2, cz - 12, Bk.stairs(Blocks.SANDSTONE_STAIRS, Direction.SOUTH, false));
        for (int z = -8; z <= 10; z += 6) {
            b.set(cx - 6, g + 12, cz + z, Bk.lantern(true));
            b.set(cx + 6, g + 12, cz + z, Bk.lantern(true));
        }
        b.chest(cx - w + 1, g + 1, cz - w + 1, Direction.EAST, "castelo");
        b.chest(cx + w - 1, g + 1, cz - w + 1, Direction.WEST, "castelo");
        b.spawn(cx - 4, g + 1, cz + 4, Mob.NPC, NpcRole.ROSE.ordinal());
        b.spawn(cx + 4, g + 1, cz + 4, Mob.NPC, NpcRole.HIRAKU.ordinal());
    }

    // ------------------------------------------------------------------ Maariz: porto pirata
    public static void pirateHarbor(Blueprint b, Site s, Pal p, Terrain t, Random r) {
        int cx = s.x(), cz = s.z(), g = s.level(), R = s.radius();
        // cais: varre faixas de z; onde a terra encontra a água, estende um píer
        int piers = 0;
        for (int dz = -R; dz <= R && piers < 8; dz += 11) {
            int z = cz + dz;
            for (int dir = -1; dir <= 1; dir += 2) {
                int lastLand = Integer.MIN_VALUE;
                int firstWater = Integer.MIN_VALUE;
                for (int dx = 0; dx <= R + 40; dx++) {
                    int x = cx + dir * dx;
                    Terrain.Column c = t.column(x, z);
                    boolean wet = c.water > c.solid;
                    if (!wet) {
                        lastLand = x;
                    } else if (lastLand != Integer.MIN_VALUE) {
                        firstWater = x;
                        break;
                    }
                }
                if (firstWater != Integer.MIN_VALUE && Math.abs(firstWater - cx) > 8) {
                    pier(b, firstWater - dir * 4, z, dir, 26, Terrain.SEA, r, piers % 2 == 0);
                    piers++;
                    break;
                }
            }
        }
    }

    private static void pier(Blueprint b, int x0, int z, int dir, int len, int sea, Random r, boolean ship) {
        int y = sea + 1;
        int xa = Math.min(x0, x0 + dir * len), xb = Math.max(x0, x0 + dir * len);
        b.fill(xa, y, z - 1, xb, y, z + 1, d(Blocks.SPRUCE_PLANKS));
        for (int i = 0; i <= len; i += 4) {
            int x = x0 + dir * i;
            b.fill(x, sea - 6, z - 1, x, y - 1, z - 1, d(Blocks.SPRUCE_LOG));
            b.fill(x, sea - 6, z + 1, x, y - 1, z + 1, d(Blocks.SPRUCE_LOG));
            b.set(x, y + 1, z - 1, d(Blocks.SPRUCE_FENCE));
            b.set(x, y + 1, z + 1, d(Blocks.SPRUCE_FENCE));
            if (i % 8 == 0) {
                b.set(x, y + 2, z - 1, d(Blocks.LANTERN));
            }
        }
        if (ship) {
            int sx = x0 + dir * (len - 8), sz = z + 5;
            ship(b, Math.min(sx, sx + 14), sz, y, true);
        }
    }

    /** Navio pirata ao longo de x: casco de carvalho escuro, mastro e vela negra. */
    public static void ship(Blueprint b, int x, int z, int y, boolean sails) {
        int len = 15;
        for (int i = 0; i < len; i++) {
            int w = i < 2 || i > len - 3 ? 1 : 2;
            b.fill(x + i, y - 1, z - w, x + i, y - 1, z + w, d(Blocks.DARK_OAK_PLANKS));
            b.fill(x + i, y, z - w - (w < 2 ? 0 : 0), x + i, y, z - w, d(Blocks.DARK_OAK_PLANKS));
            b.fill(x + i, y, z + w, x + i, y, z + w, d(Blocks.DARK_OAK_PLANKS));
            b.fill(x + i, y + 1, z - w, x + i, y + 1, z - w, d(Blocks.DARK_OAK_FENCE));
            b.fill(x + i, y + 1, z + w, x + i, y + 1, z + w, d(Blocks.DARK_OAK_FENCE));
            b.fill(x + i, y, z - w + 1, x + i, y, z + w - 1, d(Blocks.SPRUCE_PLANKS));
        }
        b.fill(x + 7, y + 1, z, x + 7, y + 13, z, d(Blocks.DARK_OAK_LOG));
        if (sails) {
            b.fill(x + 7, y + 6, z - 3, x + 7, y + 12, z + 3, d(Blocks.BLACK_WOOL));
            b.set(x + 7, y + 14, z, Bk.banner(Blocks.BLACK_BANNER, 0));
        }
        b.chest(x + 12, y + 1, z, Direction.WEST, "porto");
    }

    // ------------------------------------------------------------------ Montainezz: mosteiro
    public static void monastery(Blueprint b, int cx, int cz, int g, Pal p) {
        Lay.clearDisc(b, cx, cz, 44, g, 40);
        int half = 30;
        b.fill(cx - half, g, cz - half, cx + half, g, cz + half, d(Blocks.STONE_BRICKS));
        b.walls(cx - half, g + 1, cz - half, cx + half, g + 4, cz + half, d(Blocks.WHITE_CONCRETE));
        b.walls(cx - half, g + 5, cz - half, cx + half, g + 5, cz + half, d(Blocks.CHERRY_SLAB));
        // portão torii a sul
        b.clear(cx - 3, g + 1, cz + half, cx + 3, g + 4, cz + half);
        for (int sd : new int[]{-4, 4}) {
            b.fill(cx + sd, g + 1, cz + half + 3, cx + sd, g + 8, cz + half + 3, d(Blocks.RED_TERRACOTTA));
        }
        b.fill(cx - 6, g + 8, cz + half + 3, cx + 6, g + 8, cz + half + 3, d(Blocks.RED_TERRACOTTA));
        b.fill(cx - 5, g + 6, cz + half + 3, cx + 5, g + 6, cz + half + 3, d(Blocks.CRIMSON_PLANKS));
        b.fill(cx, g, cz + half + 1, cx, g, cz + half + 12, d(Blocks.STONE_BRICKS));
        // caminho central e lagos
        b.fill(cx - 1, g, cz + 8, cx + 1, g, cz + half - 1, d(Blocks.POLISHED_ANDESITE));
        for (int sd : new int[]{-1, 1}) {
            b.fill(cx + sd * 12 - 4, g, cz + 10, cx + sd * 12 + 4, g, cz + 24, d(Blocks.WATER));
            b.walls(cx + sd * 12 - 5, g, cz + 9, cx + sd * 12 + 5, g, cz + 25, d(Blocks.STONE_BRICKS));
        }
        // pagode principal (5 níveis)
        for (int t = 0; t < 5; t++) {
            int hh = 9 - t * 2 + (t == 4 ? 1 : 0);
            hh = Math.max(hh, 2);
            int y0 = g + 1 + t * 6;
            b.fill(cx - hh, y0, cz - 8 - 0, cx + hh, y0, cz + 0, d(Blocks.AIR));
            b.fill(cx - hh, y0, cz - hh - 4, cx + hh, y0, cz + hh - 4, d(Blocks.DARK_OAK_PLANKS));
            b.walls(cx - hh, y0 + 1, cz - hh - 4, cx + hh, y0 + 4, cz + hh - 4, d(Blocks.WHITE_CONCRETE));
            b.clear(cx - hh + 1, y0 + 1, cz - hh - 3, cx + hh - 1, y0 + 4, cz + hh - 5);
            for (int sx : new int[]{-hh, hh}) {
                for (int sz : new int[]{-hh - 4, hh - 4}) {
                    b.fill(cx + sx, y0 + 1, cz + sz, cx + sx, y0 + 4, cz + sz, Bk.pillar(Blocks.CRIMSON_STEM, Direction.Axis.Y));
                }
            }
            for (int i = -hh + 2; i <= hh - 2; i += 3) {
                b.fill(cx + i, y0 + 2, cz + hh - 4, cx + i, y0 + 3, cz + hh - 4, Bk.pane(Blocks.WHITE_STAINED_GLASS_PANE, true));
            }
            // telhado cerejeira com beiral largo
            Parts.pyramid(b, cx, y0 + 5, cz - 4, hh + 3, 3, d(Blocks.CHERRY_PLANKS));
            b.walls(cx - hh - 3, y0 + 5, cz - 4 - hh - 3, cx + hh + 3, y0 + 5, cz - 4 + hh + 3, d(Blocks.CHERRY_SLAB));
            b.set(cx, y0 + 4, cz - 4, Bk.lantern(true));
        }
        b.set(cx, g + 33, cz - 4, d(Blocks.LIGHTNING_ROD));
        b.clear(cx - 1, g + 1, cz + 5, cx + 1, g + 4, cz + 5);
        b.chest(cx - 6, g + 2, cz - 6, Direction.EAST, "templo");
        b.spawn(cx, g + 2, cz - 2, Mob.NPC, NpcRole.YANN.ordinal());
        // dojôs laterais
        for (int sd : new int[]{-1, 1}) {
            int dx = cx + sd * 20;
            b.fill(dx - 5, g, cz - 14, dx + 5, g, cz - 6, d(Blocks.SPRUCE_PLANKS));
            b.walls(dx - 5, g + 1, cz - 14, dx + 5, g + 4, cz - 6, d(Blocks.WHITE_CONCRETE));
            b.clear(dx - 4, g + 1, cz - 13, dx + 4, g + 4, cz - 7);
            b.clear(dx - 1, g + 1, cz - 6, dx + 1, g + 2, cz - 6);
            Parts.pyramid(b, dx, g + 5, cz - 10, 6, 4, d(Blocks.CHERRY_PLANKS));
            b.set(dx, g + 4, cz - 10, Bk.lantern(true));
            b.set(dx - 3, g + 1, cz - 12, Bk.bedPart(Blocks.WHITE_BED, Direction.EAST, false));
            b.set(dx - 2, g + 1, cz - 12, Bk.bedPart(Blocks.WHITE_BED, Direction.EAST, true));
        }
        for (int a = 0; a < 4; a++) {
            Parts.lamp(b, cx + (a < 2 ? -3 : 3), g, cz + 12 + (a % 2) * 8, p);
        }
    }

    // ------------------------------------------------------------------ Torre de Merlim
    public static void merlinTower(Blueprint b, int cx, int cz, int g) {
        Lay.clearDisc(b, cx, cz, 14, g, 80);
        int rad = 6, h = 62;
        b.disc(cx, g - 3, cz, 14, d(Blocks.STONE_BRICKS));
        b.disc(cx, g, cz, 12, d(Blocks.QUARTZ_BLOCK));
        b.disc(cx, g + 1, cz, 10, d(Blocks.QUARTZ_BRICKS));
        b.ring(cx, g + 1, g + h, cz, rad, d(Blocks.QUARTZ_BRICKS));
        for (int y = g + 4; y < g + h; y += 9) {
            b.ring(cx, y, y, cz, rad, d(Blocks.PURPLE_CONCRETE));
        }
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * i / 4;
            int x = cx + (int) Math.round(Math.cos(a) * rad), z = cz + (int) Math.round(Math.sin(a) * rad);
            b.fill(x, g + 1, z, x, g + h, z, Bk.pillar(Blocks.QUARTZ_PILLAR, Direction.Axis.Y));
        }
        b.clear(cx - rad + 1, g + 2, cz - rad + 1, cx + rad - 1, g + h, cz + rad - 1);
        b.clear(cx - 1, g + 1, cz + rad, cx + 1, g + 4, cz + rad);
        b.clear(cx - rad, g + 2, cz - 2, cx - rad, g + 4, cz + 2);
        // andares e escada de mão
        int ladX = cx + rad - 1;
        b.fill(ladX, g + 1, cz, ladX, g + h - 1, cz, Bk.ladder(Direction.WEST));
        for (int y = g + 10; y < g + h - 5; y += 10) {
            b.disc(cx, y, cz, rad - 1, d(Blocks.SPRUCE_PLANKS));
            b.clear(ladX, y, cz, ladX, y, cz);
            b.set(cx, y + 5, cz, Bk.lantern(true));
            b.set(cx, y + 6, cz, d(Blocks.CHAIN));
            // janelas
            for (int k = 0; k < 4; k++) {
                int wx = cx + (k == 0 ? rad : k == 1 ? -rad : 0), wz = cz + (k == 2 ? rad : k == 3 ? -rad : 0);
                if (!(wx == ladX + 1 && wz == cz)) {
                    b.fill(wx, y + 2, wz, wx, y + 3, wz, Bk.pane(Blocks.PURPLE_STAINED_GLASS_PANE, wx == cx));
                }
            }
        }
        // estudo: estantes e mesa de encantamento no 1º andar
        b.set(cx, g + 1, cz, d(Blocks.ENCHANTING_TABLE));
        for (int dxx : new int[]{-3, 3}) {
            b.fill(cx + dxx, g + 1, cz - 3, cx + dxx, g + 3, cz - 3, d(Blocks.BOOKSHELF));
        }
        b.fill(cx - 3, g + 1, cz - 4, cx + 3, g + 3, cz - 4, d(Blocks.BOOKSHELF));
        b.chest(cx - 4, g + 1, cz + 2, Direction.EAST, "torre");
        b.chest(cx, g + h - 6, cz - 3, Direction.SOUTH, "torre");
        // coroa e telhado
        b.fill(cx - rad, g + h, cz - rad, cx + rad, g + h, cz + rad, d(Blocks.AIR));
        b.disc(cx, g + h, cz, rad + 1, d(Blocks.PURPLE_CONCRETE));
        Parts.cone(b, cx, g + h + 1, cz, rad + 1, d(Blocks.PURPLE_TERRACOTTA));
        b.set(cx, g + h + rad + 3, cz, d(Blocks.AMETHYST_BLOCK));
        b.set(cx, g + h + rad + 4, cz, d(Blocks.LIGHTNING_ROD));
    }

    // ------------------------------------------------------------------ Templo de Casthiel
    public static void sunTemple(Blueprint b, int cx, int cz, int g) {
        Lay.clearDisc(b, cx, cz, 22, g, 30);
        b.disc(cx, g, cz, 18, d(Blocks.SMOOTH_SANDSTONE));
        b.disc(cx, g + 1, cz, 16, d(Blocks.CUT_SANDSTONE));
        b.disc(cx, g + 2, cz, 14, d(Blocks.SMOOTH_SANDSTONE));
        b.disc(cx, g + 2, cz, 5, d(Blocks.GOLD_BLOCK));
        b.disc(cx, g + 2, cz, 3, d(Blocks.RED_SAND == null ? Blocks.SAND : Blocks.SANDSTONE));
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * i / 6;
            int x = cx + (int) Math.round(Math.cos(a) * 12), z = cz + (int) Math.round(Math.sin(a) * 12);
            b.fill(x, g + 3, z, x, g + 11, z, Bk.pillar(Blocks.CUT_SANDSTONE, Direction.Axis.Y));
            b.set(x, g + 3, z, d(Blocks.CHISELED_SANDSTONE));
            b.set(x, g + 11, z, d(Blocks.CHISELED_SANDSTONE));
            b.set(x, g + 12, z, d(Blocks.GOLD_BLOCK));
            b.set(x, g + 13, z, Bk.banner(Blocks.YELLOW_BANNER, i));
        }
        b.ring(cx, g + 12, g + 12, cz, 12, d(Blocks.SMOOTH_SANDSTONE));
        b.ring(cx, g + 12, g + 12, cz, 11, d(Blocks.SMOOTH_SANDSTONE));
        // câmara selada
        b.hollow(cx - 3, g + 3, cz - 3, cx + 3, g + 8, cz + 3, d(Blocks.CHISELED_SANDSTONE));
        b.clear(cx - 2, g + 4, cz - 2, cx + 2, g + 7, cz + 2);
        b.set(cx, g + 7, cz, Bk.lantern(true));
        b.chest(cx, g + 4, cz, Direction.SOUTH, "templo");
        b.chest(cx - 2, g + 4, cz - 2, Direction.SOUTH, "templo");
        // entrada para a câmara: selo de bloco frágil
        b.set(cx, g + 4, cz + 3, d(Blocks.SANDSTONE));
        b.set(cx, g + 5, cz + 3, d(Blocks.SANDSTONE));
        for (int sd = -1; sd <= 1; sd += 2) {
            Parts.lamp(b, cx + sd * 16, g + 1, cz, Pal.deserto());
        }
    }

    // ------------------------------------------------------------------ Cidades de torres
    public static void spires(Blueprint b, int cx, int cz, int g, Pal p, Random r) {
        int n = 6;
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n + 0.3;
            int rr = 14 + (i % 2) * 6;
            int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
            int h = 28 + r.nextInt(18);
            int rad = 3 + r.nextInt(2);
            b.disc(x, g, z, rad + 1, d(p.foundation));
            Parts.roundTower(b, x, z, g, rad, h, p.wallS(), d(p.foundation));
            b.clear(x - rad + 1, g + 1, z - rad + 1, x + rad - 1, g + h - 1, z + rad - 1);
            b.fill(x, g + 1, z + rad - 1, x, g + h - 1, z + rad - 1, Bk.ladder(Direction.NORTH));
            b.clear(x, g + 1, z + rad, x, g + 2, z + rad);
            for (int y = g + 8; y < g + h; y += 8) {
                b.fill(x - rad + 1, y, z - rad + 1, x + rad - 1, y, z + rad - 1, d(p.floor));
                b.clear(x, y, z + rad - 1, x, y, z + rad - 1);
                b.fill(x + rad, y + 2, z, x + rad, y + 3, z, Bk.pane(p.glass, false));
            }
            Parts.cone(b, x, g + h + 2, z, rad + 1, d(p.roofBlock));
            b.set(x, g + h + rad + 3, z, Bk.banner(p.banner, 0));
        }
        // ponte entre duas torres e praça
        b.disc(cx, g, cz, 9, d(p.path2));
        b.set(cx, g + 1, cz, d(Blocks.WATER));
        b.chest(cx + 4, g + 1, cz, Direction.WEST, "casa");
    }

    // ------------------------------------------------------------------ Ruínas e acampamentos
    public static void ruins(Blueprint b, Site s, Pal p, Random r, int licans) {
        int cx = s.x(), cz = s.z(), g = s.level(), R = s.radius();
        Block[] stones = {Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.MOSSY_COBBLESTONE};
        Lay.clearDisc(b, cx, cz, R, g, 20);
        // plataforma central quebrada
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                if (r.nextInt(5) != 0) {
                    b.set(cx + x, g, cz + z, d(stones[r.nextInt(stones.length)]));
                }
            }
        }
        // colunas e paredes parciais em anel
        for (int i = 0; i < 26; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            double dist = 6 + r.nextDouble() * (R - 10);
            int x = cx + (int) Math.round(Math.cos(a) * dist), z = cz + (int) Math.round(Math.sin(a) * dist);
            int h = 1 + r.nextInt(6);
            if (r.nextBoolean()) {
                b.fill(x, g + 1, z, x, g + h, z, Bk.pillar(Blocks.STONE_BRICKS, Direction.Axis.Y));
            } else {
                int len = 2 + r.nextInt(6);
                boolean ax = r.nextBoolean();
                b.fill(x, g + 1, z, ax ? x + len : x, g + h, ax ? z : z + len, d(stones[r.nextInt(stones.length)]));
            }
        }
        b.hollow(cx - 2, g - 3, cz - 2, cx + 2, g - 1, cz + 2, d(Blocks.STONE_BRICKS));
        b.chest(cx, g - 2, cz, Direction.SOUTH, "ruina");
        b.chest(cx + 1, g - 2, cz - 1, Direction.SOUTH, "ruina");
        b.set(cx, g, cz, d(Blocks.AIR));
        for (int i = 0; i < licans; i++) {
            double a = 2 * Math.PI * i / Math.max(1, licans);
            b.spawn(cx + (int) Math.round(Math.cos(a) * 9), g + 1, cz + (int) Math.round(Math.sin(a) * 9), Mob.LICANTROPO, 0);
        }
    }

    public static void camp(Blueprint b, Site s, Random r) {
        int cx = s.x(), cz = s.z(), g = s.level();
        Lay.clearDisc(b, cx, cz, s.radius(), g, 12);
        Block[] cols = {Blocks.RED_WOOL, Blocks.WHITE_WOOL, Blocks.BROWN_WOOL};
        for (int i = 0; i < 6; i++) {
            double a = 2 * Math.PI * i / 6;
            int x = cx + (int) Math.round(Math.cos(a) * 11) - 2, z = cz + (int) Math.round(Math.sin(a) * 11) - 3;
            Block c = cols[i % cols.length];
            b.fill(x, g + 1, z, x, g + 2, z + 5, d(c));
            b.fill(x + 4, g + 1, z, x + 4, g + 2, z + 5, d(c));
            b.fill(x + 1, g + 3, z, x + 3, g + 3, z + 5, d(c));
            b.fill(x + 1, g + 2, z, x + 3, g + 2, z + 5, d(Blocks.AIR));
            b.clear(x + 1, g + 1, z, x + 3, g + 2, z + 5);
            b.set(x + 2, g + 1, z + 2, Bk.bedPart(Blocks.RED_BED, Direction.SOUTH, false));
            b.set(x + 2, g + 1, z + 3, Bk.bedPart(Blocks.RED_BED, Direction.SOUTH, true));
        }
        b.set(cx, g, cz, d(Blocks.CAMPFIRE));
        b.chest(cx + 2, g + 1, cz, Direction.WEST, "ruina");
        for (int i = 0; i < 3; i++) {
            b.spawn(cx + 3 - i * 3, g + 1, cz + 4, Mob.SOLDADO, 0);
        }
    }

    // ------------------------------------------------------------------ Cavernas com licantropos
    public static void lair(Blueprint b, Site s, boolean cold) {
        int cx = s.x(), cz = s.z(), g = s.level();
        Lay.clearDisc(b, cx, cz, 12, g, 14);
        // arco de entrada
        b.fill(cx - 3, g + 1, cz - 2, cx + 3, g + 5, cz + 1, d(Blocks.COBBLED_DEEPSLATE));
        b.clear(cx - 1, g + 1, cz - 2, cx + 1, g + 3, cz + 1);
        b.fill(cx - 2, g + 1, cz - 2, cx - 2, g + 4, cz - 2, d(Blocks.STONE_BRICKS));
        b.fill(cx + 2, g + 1, cz - 2, cx + 2, g + 4, cz - 2, d(Blocks.STONE_BRICKS));
        b.set(cx - 2, g + 3, cz - 3, Bk.wallTorch(Direction.NORTH));
        // túnel escada descendo para o sul
        int len = 26;
        for (int i = 0; i < len; i++) {
            int y = g - i;
            int z = cz + i;
            b.clear(cx - 1, y + 1, z, cx + 1, y + 4, z);
            b.fill(cx - 1, y, z, cx + 1, y, z, Bk.stairs(Blocks.COBBLED_DEEPSLATE_STAIRS, Direction.NORTH, false));
            if (i % 6 == 3) {
                b.set(cx - 2, y + 2, z, Bk.wallTorch(Direction.EAST));
            }
        }
        // câmara
        int gy = g - len, gz = cz + len + 6;
        b.disc(cx, gy - 1, gz, 12, d(cold ? Blocks.PACKED_ICE : Blocks.DEEPSLATE));
        for (int y = gy; y <= gy + 7; y++) {
            int rr = y < gy + 5 ? 11 : 11 - (y - gy - 4) * 2;
            b.disc(cx, y, gz, rr, d(Blocks.AIR));
        }
        b.clear(cx - 1, gy, gz - 12, cx + 1, gy + 3, gz - 5);
        b.disc(cx, gy, gz, 4, d(Blocks.AIR));
        for (int i = 0; i < 10; i++) {
            double a = i * 0.9;
            b.set(cx + (int) Math.round(Math.cos(a) * (3 + i % 6)), gy, gz + (int) Math.round(Math.sin(a) * (3 + i % 6)), d(Blocks.COBWEB));
        }
        b.set(cx + 5, gy, gz + 4, d(Blocks.BONE_BLOCK));
        b.set(cx - 5, gy, gz - 3, d(Blocks.BONE_BLOCK));
        b.chest(cx, gy, gz + 8, Direction.NORTH, "caverna");
        b.chest(cx - 6, gy, gz + 5, Direction.EAST, "caverna");
        for (int i = 0; i < 4; i++) {
            b.spawn(cx - 3 + i * 2, gy, gz + (i % 2) * 3, Mob.LICANTROPO, 0);
        }
        b.spawn(cx, gy, gz + 5, Mob.ADAPTADO, 0);
    }
}
