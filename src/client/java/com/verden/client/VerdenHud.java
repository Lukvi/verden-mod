package com.verden.client;

import com.verden.item.GrimoireItem;
import com.verden.magic.Power;
import com.verden.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** HUD do mod: painel de poderes (com recarga) do Grimório, munição do Revólver e bolsa de moedas. */
public final class VerdenHud {
    private VerdenHud() {}

    private static final int[] COLORS = {
            0xFFFF7A1A, // chamas
            0xFFBFE9FF, // vento
            0xFFB06BFF, // telecinese
            0xFF7FD8FF, // gelo
            0xFFD02838, // névoa carmesim
            0xFFE8D060, // crono
            0xFF6A2A8A, // magia negra
            0xFF4A7BFF  // metamorfose
    };

    public static void register() {
        HudRenderCallback.EVENT.register(VerdenHud::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (p == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) {
            return;
        }
        int h = ctx.getScaledWindowHeight();
        int w = ctx.getScaledWindowWidth();
        float delta = tick.getTickDelta(false);

        ItemStack grim = held(p, ModItems.GRIMORIO);
        if (!grim.isEmpty()) {
            drawPowers(ctx, mc, p, grim, 8, h - 46, delta);
        }

        ItemStack gun = held(p, ModItems.REVOLVER_CUBUS);
        if (!gun.isEmpty()) {
            drawAmmo(ctx, mc, p, 8, h - 36);
        }

        drawCoins(ctx, mc, p, w - 8);
    }

    private static ItemStack held(PlayerEntity p, Item item) {
        if (p.getMainHandStack().isOf(item)) {
            return p.getMainHandStack();
        }
        if (p.getOffHandStack().isOf(item)) {
            return p.getOffHandStack();
        }
        return ItemStack.EMPTY;
    }

    private static void drawPowers(DrawContext ctx, MinecraftClient mc, PlayerEntity p, ItemStack grim, int x, int y, float delta) {
        Power cur = GrimoireItem.getPower(grim);
        int color = COLORS[Math.min(cur.ordinal(), COLORS.length - 1)];
        int w = 112;
        ctx.fill(x - 3, y - 3, x + w + 3, y + 38, 0xA0000000);
        ctx.fill(x - 3, y - 3, x - 1, y + 38, color);
        ctx.drawText(mc.textRenderer, cur.displayName(), x + 4, y, color | 0xFF000000, true);

        float cd = p.getItemCooldownManager().getCooldownProgress(grim.getItem(), delta);
        int bar = (int) ((1f - cd) * (w - 6));
        ctx.fill(x + 4, y + 12, x + w - 2, y + 17, 0xFF202020);
        ctx.fill(x + 4, y + 12, x + 4 + bar, y + 17, cd > 0 ? 0xFF888888 : color);

        Power[] all = Power.values();
        for (int i = 0; i < all.length; i++) {
            int cx = x + 4 + i * 13;
            int c = COLORS[Math.min(i, COLORS.length - 1)];
            boolean sel = all[i] == cur;
            ctx.fill(cx, y + 22, cx + 10, y + 32, sel ? 0xFFFFFFFF : 0xFF101010);
            ctx.fill(cx + 1, y + 23, cx + 9, y + 31, sel ? c : (c & 0x00FFFFFF) | 0x66000000);
        }
    }

    private static void drawAmmo(DrawContext ctx, MinecraftClient mc, PlayerEntity p, int x, int y) {
        int n = count(p, ModItems.MUNICAO_45), f = count(p, ModItems.MUNICAO_INCENDIARIA), pr = count(p, ModItems.MUNICAO_PERFURANTE);
        ctx.fill(x - 3, y - 3, x + 112, y + 16, 0xA0000000);
        ctx.drawItem(new ItemStack(ModItems.MUNICAO_45), x, y - 1);
        ctx.drawText(mc.textRenderer, Text.literal(String.valueOf(n)), x + 17, y + 4, 0xFFFFFFFF, true);
        ctx.drawItem(new ItemStack(ModItems.MUNICAO_INCENDIARIA), x + 38, y - 1);
        ctx.drawText(mc.textRenderer, Text.literal(String.valueOf(f)), x + 55, y + 4, 0xFFFFA040, true);
        ctx.drawItem(new ItemStack(ModItems.MUNICAO_PERFURANTE), x + 76, y - 1);
        ctx.drawText(mc.textRenderer, Text.literal(String.valueOf(pr)), x + 93, y + 4, 0xFF80D0FF, true);
    }

    private static void drawCoins(DrawContext ctx, MinecraftClient mc, PlayerEntity p, int right) {
        int m = count(p, ModItems.MONTER);
        int c = count(p, ModItems.COPTA);
        if (m + c == 0) {
            return;
        }
        int y = 6;
        String sm = String.valueOf(m), sc = String.valueOf(c);
        int wm = mc.textRenderer.getWidth(sm), wc = mc.textRenderer.getWidth(sc);
        int total = 16 + wm + 6 + 16 + wc + 4;
        int x = right - total;
        ctx.fill(x - 3, y - 3, right + 3, y + 17, 0x90000000);
        ctx.drawItem(new ItemStack(ModItems.MONTER), x, y);
        ctx.drawText(mc.textRenderer, Text.literal(sm), x + 17, y + 4, 0xFFFFE070, true);
        ctx.drawItem(new ItemStack(ModItems.COPTA), x + 17 + wm + 6, y);
        ctx.drawText(mc.textRenderer, Text.literal(sc), x + 17 + wm + 6 + 17, y + 4, 0xFFE8B070, true);
    }

    private static int count(PlayerEntity p, Item item) {
        int n = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(item)) {
                n += s.getCount();
            }
        }
        return n;
    }
}
