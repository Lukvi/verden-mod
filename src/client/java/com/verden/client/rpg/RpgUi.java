package com.verden.client.rpg;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Peças visuais compartilhadas: moldura dourada sobre fundo escuro, ícones 16x16 e retratos 64x64. */
final class RpgUi {
    private RpgUi() {}

    static final int GOLD = 0xFFE8C45A, WHITE = 0xFFFFFFFF, GREY = 0xFFB8B0A0, DIM = 0xFF807868, GREEN = 0xFF78DC78, RED = 0xFFD84848;

    static Identifier tex(String path) {
        return Identifier.of("verden", "textures/gui/" + path + ".png");
    }

    static void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x, y, x + w, y + h, 0xE0181410);
        c.drawBorder(x, y, w, h, 0xFFC8A25A);
        c.drawBorder(x + 2, y + 2, w - 4, h - 4, 0x60C8A25A);
    }

    static void icon(DrawContext c, String name, int x, int y, int size) {
        c.drawTexture(tex("icon/" + name), x, y, size, size, 0f, 0f, 16, 16, 16, 16);
    }

    static void portrait(DrawContext c, String id, int x, int y, int size) {
        c.drawTexture(tex("portrait/" + id), x, y, size, size, 0f, 0f, 64, 64, 64, 64);
    }

    static void bar(DrawContext c, int x, int y, int w, int h, float frac, int color) {
        c.fill(x, y, x + w, y + h, 0xFF000000);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF302820);
        int fw = Math.round((w - 2) * Math.max(0f, Math.min(1f, frac)));
        if (fw > 0) c.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
    }

    static String pretty(String id) {
        String s = id.replace('_', ' ');
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
