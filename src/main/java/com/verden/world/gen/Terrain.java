package com.verden.world.gen;

/** Função de terreno de Verden (pura, sem APIs do Minecraft): a partir do mapa + ruído, descreve cada coluna. */
public final class Terrain {
    public static final int SEA = VerdenMap.SEA_LEVEL;
    public static final int MIN_Y = -64;

    /** Descrição de uma coluna (x, z). */
    public static final class Column {
        /** Y do bloco sólido mais alto (fundo, se houver água por cima). */
        public int solid;
        /** Y da superfície da água (== solid se não houver água). */
        public int water;
        /** Região para decoração de superfície. */
        public Region surface;
        /** Região para o bioma. */
        public Region biome;
        public boolean river;
        /** Distância à costa em blocos (>0 terra, <0 mar), aproximada. */
        public double coast;
    }

    private final VerdenMap map = VerdenMap.get();
    private final FastNoise warp = new FastNoise(0x5eed1L);
    private final FastNoise warp2 = new FastNoise(0x5eed2L);
    private final FastNoise hills = new FastNoise(0x4111L);
    private final FastNoise ridge = new FastNoise(0x4112L);
    private final FastNoise rnoise = new FastNoise(0x4113L);

    public VerdenMap map() {
        return map;
    }

    private double wx(double bx, double bz) {
        return bx + warp.fbm(bx * 0.006, bz * 0.006, 3) * 22.0;
    }

    private double wz(double bx, double bz) {
        return bz + warp2.fbm(bx * 0.006 + 40, bz * 0.006 - 40, 3) * 22.0;
    }

    private int waterLevel(double bx, double bz) {
        double gx = Math.round(bx / 48.0) * 48.0, gz = Math.round(bz / 48.0) * 48.0;
        return Math.max(SEA, (int) Math.floor(map.height(gx, gz)) - 1);
    }

    public Column column(int bx, int bz) {
        Column c = new Column();
        double x = wx(bx, bz), z = wz(bx, bz);
        double hm = map.height(x, z);
        Region reg = Region.of(map.region(x, z));
        c.surface = reg;
        c.biome = reg;
        double h;
        if (reg.isOcean() || hm < SEA - 0.5) {
            h = hm + hills.fbm(bx * 0.02, bz * 0.02, 3) * 2.5;
            c.coast = hm - SEA;
            c.solid = (int) Math.floor(h);
            c.water = SEA;
            if (c.solid >= SEA) {          // evita ilhotas de 1 bloco no mar
                c.solid = SEA - 1;
            }
            return c;
        }
        double above = hm - SEA;
        double rough = reg.roughness() * Math.min(1.0, Math.max(0.12, above / 6.0));
        double d = hills.fbm(bx * 0.011, bz * 0.011, 4) * rough + hills.noise(bx * 0.05, bz * 0.05) * rough * 0.22;
        if (reg == Region.FROZEN_PEAKS || reg == Region.STONY_PEAKS) {
            double k = Math.min(1.0, Math.max(0.0, (hm - 78.0) / 40.0));
            d += (ridge.ridged(bx * 0.0055, bz * 0.0055, 4) - 0.35) * 55.0 * k;
        } else if (reg == Region.DESERT || reg == Region.BADLANDS) {
            d += (ridge.ridged(bx * 0.012, bz * 0.012, 2) - 0.3) * 9.0;
        } else if (reg == Region.JUNGLE) {
            d += ridge.ridged(bx * 0.008, bz * 0.008, 3) * 14.0 * Math.min(1.0, above / 12.0);
        }
        h = hm + d;
        c.coast = above;
        c.solid = (int) Math.floor(h);
        c.water = c.solid;
        // plataforma plana sob cidades e marcos
        double fw = 0;
        int flevel = 0;
        for (Sites.Site s : Sites.all()) {
            double dx = bx - s.x(), dz = bz - s.z();
            double rr = s.radius() + 10.0;
            double d2 = dx * dx + dz * dz;
            if (d2 > (rr + 30) * (rr + 30)) {
                continue;
            }
            double dd = Math.sqrt(d2);
            double t = dd <= rr ? 1.0 : 1.0 - Math.min(1.0, (dd - rr) / 30.0);
            t = t * t * (3 - 2 * t);
            if (t > fw) {
                fw = t;
                flevel = s.level();
            }
        }
        if (fw > 0) {
            c.solid = (int) Math.round(c.solid * (1 - fw) + flevel * fw);
            c.water = c.solid;
            if (fw > 0.6) {
                c.solid = flevel;
                c.water = flevel;
                return c;
            }
        }
        // rios e lagos
        int hw = map.riverHalfWidth(x, z);
        if (hw > 0) {
            double rdist = map.riverDistance(x, z) + rnoise.noise(bx * 0.05, bz * 0.05) * 3.0;
            if (rdist < hw + 6) {
                int wl = waterLevel(bx, bz);
                if (rdist < hw) {
                    double t = rdist / hw;
                    int depth = 1 + (int) Math.round(3.0 * (1.0 - t * t));
                    c.solid = wl - depth;
                    c.water = wl;
                    c.river = true;
                    c.surface = Region.RIVER;
                    c.biome = reg.isCold() ? Region.FROZEN_RIVER : Region.RIVER;
                } else {
                    int bank = wl + 1 + (int) Math.floor((rdist - hw) * 0.6);
                    if (c.solid > bank) {
                        c.solid = bank;
                        c.water = bank;
                    }
                }
            }
        }
        if (c.solid < SEA && !c.river) {
            c.water = SEA;       // depressões costeiras alagadas
        }
        return c;
    }

    /** Região de bioma na posição (bx, bz), consistente com {@link #column}. */
    public Region biomeAt(int bx, int bz) {
        double x = wx(bx, bz), z = wz(bx, bz);
        Region reg = Region.of(map.region(x, z));
        if (reg.isOcean()) {
            return reg;
        }
        int hw = map.riverHalfWidth(x, z);
        if (hw > 0 && map.riverDistance(x, z) < hw + 1.5) {
            return reg.isCold() ? Region.FROZEN_RIVER : Region.RIVER;
        }
        return reg;
    }
}
