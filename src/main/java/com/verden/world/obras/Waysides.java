package com.verden.world.obras;

import com.verden.world.gen.RoadNet;
import com.verden.world.gen.Sites;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.Terrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pontos de interesse à beira das estradas (estalagens, torres de vigia, santuários, postos abandonados). */
public final class Waysides {
    private Waysides() {}

    private static List<Site> all;

    private static int hash(int a, int b) {
        int h = a * 374761393 + b * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }

    /** Lista fixa (calculada uma vez a partir das estradas e do terreno). */
    public static synchronized List<Site> all(Terrain t) {
        if (all != null) {
            return all;
        }
        List<Site> out = new ArrayList<>();
        List<RoadNet.Road> roads = RoadNet.all();
        for (int ri = 0; ri < roads.size(); ri++) {
            RoadNet.Road r = roads.get(ri);
            int k = 0;
            for (double pos = 320; pos < r.length - 220; pos += 640, k++) {
                // longe dos marcos de milha (a cada 500, a partir de 18)
                double m = (pos - 18) % 500;
                if (m < 40 || m > 460) {
                    continue;
                }
                double[] p = r.at(pos);
                int side = (hash(ri, k) & 1) == 0 ? 1 : -1;
                int x = (int) Math.round(p[0] - p[3] * 14 * side), z = (int) Math.round(p[1] + p[2] * 14 * side);
                if (!suitable(t, x, z)) {
                    continue;
                }
                boolean near = false;
                for (Site s : Sites.all()) {
                    if (Math.hypot(s.x() - x, s.z() - z) < s.radius() + 70) {
                        near = true;
                        break;
                    }
                }
                for (Site o : out) {
                    if (Math.hypot(o.x() - x, o.z() - z) < 140) {
                        near = true;
                        break;
                    }
                }
                if (near) {
                    continue;
                }
                int h = hash(x, z) % 100;
                String kind = h < 35 ? "estalagem" : h < 60 ? "torre_vigia" : h < 80 ? "santuario" : "posto_abandonado";
                int level = t.column(x, z).solid;
                out.add(new Site("pe_" + ri + "_" + k, kind, kind, x, z, 10, level));
            }
        }
        all = Collections.unmodifiableList(out);
        return all;
    }

    private static boolean suitable(Terrain t, int x, int z) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                Terrain.Column c = t.column(x + dx, z + dz);
                if (c.water > c.solid || c.river) {
                    return false;
                }
                lo = Math.min(lo, c.solid);
                hi = Math.max(hi, c.solid);
            }
        }
        return lo >= Terrain.SEA + 1 && hi <= 110 && hi - lo <= 4;
    }
}
