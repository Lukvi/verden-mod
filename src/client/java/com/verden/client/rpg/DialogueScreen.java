package com.verden.client.rpg;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caixa de diálogo: retrato + nome do falante, texto que "digita", opções numeradas (1–9 ou clique).
 * O mundo continua visível. O jogo NÃO pausa (o servidor precisa rodar para responder). ESC encerra a conversa.
 */
public class DialogueScreen extends Screen {
    private String name = "", portrait = "narrador", text = "";
    private List<String> options = new ArrayList<>();
    private int age;
    private final List<ClickableWidget> buttons = new ArrayList<>();

    public DialogueScreen() {
        super(Text.literal("Diálogo"));
    }

    @SuppressWarnings("unchecked")
    void update(Map<String, Object> d) {
        name = String.valueOf(d.get("name"));
        portrait = String.valueOf(d.get("portrait"));
        text = String.valueOf(d.get("text"));
        options = new ArrayList<>();
        if (d.get("options") instanceof List<?> l) for (Object o : l) options.add(String.valueOf(o));
        age = 0;
        rebuild();
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearChildren();
        buttons.clear();
        if (width == 0) return;
        for (int i = 0; i < options.size(); i++) {
            final int idx = i;
            ButtonWidget b = ButtonWidget.builder(Text.literal((i + 1) + ". " + options.get(i)), btn -> RpgClient.send("choose:" + idx))
                    .dimensions(width / 2 - 190, height - 132 - (options.size() - i) * 22, 380, 20).build();
            b.visible = false;
            buttons.add(b);
            addDrawableChild(b);
        }
    }

    private int shown() {
        return Math.min(text.length(), age * 2);
    }

    private boolean fullyShown() {
        return shown() >= text.length();
    }

    @Override
    public void tick() {
        age++;
        if (fullyShown()) for (ClickableWidget b : buttons) b.visible = true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        // sem escurecer o mundo inteiro: só um degradê na parte de baixo
        c.fillGradient(0, height - 170, width, height, 0x00000000, 0xC0000000);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c, mx, my, delta);
        TextRenderer tr = textRenderer;
        int bw = Math.min(width - 24, 420), x = (width - bw) / 2, y = height - 118, bh = 108;
        RpgUi.panel(c, x, y, bw, bh);
        RpgUi.portrait(c, portrait, x + 8, y + 8, 64);
        if (!name.isEmpty()) c.drawText(tr, name, x + 82, y + 8, RpgUi.GOLD, true);
        int ty = y + (name.isEmpty() ? 10 : 22);
        String shownText = text.substring(0, shown());
        boolean narr = name.isEmpty();
        for (OrderedText line : tr.wrapLines(Text.literal(shownText), bw - 96)) {
            c.drawText(tr, line, x + 82, ty, narr ? RpgUi.GREY : RpgUi.WHITE, true);
            ty += 11;
        }
        if (fullyShown() && options.isEmpty()) c.drawText(tr, "[Espaço] continuar   [Esc] sair", x + bw - 160, y + bh - 14, RpgUi.DIM, false);
        else if (fullyShown()) c.drawText(tr, "Escolha 1–" + options.size() + "   [Esc] sair", x + bw - 120, y + bh - 14, RpgUi.DIM, false);
        super.render(c, mx, my, delta);
    }

    private void proceed() {
        if (!fullyShown()) {
            age = text.length();
            tick();
        } else if (options.isEmpty()) {
            RpgClient.send("advance");
        }
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 32 || key == 257) { // espaço / enter
            proceed();
            return true;
        }
        if (key >= 49 && key <= 57 && fullyShown() && key - 49 < options.size()) {
            RpgClient.send("choose:" + (key - 49));
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (super.mouseClicked(mx, my, btn)) return true;
        proceed();
        return true;
    }

    @Override
    public void close() {
        RpgClient.send("close");
        super.close();
    }
}
