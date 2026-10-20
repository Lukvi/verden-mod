package com.verden.world.obras;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

import java.util.Random;

/** Peças de construção reutilizáveis: casas, telhados, torres, domos, poços... Coordenadas absolutas; g = topo do chão. */
public final class Parts {
    private Parts() {}

    // ------------------------------------------------------------------ telhados
    /** Telhado de duas águas. ridgeAlongX: cumeeira paralela ao eixo x. y = altura do topo da parede. */
    public static void gable(Blueprint b, Pal p, int x, int z, int w, int d, int y, boolean ridgeAlongX) {
        Block stairs = p.roofStairs, full = p.roofBlock;
        if (ridgeAlongX) {
            for (int k = 0; ; k++) {
                int lo = z - 1 + k, hi = z + d - k;
                if (lo > hi) {
                    break;
                }
                int yy = y + k;
                if (lo == hi) {
                    b.fill(x - 1, yy, lo, x + w, yy, lo, Bk.d(full));
                } else {
                    b.fill(x - 1, yy, lo, x + w, yy, lo, Bk.stairs(stairs, Direction.SOUTH, false));
                    b.fill(x - 1, yy, hi, x + w, yy, hi, Bk.stairs(stairs, Direction.NORTH, false));
                    if (k >= 1) {
                        b.fill(x, yy, lo + 1, x, yy, hi - 1, p.wallS());
                        b.fill(x + w - 1, yy, lo + 1, x + w - 1, yy, hi - 1, p.wallS());
                    }
                }
            }
        } else {
            for (int k = 0; ; k++) {
                int lo = x - 1 + k, hi = x + w - k;
                if (lo > hi) {
                    break;
                }
                int yy = y + k;
                if (lo == hi) {
                    b.fill(lo, yy, z - 1, lo, yy, z + d, Bk.d(full));
                } else {
                    b.fill(lo, yy, z - 1, lo, yy, z + d, Bk.stairs(stairs, Direction.EAST, false));
                    b.fill(hi, yy, z - 1, hi, yy, z + d, Bk.stairs(stairs, Direction.WEST, false));
                    if (k >= 1) {
                        b.fill(lo + 1, yy, z, hi - 1, yy, z, p.wallS());
                        b.fill(lo + 1, yy, z + d - 1, hi - 1, yy, z + d - 1, p.wallS());
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ casas
    /**
     * Casa retangular. (x, z) = canto de menor coordenada; w x d = tamanho; g = nível do chão;
     * front = lado da porta; floors = 1 ou 2 andares.
     */
    public static void house(Blueprint b, Pal p, Random r, int x, int z, int w, int d, int g, int floors, Direction front) {
        int h = 4;
        int top = g + floors * h;
        int x2 = x + w - 1, z2 = z + d - 1;
        // piso e interior livre
        b.fill(x, g - 8, z, x2, g - 1, z2, Bk.d(p.foundation));
        b.fill(x, g, z, x2, g, z2, Bk.d(p.floor));
        b.clear(x + 1, g + 1, z + 1, x2 - 1, top - 1, z2 - 1);
        // paredes
        b.walls(x, g + 1, z, x2, top, z2, p.wallS());
        b.walls(x, g + 1, z, x2, g + 1, z2, Bk.d(p.foundation));
        // vigas de canto
        BlockState corner = Bk.pillar(p.corner, Direction.Axis.Y);
        b.fill(x, g + 1, z, x, top, z, corner);
        b.fill(x2, g + 1, z, x2, top, z, corner);
        b.fill(x, g + 1, z2, x, top, z2, corner);
        b.fill(x2, g + 1, z2, x2, top, z2, corner);
        // faixa de madeira entre os andares
        for (int f = 1; f <= floors; f++) {
            int yy = g + f * h;
            b.walls(x, yy, z, x2, yy, z2, Bk.pillar(p.corner, Direction.Axis.X));
            b.fill(x, yy, z, x, yy, z2, Bk.pillar(p.corner, Direction.Axis.Z));
            b.fill(x2, yy, z, x2, yy, z2, Bk.pillar(p.corner, Direction.Axis.Z));
        }
        // piso do 2º andar com escada de mão
        if (floors == 2) {
            int yy = g + h;
            b.fill(x + 1, yy, z + 1, x2 - 1, yy, z2 - 1, Bk.d(p.floor));
            b.clear(x + 1, yy, z + 1, x + 1, yy, z + 1);
            b.fill(x + 1, g + 1, z + 1, x + 1, g + h + 1, z + 1, Bk.ladder(Direction.EAST));
            b.fill(x, g + 1, z + 1, x, g + h + 1, z + 1, p.wallS());
        }
        // janelas
        for (int f = 0; f < floors; f++) {
            int wy = g + 2 + f * h;
            for (int i = x + 2; i <= x2 - 2; i += 3) {
                b.set(i, wy, z, Bk.pane(p.glass, true));
                b.set(i, wy, z2, Bk.pane(p.glass, true));
            }
            for (int i = z + 2; i <= z2 - 2; i += 3) {
                b.set(x, wy, i, Bk.pane(p.glass, false));
                b.set(x2, wy, i, Bk.pane(p.glass, false));
            }
        }
        // porta
        int dx, dz;
        Direction out = front;
        switch (front) {
            case NORTH -> { dx = x + w / 2; dz = z; }
            case SOUTH -> { dx = x + w / 2; dz = z2; }
            case WEST -> { dx = x; dz = z + d / 2; }
            default -> { dx = x2; dz = z + d / 2; }
        }
        b.set(dx, g + 1, dz, Bk.door(p.door, out, false));
        b.set(dx, g + 2, dz, Bk.door(p.door, out, true));
        b.set(dx + out.getOffsetX(), g, dz + out.getOffsetZ(), Bk.d(p.path));
        b.set(dx + out.getOffsetX() * 2, g, dz + out.getOffsetZ() * 2, Bk.d(p.path));
        // luminária ao lado da porta
        int sx = out.getAxis() == Direction.Axis.Z ? dx + 1 : dx;
        int sz = out.getAxis() == Direction.Axis.X ? dz + 1 : dz;
        b.set(sx + out.getOffsetX(), g + 3, sz + out.getOffsetZ(), Bk.wallTorch(out));
        // telhado
        if (p.flatRoof) {
            b.fill(x - 1, top + 1, z - 1, x2 + 1, top + 1, z2 + 1, Bk.d(p.roofSlab));
            b.walls(x - 1, top + 1, z - 1, x2 + 1, top + 1, z2 + 1, Bk.d(p.roofBlock));
            b.fill(x, top, z, x2, top, z2, Bk.d(p.roofBlock));
        } else {
            b.fill(x, top, z, x2, top, z2, Bk.d(p.floor));
            gable(b, p, x, z, w, d, top, w >= d);
        }
        // interior: lanterna, cama, baú
        b.set(x + w / 2, g + h - 1, z + d / 2, Bk.lantern(true));
        b.set(x + w / 2, g + h, z + d / 2, Bk.d(p.floor));
        int bx = x + 2, bz = z2 - 1;
        if (bx + 1 <= x2 - 1 && front != Direction.SOUTH) {
            b.set(bx, g + 1, bz, Bk.bedPart(Blocks.RED_BED, Direction.EAST, false));
            b.set(bx + 1, g + 1, bz, Bk.bedPart(Blocks.RED_BED, Direction.EAST, true));
        }
        if (r.nextInt(3) == 0 && x2 - 2 > x + 3) {
            b.chest(x2 - 1, g + 1, z + 1, Direction.WEST, "casa");
        } else {
            b.set(x2 - 1, g + 1, z + 1, Bk.d(Blocks.CRAFTING_TABLE));
        }
    }

    // ------------------------------------------------------------------ torres e cúpulas
    /** Torre redonda com ameias. y0 = nível do chão; h = altura das paredes. */
    public static void roundTower(Blueprint b, int cx, int cz, int g, int rad, int h, BlockState wall, BlockState top) {
        b.ring(cx, g + 1, g + h, cz, rad, wall);
        b.disc(cx, g, cz, rad, wall);
        // ameias
        int n = Math.max(12, rad * 6);
        for (int i = 0; i < n; i += 2) {
            double a = 2 * Math.PI * i / n;
            int x = cx + (int) Math.round(Math.cos(a) * (rad + 1)), z = cz + (int) Math.round(Math.sin(a) * (rad + 1));
            b.set(x, g + h + 1, z, top);
        }
        b.ring(cx, g + h + 1, g + h + 1, cz, rad + 1, top);
        b.disc(cx, g + h, cz, rad, wall);
        // seteiras
        b.set(cx + rad, g + h - 3, cz, Bk.AIR);
        b.set(cx - rad, g + h - 3, cz, Bk.AIR);
        b.set(cx, g + h - 3, cz + rad, Bk.AIR);
        b.set(cx, g + h - 3, cz - rad, Bk.AIR);
    }

    /** Cone (telhado de torre). */
    public static void cone(Blueprint b, int cx, int y, int cz, int rad, BlockState s) {
        for (int i = 0; rad - i >= 0; i++) {
            b.disc(cx, y + i, cz, rad - i, s);
        }
        b.set(cx, y + rad + 1, cz, s);
    }

    /** Calota esférica (domo). */
    public static void dome(Blueprint b, int cx, int y, int cz, int rad, BlockState s) {
        for (int i = 0; i <= rad; i++) {
            double rr = Math.sqrt((double) rad * rad - (double) i * i);
            b.disc(cx, y + i, cz, (int) Math.round(rr), s);
        }
    }

    /** Escada simples de pedra de largura w que sobe na direção dir. */
    public static void stairsUp(Blueprint b, Block stair, int x, int y, int z, Direction dir, int steps, int width, boolean alongX) {
        for (int i = 0; i < steps; i++) {
            int px = x + dir.getOffsetX() * i, pz = z + dir.getOffsetZ() * i;
            int px2 = alongX ? px + width - 1 : px, pz2 = alongX ? pz : pz + width - 1;
            b.fill(px, y + i, pz, px2, y + i, pz2, Bk.stairs(stair, dir, false));
        }
    }

    // ------------------------------------------------------------------ mobiliário urbano
    public static void well(Blueprint b, int cx, int cz, int g, Pal p) {
        b.fill(cx - 1, g, cz - 1, cx + 1, g, cz + 1, Bk.d(p.foundation));
        b.fill(cx - 1, g + 1, cz - 1, cx + 1, g + 1, cz + 1, Bk.d(Blocks.COBBLESTONE));
        b.set(cx, g + 1, cz, Bk.d(Blocks.WATER));
        b.set(cx, g, cz, Bk.d(Blocks.WATER));
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                b.fill(cx + sx, g + 2, cz + sz, cx + sx, g + 3, cz + sz, Bk.d(Blocks.OAK_FENCE));
            }
        }
        b.fill(cx - 1, g + 4, cz - 1, cx + 1, g + 4, cz + 1, Bk.d(p.roofSlab));
    }

    public static void lamp(Blueprint b, int x, int g, int z, Pal p) {
        b.fill(x, g + 1, z, x, g + 3, z, Bk.d(Blocks.OAK_FENCE));
        b.set(x, g + 4, z, Bk.d(p.light == Blocks.SOUL_LANTERN ? Blocks.SOUL_LANTERN : Blocks.LANTERN));
    }

    public static void stall(Blueprint b, int x, int z, int g, Pal p, Random r) {
        Block[] cloth = {Blocks.RED_WOOL, Blocks.WHITE_WOOL, Blocks.BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.GREEN_WOOL};
        Block c = cloth[r.nextInt(cloth.length)];
        for (int dx : new int[]{0, 4}) {
            for (int dz : new int[]{0, 3}) {
                b.fill(x + dx, g + 1, z + dz, x + dx, g + 3, z + dz, Bk.d(Blocks.OAK_FENCE));
            }
        }
        b.fill(x, g + 4, z, x + 4, g + 4, z + 3, Bk.d(c));
        b.fill(x + 1, g + 1, z + 1, x + 3, g + 1, z + 1, Bk.d(Blocks.OAK_PLANKS));
        b.set(x + 2, g + 2, z + 1, Bk.d(r.nextBoolean() ? Blocks.MELON : Blocks.HAY_BLOCK));
        b.set(x + 1, g + 2, z + 1, Bk.d(Blocks.BARREL));
    }

    public static void farm(Blueprint b, int x, int z, int g, int w, int d, Random r) {
        b.fill(x, g, z, x + w - 1, g, z + d - 1, Bk.d(Blocks.FARMLAND));
        b.clear(x, g + 1, z, x + w - 1, g + 3, z + d - 1);
        for (int i = x; i < x + w; i++) {
            for (int j = z; j < z + d; j++) {
                if (i == x + w / 2) {
                    b.set(i, g, j, Bk.d(Blocks.WATER));
                } else {
                    Block c = switch (r.nextInt(3)) { case 0 -> Blocks.WHEAT; case 1 -> Blocks.CARROTS; default -> Blocks.POTATOES; };
                    b.set(i, g + 1, j, c.getDefaultState());
                }
            }
        }
        b.walls(x - 1, g, z - 1, x + w, g, z + d, Bk.d(Blocks.OAK_LOG));
    }

    public static void banner(Blueprint b, int x, int g, int z, Pal p, int height) {
        b.fill(x, g + 1, z, x, g + height, z, Bk.d(Blocks.OAK_FENCE));
        b.set(x, g + height + 1, z, Bk.banner(p.banner, 0));
    }

    /** Pirâmide escalonada de blocos (telhados de torre quadrada). */
    public static void pyramid(Blueprint b, int cx, int y, int cz, int half, int layers, BlockState s) {
        for (int i = 0; i < layers && half - i >= 0; i++) {
            int hh = half - i;
            b.fill(cx - hh, y + i, cz - hh, cx + hh, y + i, cz + hh, s);
        }
    }
}
