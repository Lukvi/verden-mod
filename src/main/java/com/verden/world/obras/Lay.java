package com.verden.world.obras;

import com.verden.entity.NpcRole;
import com.verden.world.gen.Sites.Site;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Planos urbanos: vilas e capitais muradas. */
public final class Lay {
    private Lay() {}

    /** Casas de uma cidade: cantos e nível, para colocar NPCs dentro. */
    public static final class Homes {
        public final List<int[]> list = new ArrayList<>(); // x, z, g, w, d
    }

    /** Remove tudo (árvores, relevo) num disco, do chão para cima. */
    public static void clearDisc(Blueprint b, int cx, int cz, int r, int g, int h) {
        for (int dz = -r; dz <= r; dz++) {
            int hw = (int) Math.floor(Math.sqrt((double) r * r - (double) dz * dz));
            b.clear(cx - hw, g + 1, cz + dz, cx + hw, g + h, cz + dz);
        }
    }

    private static Parts.Shop shopAt(int n, int radius, boolean capital) {
        if (capital) {
            return switch (n % 6) {
                case 1 -> Parts.Shop.TAVERNA;
                case 2 -> Parts.Shop.FERREIRO;
                case 3 -> Parts.Shop.ALQUIMISTA;
                case 4 -> Parts.Shop.ARMEIRO;
                case 5 -> Parts.Shop.MERCEARIA;
                default -> null;
            };
        }
        return switch (n) {
            case 1 -> Parts.Shop.MERCEARIA;
            case 3 -> Parts.Shop.TAVERNA;
            case 5 -> radius >= 40 ? Parts.Shop.FERREIRO : null;
            case 7 -> radius >= 40 ? Parts.Shop.ALQUIMISTA : null;
            case 9 -> radius >= 44 ? Parts.Shop.ARMEIRO : null;
            default -> null;
        };
    }

    private static NpcRole roleOf(Parts.Shop t) {
        return switch (t) {
            case TAVERNA -> NpcRole.TAVERNEIRO;
            case FERREIRO -> NpcRole.FERREIRO;
            case ALQUIMISTA -> NpcRole.ALQUIMISTA;
            case ARMEIRO -> NpcRole.ARMEIRO;
            default -> NpcRole.MERCEIRO;
        };
    }

    /** Constrói uma casa comum ou, conforme o contador, uma loja com o comerciante dentro. */
    private static void build(Blueprint b, Pal p, Random r, Homes homes, int n, int radius, boolean capital,
                              int x, int z, int w, int d, int g, int floors, Direction front) {
        Parts.Shop t = shopAt(n, radius, capital);
        if (t == null) {
            Parts.house(b, p, r, x, z, w, d, g, floors, front);
            homes.list.add(new int[]{x, z, g, w, d});
        } else {
            int[] c = Parts.shop(b, p, r, x, z, w, d, g, front, t);
            b.spawn(c[0], g + 1, c[1], Blueprint.Mob.NPC, roleOf(t).ordinal());
        }
    }

    private static boolean free(List<int[]> rects, int x1, int z1, int x2, int z2) {
        for (int[] q : rects) {
            if (x1 <= q[2] + 1 && x2 >= q[0] - 1 && z1 <= q[3] + 1 && z2 >= q[1] - 1) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ vila
    public static Homes town(Blueprint b, Site s, Pal p, Random r) {
        Homes homes = new Homes();
        int g = s.level(), cx = s.x(), cz = s.z(), R = s.radius();
        clearDisc(b, cx, cz, R + 2, g, 30);
        // praça e ruas
        b.disc(cx, g, cz, 7, Bk.d(p.path2));
        b.fill(cx - R + 4, g, cz - 1, cx + R - 4, g, cz + 1, Bk.d(p.path));
        b.fill(cx - 1, g, cz - R + 4, cx + 1, g, cz + R - 4, Bk.d(p.path));
        Parts.well(b, cx, cz, g, p);
        Parts.stall(b, cx + 3, cz + 3, g, p, r);
        Parts.stall(b, cx - 7, cz - 6, g, p, r);
        for (int k = -R + 8; k <= R - 8; k += 8) {
            if (Math.abs(k) > 8) {
                Parts.lamp(b, cx + k, g, cz + 2, p);
                Parts.lamp(b, cx + 2, g, cz + k, p);
            }
        }
        List<int[]> rects = new ArrayList<>();
        int[] count = {0};
        // casas ao longo das ruas
        for (int axis = 0; axis < 2; axis++) {
            for (int side = -1; side <= 1; side += 2) {
                for (int dir = -1; dir <= 1; dir += 2) {
                    int off = 10;
                    while (true) {
                        Parts.Shop shop = shopAt(count[0], R, false);
                        int w = 6 + r.nextInt(3), d = 6 + r.nextInt(3);
                        if (shop == Parts.Shop.TAVERNA) {
                            w = 10;
                            d = 8;
                        }
                        int far = off + (axis == 0 ? w : w) + 2;
                        if (far > R - 6) {
                            break;
                        }
                        int x, z, ww, dd;
                        Direction front;
                        if (axis == 0) { // rua ao longo de x
                            ww = w; dd = d;
                            x = dir > 0 ? cx + off : cx - off - w + 1;
                            z = side > 0 ? cz + 3 : cz - 3 - d + 1;
                            front = side > 0 ? Direction.NORTH : Direction.SOUTH;
                        } else {
                            ww = w; dd = d;
                            z = dir > 0 ? cz + off : cz - off - d + 1;
                            x = side > 0 ? cx + 3 : cx - 3 - w + 1;
                            front = side > 0 ? Direction.WEST : Direction.EAST;
                        }
                        double fx = Math.max(Math.abs(x - cx), Math.abs(x + ww - 1 - cx));
                        double fz = Math.max(Math.abs(z - cz), Math.abs(z + dd - 1 - cz));
                        if (Math.hypot(fx, fz) <= R - 4 && free(rects, x, z, x + ww - 1, z + dd - 1)) {
                            rects.add(new int[]{x, z, x + ww - 1, z + dd - 1});
                            build(b, p, r, homes, count[0], R, false, x, z, ww, dd, g, r.nextInt(4) == 0 ? 2 : 1, front);
                            count[0]++;
                        }
                        off += (axis == 0 ? w : w) + 2 + r.nextInt(2);
                    }
                }
            }
        }
        // plantações nos quadrantes
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int fx = cx + sx * (R / 2), fz = cz + sz * (R / 2);
                int x1 = fx - 4, z1 = fz - 4;
                if (Math.hypot(Math.abs(fx - cx) + 5, Math.abs(fz - cz) + 5) <= R - 2 && free(rects, x1 - 1, z1 - 1, x1 + 9, z1 + 9)) {
                    Parts.farm(b, x1, z1, g, 9, 9, r);
                    rects.add(new int[]{x1 - 1, z1 - 1, x1 + 9, z1 + 9});
                }
            }
        }
        return homes;
    }

    // ------------------------------------------------------------------ capital murada
    public static Homes capital(Blueprint b, Site s, Pal p, Pal noble, Random r, int wallR, int wallH, int inner) {
        Homes homes = new Homes();
        int g = s.level(), cx = s.x(), cz = s.z();
        clearDisc(b, cx, cz, wallR + 8, g, 40);
        b.disc(cx, g, cz, wallR, Bk.d(p.path));
        // avenidas
        b.fill(cx - wallR - 2, g, cz - 3, cx + wallR + 2, g, cz + 3, Bk.d(p.path2));
        b.fill(cx - 3, g, cz - wallR - 2, cx + 3, g, cz + wallR + 2, Bk.d(p.path2));
        // estrada anelar
        b.ring(cx, g, g, cz, inner - 4, Bk.d(p.path2));
        b.ring(cx, g, g, cz, inner - 3, Bk.d(p.path2));
        // quarteirões
        int[] shopN = {0};
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 7; j++) {
                        int ox = 5 + 16 * i, oz = 5 + 16 * j;
                        double mx = ox + 8, mz = oz + 8;
                        double dist = Math.hypot(ox + (sx > 0 ? 15 : 0) * 0 + 8, oz + 8);
                        double far = Math.hypot(ox + 15, oz + 15);
                        if (far > wallR - 6 || Math.hypot(ox, oz) < inner) {
                            continue;
                        }
                        int x1 = sx > 0 ? cx + ox : cx - ox - 15;
                        int z1 = sz > 0 ? cz + oz : cz - oz - 15;
                        int w = 8 + r.nextInt(3), d = 7 + r.nextInt(3);
                        if (shopAt(shopN[0], wallR, true) == Parts.Shop.TAVERNA) {
                            w = 11;
                            d = 9;
                        }
                        int hx = x1 + 3, hz = z1 + 3;
                        Direction front;
                        if (ox < oz) {
                            front = sx > 0 ? Direction.WEST : Direction.EAST;
                        } else {
                            front = sz > 0 ? Direction.NORTH : Direction.SOUTH;
                        }
                        Pal pp = (i + j) % 5 == 0 ? noble : p;
                        build(b, pp, r, homes, shopN[0]++, wallR, true, hx, hz, w, d, g, (i + j) % 3 == 0 ? 2 : 1, front);
                        if (i == 0 || j == 0) {
                            Parts.lamp(b, x1 + 1, g, z1 + 1, p);
                        }
                    }
                }
            }
        }
        // lampiões nas avenidas
        for (int k = inner + 2; k < wallR - 6; k += 10) {
            for (int sg = -1; sg <= 1; sg += 2) {
                Parts.lamp(b, cx + sg * k, g, cz + 4, p);
                Parts.lamp(b, cx + 4, g, cz + sg * k, p);
            }
        }
        // muralha
        for (int rr = wallR - 2; rr <= wallR; rr++) {
            b.ring(cx, g - 6, g + wallH, cz, rr, p.wallS());
        }
        b.ring(cx, g + wallH, g + wallH, cz, wallR - 1, Bk.d(p.floor));
        int n = (int) Math.round(2 * Math.PI * wallR / 2.0);
        for (int i = 0; i < n; i += 2) {
            double a = 2 * Math.PI * i / n;
            b.set(cx + (int) Math.round(Math.cos(a) * wallR), g + wallH + 1, cz + (int) Math.round(Math.sin(a) * wallR), Bk.d(p.foundation));
        }
        // portões
        for (int dir = 0; dir < 4; dir++) {
            int dx = new int[]{1, -1, 0, 0}[dir], dz = new int[]{0, 0, 1, -1}[dir];
            int gx = cx + dx * wallR, gz = cz + dz * wallR;
            if (dx != 0) {
                b.clear(gx - 3, g + 1, gz - 3, gx + 3, g + 7, gz + 3);
                b.fill(gx - 3, g + 8, gz - 4, gx + 3, g + 9, gz + 4, p.wallS());
                b.fill(gx - 3, g + 1, gz - 4, gx + 3, g + wallH + 6, gz - 4, Bk.pillar(p.pillar, Direction.Axis.Y));
                b.fill(gx - 3, g + 1, gz + 4, gx + 3, g + wallH + 6, gz + 4, Bk.pillar(p.pillar, Direction.Axis.Y));
                b.set(gx + dx * 4, g + 7, gz - 4, Bk.banner(p.banner, 0));
            } else {
                b.clear(gx - 3, g + 1, gz - 3, gx + 3, g + 7, gz + 3);
                b.fill(gx - 4, g + 8, gz - 3, gx + 4, g + 9, gz + 3, p.wallS());
                b.fill(gx - 4, g + 1, gz - 3, gx - 4, g + wallH + 6, gz + 3, Bk.pillar(p.pillar, Direction.Axis.Y));
                b.fill(gx + 4, g + 1, gz - 3, gx + 4, g + wallH + 6, gz + 3, Bk.pillar(p.pillar, Direction.Axis.Y));
            }
            b.spawn(gx - dx * 5 + (dz != 0 ? 2 : 0), g + 1, gz - dz * 5 + (dx != 0 ? 2 : 0), Blueprint.Mob.SOLDADO, 0);
            b.spawn(gx - dx * 5 - (dz != 0 ? 2 : 0), g + 1, gz - dz * 5 - (dx != 0 ? 2 : 0), Blueprint.Mob.SOLDADO, 0);
        }
        // torres
        int towers = 4 * (int) Math.ceil(2 * Math.PI * wallR / 28.0 / 4.0);
        for (int k = 0; k < towers; k++) {
            double a = 2 * Math.PI * (k + 0.5) / towers;
            int tx = cx + (int) Math.round(Math.cos(a) * (wallR - 1)), tz = cz + (int) Math.round(Math.sin(a) * (wallR - 1));
            Parts.roundTower(b, tx, tz, g - 4, 4, wallH + 9, p.wallS(), Bk.d(p.foundation));
            Parts.cone(b, tx, g + wallH + 6, tz, 4, Bk.d(p.roofBlock));
        }
        return homes;
    }
}
