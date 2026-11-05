package com.verden.client.rpg;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Menu principal de Verden: substitui a tela inicial do Minecraft. Só oferece Novo Jogo, Continuar Jogo,
 * Configurações e Sair. "Novo Jogo" cria o mundo de Verden sozinho (ver {@link RpgClient}) e o prólogo começa ao entrar.
 * "Continuar Jogo" abre, por enquanto, a lista de mundos do Minecraft — a tela de saves própria é a próxima etapa.
 */
public class RpgTitleScreen extends Screen {
    /** Quando verdadeiro, a próxima tela "Criar mundo" é preenchida e confirmada automaticamente. */
    static boolean autoCreate;

    public RpgTitleScreen() {
        super(Text.literal("Verden"));
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 2 + 6, w = 200;
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo Jogo"), b -> {
            autoCreate = true;
            CreateWorldScreen.create(client, this);
        }).dimensions(cx - w / 2, y, w, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Continuar Jogo"), b -> client.setScreen(new SelectWorldScreen(this))).dimensions(cx - w / 2, y + 27, w, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Configurações"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(cx - w / 2, y + 54, w, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Sair do Jogo"), b -> client.scheduleStop()).dimensions(cx - w / 2, y + 81, w, 22).build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fillGradient(0, 0, width, height, 0xFF1C1208, 0xFF0A0604);
        c.fillGradient(0, height / 2, width, height, 0x00000000, 0x40C8A25A);
        long now = System.currentTimeMillis();
        for (int i = 0; i < 70; i++) { // brasas subindo
            float seed = (i * 0.6180339f) % 1f, sp = 0.15f + 0.5f * ((i * 0.37f) % 1f);
            int px = (int) (seed * width), py = height - (int) (((now / 1000f * sp * 36f) + i * 29) % height);
            int a = 40 + (int) (90 * ((i * 0.73f) % 1f));
            c.fill(px, py, px + 1 + (i % 2), py + 1 + (i % 2), (a << 24) | 0xE8C45A);
        }
        int lw = 320, lh = 80;
        c.drawTexture(RpgUi.tex("logo"), (width - lw) / 2, height / 2 - 120, lw, lh, 0f, 0f, 256, 64, 256, 64);
        c.drawCenteredTextWithShadow(textRenderer, "Prelúdio e Agouro", width / 2, height / 2 - 34, RpgUi.GREY);
        super.render(c, mx, my, delta);
    }
}
