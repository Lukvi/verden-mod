package com.verden.world.gen;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Dados do mapa de Verden (gerados por tools/map_gen.py): altura, bioma, distância a rios.
 * Escala: 1 pixel = {@link #SCALE} blocos; o pixel (originX, originY) é o bloco (0, 0).
 */
public final class VerdenMap {
    public static final int SCALE = 8;
    public static final int SEA_LEVEL = 63;
    private static VerdenMap instance;

    private final int w, h, ox, oy;
    private final byte[] height, cls, rd, rw;

    public static synchronized VerdenMap get() {
        if (instance == null) {
            try (InputStream in = VerdenMap.class.getResourceAsStream("/data/verden/map/verden_map.bin")) {
                if (in == null) {
                    throw new IllegalStateException("verden_map.bin não encontrado no jar do mod");
                }
                instance = new VerdenMap(new DataInputStream(in));
            } catch (IOException e) {
                throw new IllegalStateException("falha ao ler verden_map.bin", e);
            }
        }
        return instance;
    }

    private VerdenMap(DataInputStream in) throws IOException {
        byte[] magic = new byte[4];
        in.readFully(magic);
        this.w = in.readUnsignedShort();
        this.h = in.readUnsignedShort();
        int scale = in.readUnsignedShort();
        if (scale != SCALE) {
            throw new IOException("escala inesperada: " + scale);
        }
        this.ox = in.readUnsignedShort();
        this.oy = in.readUnsignedShort();
        int n = w * h;
        height = new byte[n];
        cls = new byte[n];
        rd = new byte[n];
        rw = new byte[n];
        in.readFully(height);
        in.readFully(cls);
        in.readFully(rd);
        in.readFully(rw);
    }

    private double mx(double bx) {
        return bx / SCALE + ox;
    }

    private double mz(double bz) {
        return bz / SCALE + oy;
    }

    private int idx(int x, int y) {
        x = Math.max(0, Math.min(w - 1, x));
        y = Math.max(0, Math.min(h - 1, y));
        return y * w + x;
    }

    private double bilinear(byte[] plane, double u, double v) {
        u -= 0.5;
        v -= 0.5;
        int x0 = (int) Math.floor(u), y0 = (int) Math.floor(v);
        double fx = u - x0, fy = v - y0;
        double a = plane[idx(x0, y0)] & 255, b = plane[idx(x0 + 1, y0)] & 255;
        double c = plane[idx(x0, y0 + 1)] & 255, d = plane[idx(x0 + 1, y0 + 1)] & 255;
        return (a + (b - a) * fx) * (1 - fy) + (c + (d - c) * fx) * fy;
    }

    private int nearest(byte[] plane, double u, double v) {
        return plane[idx((int) Math.floor(u), (int) Math.floor(v))] & 255;
    }

    /** Altura do terreno (ou do fundo do mar) em blocos, interpolada. */
    public double height(double bx, double bz) {
        return bilinear(height, mx(bx), mz(bz));
    }

    /** Id da região/bioma (índice de {@link Region}). */
    public int region(double bx, double bz) {
        return nearest(cls, mx(bx), mz(bz));
    }

    /** Distância (em blocos) ao eixo do rio/lago mais próximo; 510 = longe. */
    public double riverDistance(double bx, double bz) {
        return bilinear(rd, mx(bx), mz(bz)) * 2.0;
    }

    /** Meia-largura (em blocos) do rio mais próximo. */
    public int riverHalfWidth(double bx, double bz) {
        return nearest(rw, mx(bx), mz(bz));
    }

    public int pixelWidth() {
        return w;
    }

    public int pixelHeight() {
        return h;
    }
}
