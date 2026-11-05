package com.verden.client.rpg;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

/** Menu do RPG (tecla V): Missões, Personagens (troca de protagonista) e Codex, com iniciar/reiniciar a Fase 1. */
public class RpgMenuScreen extends Screen {
    private static final int PW = 420, PH = 250;
    private int tab; // 0 missões, 1 personagens, 2 codex
    private boolean confirmRestart;

    public RpgMenuScreen() {
        super(Text.literal("Verden"));
    }

    @Override
    protected void init() {
        RpgClient.send("sync");
        rebuild();
    }

    private void rebuild() {
        clearChildren();
        int x0 = (width - PW) / 2, y0 = (height - PH) / 2;
        String[] names = {"Missões", "Personagens", "Codex"};
        for (int i = 0; i < 3; i++) {
            final int t = i;
            addDrawableChild(ButtonWidget.builder(Text.literal((tab == i ? "» " : "") + names[i]), b -> { tab = t; rebuild(); })
                    .dimensions(x0 + 8 + i * 92, y0 + 8, 88, 18).build());
        }
        String startLabel = !RpgClient.started() ? "Iniciar Fase 1" : confirmRestart ? "Confirmar: apagar e reiniciar" : "Reiniciar Fase 1";
        addDrawableChild(ButtonWidget.builder(Text.literal(startLabel), b -> {
            if (!RpgClient.started()) { RpgClient.send("start"); close(); }
            else if (!confirmRestart) { confirmRestart = true; rebuild(); }
            else { RpgClient.send("restart"); close(); }
        }).dimensions(x0 + PW - 188, y0 + PH - 26, 150, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), b -> close()).dimensions(x0 + PW - 34, y0 + PH - 26, 28, 18).build());
        if (tab == 1) {
            int y = y0 + 34;
            for (Map<String, Object> ch : RpgClient.list("chars")) {
                boolean active = Boolean.TRUE.equals(ch.get("active")), avail = Boolean.TRUE.equals(ch.get("available"));
                final String id = String.valueOf(ch.get("id"));
                ButtonWidget b = ButtonWidget.builder(Text.literal(active ? "Em controle" : avail ? "Controlar" : "Bloqueada"), bb -> RpgClient.send("switch:" + id))
                        .dimensions(x0 + PW - 108, y + 20, 96, 18).build();
                b.active = avail && !active;
                addDrawableChild(b);
                y += 66;
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        int x0 = (width - PW) / 2, y0 = (height - PH) / 2;
        RpgUi.panel(c, x0, y0, PW, PH);
        TextRenderer tr = textRenderer;
        c.drawText(tr, "VERDEN", x0 + PW - 52, y0 + 13, RpgUi.GOLD, true);
        switch (tab) {
            case 0 -> quests(c, tr, x0, y0);
            case 1 -> chars(c, tr, x0, y0);
            default -> codex(c, tr, x0, y0);
        }
        if (!RpgClient.started()) c.drawText(tr, "Nenhuma campanha iniciada neste mundo.", x0 + 12, y0 + PH - 22, RpgUi.GREY, false);
        super.render(c, mx, my, delta);
    }

    @SuppressWarnings("unchecked")
    private void quests(DrawContext c, TextRenderer tr, int x0, int y0) {
        int y = y0 + 34, limit = y0 + PH - 32;
        List<Map<String, Object>> qs = RpgClient.list("quests");
        if (qs.isEmpty()) c.drawText(tr, "Sem missões. Inicie a Fase 1 para começar.", x0 + 14, y + 4, RpgUi.GREY, false);
        for (int pass = 0; pass < 2; pass++) {
            for (Map<String, Object> q : qs) {
                boolean active = "active".equals(q.get("status"));
                if (active != (pass == 0)) continue;
                if (y > limit - 24) return;
                String cat = String.valueOf(q.get("category"));
                RpgUi.icon(c, !active ? "quest_done" : "MAIN".equals(cat) ? "quest_main" : "INVESTIGATION".equals(cat) ? "quest_invest" : "quest_side", x0 + 12, y, 14);
                c.drawText(tr, String.valueOf(q.get("title")) + (active ? "" : " (concluída)"), x0 + 32, y + 3, active ? RpgUi.GOLD : RpgUi.GREEN, true);
                y += 16;
                if (!active) continue;
                for (Object o : (List<Object>) q.get("lines")) {
                    String s = String.valueOf(o);
                    boolean obj = s.startsWith("[");
                    for (OrderedText l : tr.wrapLines(Text.literal(obj ? s : s), PW - 60)) {
                        if (y > limit) return;
                        c.drawText(tr, l, x0 + 32, y, obj ? (s.startsWith("[x]") ? RpgUi.GREEN : RpgUi.WHITE) : RpgUi.GREY, false);
                        y += 10;
                    }
                }
                y += 6;
            }
        }
    }

    private void chars(DrawContext c, TextRenderer tr, int x0, int y0) {
        int y = y0 + 34;
        for (Map<String, Object> ch : RpgClient.list("chars")) {
            boolean avail = Boolean.TRUE.equals(ch.get("available"));
            RpgUi.panel(c, x0 + 10, y, PW - 20, 60);
            RpgUi.portrait(c, String.valueOf(ch.get("id")), x0 + 16, y + 6, 48);
            c.drawText(tr, String.valueOf(ch.get("name")), x0 + 72, y + 8, RpgUi.GOLD, true);
            c.drawText(tr, "Nv " + ch.get("level") + "   XP " + ch.get("xp") + "/" + ch.get("xpNext") + "   Pontos: " + ch.get("points"), x0 + 72, y + 22, RpgUi.WHITE, false);
            c.drawText(tr, "Aparência: " + ch.get("outfit"), x0 + 72, y + 34, RpgUi.GREY, false);
            if (!avail) {
                RpgUi.icon(c, "lock", x0 + 72, y + 45, 10);
                c.drawText(tr, "A história ainda não liberou esta personagem.", x0 + 86, y + 46, RpgUi.DIM, false);
            }
            y += 66;
        }
        if (Boolean.TRUE.equals(RpgClient.state == null ? null : RpgClient.state.get("locked")))
            c.drawText(tr, "A história não permite trocar de personagem agora.", x0 + 14, y + 2, RpgUi.RED, false);
        else c.drawText(tr, "A skin do jogador ainda não muda com a troca (pendente).", x0 + 14, y + 2, RpgUi.DIM, false);
    }

    private void codex(DrawContext c, TextRenderer tr, int x0, int y0) {
        List<Map<String, Object>> dummy = RpgClient.list("codex");
        Object o = RpgClient.state == null ? null : RpgClient.state.get("codex");
        int y = y0 + 34, col = 0;
        if (!(o instanceof List<?> l) || l.isEmpty()) {
            c.drawText(tr, "Nada registrado ainda. Descobertas aparecem aqui.", x0 + 14, y + 4, RpgUi.GREY, false);
            return;
        }
        for (Object e : l) {
            int x = x0 + 14 + col * (PW / 2 - 6);
            RpgUi.icon(c, "codex", x, y, 10);
            c.drawText(tr, tr.trimToWidth(RpgUi.pretty(String.valueOf(e)), PW / 2 - 40), x + 14, y + 1, RpgUi.WHITE, false);
            if (++col == 2) { col = 0; y += 14; }
            if (y > y0 + PH - 40) break;
        }
    }
}
