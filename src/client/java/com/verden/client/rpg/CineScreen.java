package com.verden.client.rpg;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * Tela que hospeda a cutscene: captura todo o input (o jogador não anda nem interage), mantém o mundo visível,
 * move a câmera a cada quadro e desenha letterbox, fade, legendas, o título "VERDEN AF" e o selo de fim de fase.
 * Esc/Enter/Espaço pedem para pular (o servidor decide se a cena é pulável).
 */
public class CineScreen extends Screen {
    public CineScreen() {
        super(Text.literal("Cena"));
    }

    @Override public boolean shouldPause() { return false; }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override public void renderBackground(DrawContext c, int mx, int my, float delta) { }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if ((key == 256 || key == 257 || key == 32) && CineController.skippable) RpgClient.send("cine_skip");
        return true; // engole todo o resto: nada de abrir menus no meio da cena
    }

    @Override public boolean mouseClicked(double x, double y, int b) { return true; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        CineController.frame();
        long now = System.currentTimeMillis();
        TextRenderer tr = textRenderer;
        CineController.letter += (CineController.letterTarget - CineController.letter) * 0.08f;
        int bar = Math.round(height * 0.12f * CineController.letter);
        c.fill(0, 0, width, bar, 0xFF000000);
        c.fill(0, height - bar, width, height, 0xFF000000);
        // legenda
        if (!CineController.text.isEmpty() && now - CineController.lineStart < CineController.lineDur) {
            long age = now - CineController.lineStart;
            float a = Math.min(1f, Math.min(age / 350f, (CineController.lineDur - age) / 350f));
            int alpha = Math.max(8, (int) (255 * a)) << 24;
            int w = Math.min(width - 60, 520), x = (width - w) / 2, y = height - bar - 20;
            var lines = tr.wrapLines(Text.literal(CineController.text), w);
            y -= lines.size() * 11;
            boolean relato = CineController.speaker.startsWith("Relato");
            if (!CineController.speaker.isEmpty()) c.drawText(tr, CineController.speaker, x, y - 12, (relato ? 0xB8B0A0 : 0xE8C45A) | alpha, true);
            for (OrderedText l : lines) { c.drawText(tr, l, x, y, 0xFFFFFF | alpha, true); y += 11; }
        }
        // título
        if (CineController.titleId != null) {
            long age = now - CineController.titleStart;
            if (age <= CineController.titleDur) title(c, tr, age / (float) CineController.titleDur, now);
        }
        // fade (por cima de tudo)
        float fa = CineController.fadeAlpha(now);
        if (fa > 0.003f) c.fill(0, 0, width, height, (Math.max(1, Math.min(255, (int) (fa * 255))) << 24));
        if (CineController.skippable) c.drawText(tr, "[Esc] pular", width - 70, height - 12, 0x80808080, false);
    }

    private void title(DrawContext c, TextRenderer tr, float t, long now) {
        boolean fim = "fim_fase".equals(CineController.titleId);
        c.fill(0, 0, width, height, 0xFF000000);
        float fadeOut = t > 0.9f ? (1f - t) / 0.1f : 1f;
        int cx = width / 2, cy = height / 2;
        if (fim) {
            float a = Math.min(1f, t / 0.25f) * fadeOut;
            String s1 = "FIM DA PRIMEIRA FASE";
            drawScaled(c, tr, s1, cx, cy - 6, 2.4f, 0xE8C45A, a);
            drawScaled(c, tr, "Karamarraw e a Proposta de Jord", cx, cy + 22, 1f, 0xB8B0A0, a * 0.8f);
            return;
        }
        // 1) linha dourada abre do centro; 2) letras entram uma a uma com brilho; 3) subtítulo; 4) fecha em preto
        float line = Math.min(1f, Math.max(0f, (t - 0.06f) / 0.16f));
        int half = (int) (width * 0.34f * line);
        c.fill(cx - half, cy + 18, cx + half, cy + 19, ((int) (255 * fadeOut) << 24) | 0xC8A25A);
        String word = "V E R D E N   A F";
        float scale = 4f + 0.25f * (t * 0.5f);
        int n = word.length();
        float total = tr.getWidth(word) * scale, x = cx - total / 2f;
        for (int i = 0; i < n; i++) {
            float start = 0.2f + 0.34f * i / n, a = Math.min(1f, Math.max(0f, (t - start) / 0.07f)) * fadeOut;
            String ch = String.valueOf(word.charAt(i));
            float cw = tr.getWidth(ch) * scale;
            if (a > 0.02f && !ch.isBlank()) {
                for (int g = 3; g >= 1; g--) drawScaled(c, tr, ch, Math.round(x + cw / 2), cy - 22, scale * (1f + 0.05f * g), 0xC8A25A, a * 0.10f);
                drawScaled(c, tr, ch, Math.round(x + cw / 2), cy - 22, scale, 0xF2D27A, a);
            }
            x += cw;
        }
        float sub = Math.min(1f, Math.max(0f, (t - 0.6f) / 0.15f)) * fadeOut;
        if (sub > 0.02f) drawScaled(c, tr, "P R E L Ú D I O", cx, cy + 30, 1.3f, 0xB8B0A0, sub);
        // partículas de brasa subindo (determinísticas)
        for (int i = 0; i < 60; i++) {
            float seed = (i * 0.6180339f) % 1f, sp = 0.2f + 0.5f * ((i * 0.37f) % 1f);
            int px = (int) (seed * width), py = height - (int) (((now / 1000f * sp * 40f) + i * 31) % height);
            int a = (int) (110 * fadeOut * (0.4f + 0.6f * ((i * 0.73f) % 1f)));
            c.fill(px, py, px + 1 + (i % 2), py + 1 + (i % 2), (Math.max(8, a) << 24) | 0xE8C45A);
        }
    }

    private void drawScaled(DrawContext c, TextRenderer tr, String s, int cx, int cy, float sc, int rgb, float alpha) {
        int a = Math.max(8, Math.min(255, (int) (255 * alpha)));
        c.getMatrices().push();
        c.getMatrices().translate(cx, cy, 0);
        c.getMatrices().scale(sc, sc, 1f);
        c.drawText(tr, s, -tr.getWidth(s) / 2, 0, (a << 24) | rgb, false);
        c.getMatrices().pop();
    }
}
