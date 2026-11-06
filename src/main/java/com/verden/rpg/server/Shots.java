package com.verden.rpg.server;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;

/** Planos de câmera (posição e alvo no início e no fim, duração). Interpolados no cliente a cada quadro. */
final class Shots {
    private Shots() {}

    record Shot(double[] p0, double[] p1, double[] t0, double[] t1, int ticks) {}

    private static double[] v(double x, double y, double z) { return new double[]{x, y, z}; }

    private static Shot sh(double[] a, double[] b, double[] ta, double[] tb, int t) { return new Shot(a, b, ta, tb, t); }

    // ilha: x leste, y ACIMA DO MAR, z para o interior (origem na costa norte real)
    private static double[] k(double x, double y, double z) { return v(KarBuilder.LX + x, KarBuilder.SEA + y, KarBuilder.cz + z); }

    // sala: relativo ao centro, y acima do piso
    private static double[] r(double x, double y, double z) { return v(KarBuilder.RX + 0.5 + x, KarBuilder.RY + 1 + y, KarBuilder.RZ + 0.5 + z); }

    static Shot get(String id, ServerWorld w, int zigAy) {
        int sx = Fase01.YSX, sz = Fase01.YSZ;
        int sy = Fase01.surface(w, sx, sz);
        KarBuilder.locate(w);
        int ax = RpgServer.AX, az = RpgServer.AZ;
        return switch (id) {
            case "kar_mar_aberto" -> sh(k(10, 34, -190), k(6, 24, -110), k(0, 40, 150), k(0, 30, 120), 240);
            case "kar_ilha_aproxima" -> sh(k(60, 18, -100), k(18, 9, -52), k(0, 45, 150), k(0, 6, 6), 240);
            case "kar_expedicao_praia" -> sh(k(-8, 6, -30), k(-3, 5, -18), k(0, 3, 4), k(0, 3, 4), 150);
            case "kar_navio_afasta" -> sh(k(0, 9, -28), k(0, 16, -130), k(0, 8, 20), k(0, 8, 20), 280);
            case "kar_praia_distante" -> sh(k(6, 14, -118), k(4, 14, -124), k(0, 3, 4), k(0, 3, 4), 220);
            case "rel_papel" -> sh(r(0.5, 2.0, -0.3), r(0.15, 1.95, -1.0), r(0, 1.6, -1.75), r(0, 1.6, -1.75), 150);
            case "rel_homem_lendo" -> sh(r(-2.4, 2.3, 1.8), r(-1.8, 2.15, 1.1), r(0, 2.3, -2.0), r(0, 2.3, -2.0), 160);
            case "rel_olhos" -> sh(r(0, 2.6, -0.6), r(0, 2.6, -1.2), r(0, 2.62, -2.0), r(0, 2.62, -2.0), 120);
            case "rel_mulher" -> sh(r(0.4, 1.0, -0.8), r(0.2, 1.0, 0.2), r(0, 1.0, 2.0), r(0, 1.0, 2.0), 140);
            case "rel_taca" -> sh(r(-0.9, 1.9, -0.9), r(-0.55, 1.85, -1.25), r(0.25, 1.55, -1.95), r(0.25, 1.55, -1.95), 120);
            case "rel_dois" -> sh(r(-6, 3.2, 6), r(-4.5, 2.6, 4.5), r(0, 1.3, 0), r(0, 1.3, 0), 200);
            case "rel_soldado" -> sh(r(0.8, 2.0, 0.6), r(1.8, 1.9, -0.4), r(3, 1.5, -2), r(3, 1.5, -2), 140);
            case "rel_saida" -> sh(r(0, 2.4, -2.5), r(0, 2.8, -4), r(0, 1.2, 5), r(0, 1.2, 6), 260);
            case "yet_aberto" -> sh(v(sx - 40, sy + 30, sz + 30), v(sx - 10, sy + 8, sz + 10), v(sx + 140, sy + 5, sz), v(sx + 140, sy + 5, sz), 200);
            case "yet_galope" -> sh(v(sx - 6, sy + 3, sz + 6), v(sx + 14, sy + 3, sz + 6), v(sx, sy + 2, sz), v(sx + 20, sy + 2, sz), 120);
            case "trono_porta" -> sh(v(ax + 0.5, zigAy + 2.5, az - 12), v(ax + 0.5, zigAy + 2.5, az - 10), v(ax + 0.5, zigAy + 2.5, az + 2), v(ax + 0.5, zigAy + 2.5, az + 2), 80);
            case "trono_amplo" -> sh(v(ax - 9, zigAy + 5, az - 6), v(ax - 3, zigAy + 3.5, az - 9), v(ax + 0.5, zigAy + 2, az + 2), v(ax + 0.5, zigAy + 2, az + 2), 160);
            case "saida_afasta" -> sh(v(ax - 3, zigAy + 3, az + 1), v(ax + 1, zigAy + 5, az - 10), v(ax - 13, zigAy + 2, az + 4), v(ax - 13, zigAy + 2, az + 4), 220);
            default -> null;
        };
    }
}
