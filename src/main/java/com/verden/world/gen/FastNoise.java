package com.verden.world.gen;

import java.util.Random;

/** Ruído de Perlin simples (2D/3D) com semente fixa — sem dependência de APIs do Minecraft. */
public final class FastNoise {
    private final int[] p = new int[512];

    public FastNoise(long seed) {
        int[] perm = new int[256];
        for (int i = 0; i < 256; i++) {
            perm[i] = i;
        }
        Random r = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = perm[i];
            perm[i] = perm[j];
            perm[j] = t;
        }
        for (int i = 0; i < 512; i++) {
            p[i] = perm[i & 255];
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /** Ruído 3D em aproximadamente [-1, 1]. */
    public double noise(double x, double y, double z) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = fade(x), v = fade(y), w = fade(z);
        int a = p[xi] + yi, aa = p[a] + zi, ab = p[a + 1] + zi;
        int b = p[xi + 1] + yi, ba = p[b] + zi, bb = p[b + 1] + zi;
        return lerp(w,
                lerp(v, lerp(u, grad(p[aa], x, y, z), grad(p[ba], x - 1, y, z)),
                        lerp(u, grad(p[ab], x, y - 1, z), grad(p[bb], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[aa + 1], x, y, z - 1), grad(p[ba + 1], x - 1, y, z - 1)),
                        lerp(u, grad(p[ab + 1], x, y - 1, z - 1), grad(p[bb + 1], x - 1, y - 1, z - 1))));
    }

    public double noise(double x, double z) {
        return noise(x, 0.37, z);
    }

    /** Soma de oitavas, normalizada para ~[-1, 1]. */
    public double fbm(double x, double z, int octaves) {
        double sum = 0, amp = 1, freq = 1, norm = 0;
        for (int i = 0; i < octaves; i++) {
            sum += noise(x * freq, 0.37 + i * 11.1, z * freq) * amp;
            norm += amp;
            amp *= 0.5;
            freq *= 2;
        }
        return sum / norm;
    }

    /** Ruído "crista" (picos agudos), em [0, 1]. */
    public double ridged(double x, double z, int octaves) {
        double sum = 0, amp = 1, freq = 1, norm = 0;
        for (int i = 0; i < octaves; i++) {
            double n = 1.0 - Math.abs(noise(x * freq, 3.7 + i * 7.3, z * freq)) * 1.6;
            n = Math.max(0, n);
            sum += n * n * amp;
            norm += amp;
            amp *= 0.5;
            freq *= 2;
        }
        return sum / norm;
    }
}
