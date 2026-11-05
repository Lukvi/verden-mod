package com.verden.client.rpg;

import com.verden.entity.NpcRole;
import com.verden.entity.VerdenNpcEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;
import java.util.Map;

/**
 * HUD do RPG (desenhada por cima da vanilla): painel da protagonista (retrato, nível, XP, vida), rastreador de
 * missão, avisos animados (toasts) e dica de interação. Discreta na exploração; some durante diálogos.
 * Os corações vanilla continuam visíveis (escondê-los exigiria Mixin — pendente).
 */
final class RpgHud {
    private RpgHud() {}

    private static final long TOAST_MS = 4200, SLIDE_MS = 350;

    static void render(DrawContext c, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || RpgClient.state == null || !RpgClient.started()) return;
        if (mc.currentScreen instanceof DialogueScreen || mc.currentScreen instanceof CineScreen) return;
        TextRenderer tr = mc.textRenderer;
        int w = c.getScaledWindowWidth(), h = c.getScaledWindowHeight();
        playerPanel(c, tr, mc, h);
        questTracker(c, tr, w);
        prompt(c, tr, mc, w, h);
        toasts(c, tr, w);
    }

    private static void playerPanel(DrawContext c, TextRenderer tr, MinecraftClient mc, int h) {
        Map<String, Object> me = null;
        for (Map<String, Object> ch : RpgClient.list("chars")) if (Boolean.TRUE.equals(ch.get("active"))) me = ch;
        if (me == null) return;
        int x = 6, y = h - 56;
        RpgUi.panel(c, x, y, 156, 50);
        RpgUi.portrait(c, String.valueOf(me.get("id")), x + 5, y + 5, 40);
        c.drawText(tr, String.valueOf(me.get("name")), x + 50, y + 6, RpgUi.GOLD, true);
        c.drawText(tr, "Nv " + me.get("level"), x + 50, y + 17, RpgUi.WHITE, true);
        float xp = ((Number) me.get("xp")).floatValue() / Math.max(1f, ((Number) me.get("xpNext")).floatValue());
        RpgUi.icon(c, "xp", x + 50, y + 28, 8);
        RpgUi.bar(c, x + 60, y + 29, 90, 6, xp, 0xFF58B858);
        RpgUi.icon(c, "heart", x + 50, y + 38, 8);
        RpgUi.bar(c, x + 60, y + 39, 90, 6, mc.player.getHealth() / Math.max(1f, mc.player.getMaxHealth()), 0xFFC84040);
    }

    private static void questTracker(DrawContext c, TextRenderer tr, int w) {
        Map<String, Object> pick = null;
        for (Map<String, Object> q : RpgClient.list("quests")) {
            if (!"active".equals(q.get("status"))) continue;
            if (pick == null || "MAIN".equals(q.get("category")) && !"MAIN".equals(pick.get("category"))) pick = q;
        }
        if (pick == null) return;
        @SuppressWarnings("unchecked") List<Object> lines = (List<Object>) pick.get("lines");
        int bw = 190, x = w - bw - 6, y = 6, rows = 0;
        for (Object o : lines) if (String.valueOf(o).startsWith("[")) rows++;
        RpgUi.panel(c, x, y, bw, 22 + rows * 11);
        RpgUi.icon(c, "MAIN".equals(pick.get("category")) ? "quest_main" : "INVESTIGATION".equals(pick.get("category")) ? "quest_invest" : "quest_side", x + 5, y + 4, 12);
        c.drawText(tr, tr.trimToWidth(String.valueOf(pick.get("title")), bw - 28), x + 21, y + 6, RpgUi.GOLD, true);
        int ty = y + 19;
        for (Object o : lines) {
            String s = String.valueOf(o);
            if (!s.startsWith("[")) continue;
            boolean done = s.startsWith("[x]");
            c.drawText(tr, tr.trimToWidth((done ? "+ " : "- ") + s.substring(4), bw - 12), x + 6, ty, done ? RpgUi.GREEN : RpgUi.WHITE, true);
            ty += 11;
        }
    }

    private static void prompt(DrawContext c, TextRenderer tr, MinecraftClient mc, int w, int h) {
        String label = null;
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult eh && eh.getEntity() instanceof VerdenNpcEntity n && n.getRole().ordinal() >= NpcRole.DAN.ordinal()) {
            label = "Conversar com " + n.getDisplayName().getString();
        } else if (hr instanceof BlockHitResult bh && hr.getType() == HitResult.Type.BLOCK) {
            for (Map<String, Object> p : RpgClient.list("points")) {
                if (((Number) p.get("x")).intValue() == bh.getBlockPos().getX() && ((Number) p.get("y")).intValue() == bh.getBlockPos().getY()
                        && ((Number) p.get("z")).intValue() == bh.getBlockPos().getZ()) label = "Examinar: " + p.get("label");
            }
        }
        if (label == null) return;
        int tw = tr.getWidth(label) + 28;
        int x = (w - tw) / 2, y = h / 2 + 24;
        RpgUi.panel(c, x, y, tw, 20);
        RpgUi.icon(c, "interact", x + 5, y + 2, 16);
        c.drawText(tr, label, x + 24, y + 6, RpgUi.WHITE, true);
    }

    private static void toasts(DrawContext c, TextRenderer tr, int w) {
        long now = System.currentTimeMillis();
        RpgClient.toasts.removeIf(t -> now - t.startMs() > TOAST_MS);
        int i = 0;
        for (RpgClient.Toast t : RpgClient.toasts) {
            long age = now - t.startMs();
            float in = Math.min(1f, age / (float) SLIDE_MS), out = Math.min(1f, (TOAST_MS - age) / (float) SLIDE_MS);
            float k = Math.min(in, out);
            int bw = 230, x = (w - bw) / 2, y = Math.round(-34 + k * (44 + i * 34));
            String title, icon;
            switch (t.kind()) {
                case "QUEST_STARTED" -> { title = "Missão iniciada"; icon = "quest_main"; }
                case "QUEST_UPDATED" -> { title = "Objetivo atualizado"; icon = "quest_invest"; }
                case "QUEST_COMPLETED" -> { title = "Missão concluída"; icon = "quest_done"; }
                case "LEVEL_UP" -> { title = "Nível acima!"; icon = "xp"; }
                case "DISCOVERY" -> { title = "Descoberta"; icon = "codex"; }
                default -> { title = "Aviso"; icon = "interact"; }
            }
            RpgUi.panel(c, x, y, bw, 30);
            RpgUi.icon(c, icon, x + 6, y + 7, 16);
            c.drawText(tr, title, x + 28, y + 5, RpgUi.GOLD, true);
            c.drawText(tr, tr.trimToWidth(t.text(), bw - 34), x + 28, y + 17, RpgUi.WHITE, true);
            i++;
        }
    }
}
