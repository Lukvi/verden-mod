package com.verden.world.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Rede de estradas de Verden (roads.json, gerado por tools/roads_gen.py). Código puro, sem APIs do Minecraft. */
public final class RoadNet {
    private RoadNet() {}

    public static final class Road {
        public final String a, b;
        public final int[] xs, zs;
        public final double[] cum;   // comprimento acumulado até cada ponto
        public final double length;

        Road(String a, String b, int[] xs, int[] zs) {
            this.a = a;
            this.b = b;
            this.xs = xs;
            this.zs = zs;
            this.cum = new double[xs.length];
            for (int i = 1; i < xs.length; i++) {
                cum[i] = cum[i - 1] + Math.hypot(xs[i] - xs[i - 1], zs[i] - zs[i - 1]);
            }
            this.length = cum[xs.length - 1];
        }

        /** Ponto na posição t (0..length) com a direção unitária da estrada ali: {x, z, dx, dz}. */
        public double[] at(double t) {
            int i = 1;
            while (i < xs.length - 1 && cum[i] < t) {
                i++;
            }
            double seg = Math.max(1e-6, cum[i] - cum[i - 1]);
            double f = Math.min(1, Math.max(0, (t - cum[i - 1]) / seg));
            double dx = (xs[i] - xs[i - 1]) / seg, dz = (zs[i] - zs[i - 1]) / seg;
            return new double[]{xs[i - 1] + (xs[i] - xs[i - 1]) * f, zs[i - 1] + (zs[i] - zs[i - 1]) * f, dx, dz};
        }
    }

    private static final int CELL = 32;
    private static List<Road> roads;
    private static Map<Long, List<int[]>> index;

    private static long key(int cx, int cz) {
        return ((long) cx << 32) ^ (cz & 0xffffffffL);
    }

    public static synchronized List<Road> all() {
        if (roads == null) {
            List<Road> list = new ArrayList<>();
            try (InputStream in = RoadNet.class.getResourceAsStream("/data/verden/map/roads.json")) {
                if (in != null) {
                    JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    for (JsonElement e : root.getAsJsonArray("roads")) {
                        JsonObject o = e.getAsJsonObject();
                        JsonArray pts = o.getAsJsonArray("pts");
                        int[] xs = new int[pts.size()], zs = new int[pts.size()];
                        for (int i = 0; i < pts.size(); i++) {
                            xs[i] = pts.get(i).getAsJsonArray().get(0).getAsInt();
                            zs[i] = pts.get(i).getAsJsonArray().get(1).getAsInt();
                        }
                        if (xs.length >= 2) {
                            list.add(new Road(o.get("a").getAsString(), o.get("b").getAsString(), xs, zs));
                        }
                    }
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            Map<Long, List<int[]>> idx = new HashMap<>();
            for (int r = 0; r < list.size(); r++) {
                Road rd = list.get(r);
                for (int i = 0; i + 1 < rd.xs.length; i++) {
                    int x0 = Math.min(rd.xs[i], rd.xs[i + 1]), x1 = Math.max(rd.xs[i], rd.xs[i + 1]);
                    int z0 = Math.min(rd.zs[i], rd.zs[i + 1]), z1 = Math.max(rd.zs[i], rd.zs[i + 1]);
                    for (int cx = Math.floorDiv(x0 - 4, CELL); cx <= Math.floorDiv(x1 + 4, CELL); cx++) {
                        for (int cz = Math.floorDiv(z0 - 4, CELL); cz <= Math.floorDiv(z1 + 4, CELL); cz++) {
                            idx.computeIfAbsent(key(cx, cz), k -> new ArrayList<>()).add(new int[]{r, i});
                        }
                    }
                }
            }
            index = idx;
            roads = java.util.Collections.unmodifiableList(list);
        }
        return roads;
    }

    /** Há alguma estrada a menos de ~4 blocos desta área? */
    public static boolean near(int minX, int minZ, int maxX, int maxZ) {
        all();
        for (int cx = Math.floorDiv(minX, CELL); cx <= Math.floorDiv(maxX, CELL); cx++) {
            for (int cz = Math.floorDiv(minZ, CELL); cz <= Math.floorDiv(maxZ, CELL); cz++) {
                if (index.containsKey(key(cx, cz))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Distância (em blocos) ao eixo da estrada mais próxima; Double.MAX_VALUE se não há nenhuma por perto. */
    public static double distance(double x, double z) {
        all();
        List<int[]> segs = index.get(key(Math.floorDiv((int) Math.floor(x), CELL), Math.floorDiv((int) Math.floor(z), CELL)));
        if (segs == null) {
            return Double.MAX_VALUE;
        }
        double best = Double.MAX_VALUE;
        for (int[] s : segs) {
            Road r = roads.get(s[0]);
            int i = s[1];
            double ax = r.xs[i], az = r.zs[i], bx = r.xs[i + 1], bz = r.zs[i + 1];
            double vx = bx - ax, vz = bz - az;
            double l2 = vx * vx + vz * vz;
            double t = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / l2));
            double d = Math.hypot(x - (ax + vx * t), z - (az + vz * t));
            if (d < best) {
                best = d;
            }
        }
        return best;
    }
}
