package com.verden.client.rpg;

import com.verden.rpg.core.Json;
import com.verden.rpg.net.RpgNet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lado cliente do RPG: guarda o último retrato da campanha enviado pelo servidor e liga HUD, telas e tecla V. */
public final class RpgClient {
    private RpgClient() {}

    /** Último estado recebido (null até o servidor enviar). Só lido/escrito na thread do cliente. */
    static Map<String, Object> state;
    static final List<Toast> toasts = new ArrayList<>();

    record Toast(String kind, String text, long startMs) {}

    public static void init() {
        KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.verden.rpg_menu", 86, "key.categories.verden")); // V
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (key.wasPressed()) {
                if (mc.player != null && mc.currentScreen == null && !CineController.active) mc.setScreen(new RpgMenuScreen());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(RpgNet.State.ID, (payload, ctx) -> ctx.client().execute(() -> onState(payload.json())));
        ClientPlayNetworking.registerGlobalReceiver(RpgNet.Dialogue.ID, (payload, ctx) -> ctx.client().execute(() -> onDialogue(payload.json())));
        ClientPlayNetworking.registerGlobalReceiver(RpgNet.Cine.ID, (payload, ctx) -> ctx.client().execute(() -> CineController.handle(payload.json())));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { CineController.reset(); state = null; });
        HudRenderCallback.EVENT.register(RpgHud::render);
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                // o menu do Minecraft some: Verden tem o seu (só Novo Jogo / Continuar / Configurações / Sair)
                client.execute(() -> { if (client.currentScreen == screen) client.setScreen(new RpgTitleScreen()); });
            } else if (screen instanceof CreateWorldScreen cws && RpgTitleScreen.autoCreate) {
                RpgTitleScreen.autoCreate = false;
                WorldCreator wc = cws.getWorldCreator();
                wc.getGeneratorOptionsHolder().getCombinedRegistryManager().get(RegistryKeys.WORLD_PRESET)
                        .getEntry(RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of("verden", "verden")))
                        .ifPresent(e -> wc.setWorldType(new WorldCreator.WorldType(e)));
                wc.setGameMode(WorldCreator.Mode.SURVIVAL);
                wc.setWorldName("Verden - Campanha");
                client.execute(() -> {
                    for (ClickableWidget bt : Screens.getButtons(screen))
                        if (bt instanceof PressableWidget pw && bt.getMessage().getContent() instanceof TranslatableTextContent t && t.getKey().equals("selectWorld.create")) { pw.onPress(); return; }
                });
            }
        });
    }

    static void send(String cmd) {
        ClientPlayNetworking.send(new RpgNet.Action(cmd));
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> list(String key) {
        if (state == null || !(state.get(key) instanceof List<?> l)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : l) if (o instanceof Map<?, ?> m) out.add((Map<String, Object>) m);
        return out;
    }

    static boolean started() {
        return state != null && Boolean.TRUE.equals(state.get("started"));
    }

    private static void onState(String json) {
        try {
            state = Json.parseObject(json);
        } catch (RuntimeException e) {
            return;
        }
        // rede de segurança: se o servidor diz que não há cutscene mas a tela de cinema ficou ativa, devolve o controle agora
        if (CineController.active && !Boolean.TRUE.equals(state.get("cine"))) CineController.end(MinecraftClient.getInstance());
        for (Map<String, Object> t : list("toasts")) toasts.add(new Toast(String.valueOf(t.get("kind")), String.valueOf(t.get("text")), System.currentTimeMillis()));
    }

    private static void onDialogue(String json) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Map<String, Object> d;
        try {
            d = Json.parseObject(json);
        } catch (RuntimeException e) {
            return;
        }
        if (Boolean.TRUE.equals(d.get("open"))) {
            if (!(mc.currentScreen instanceof DialogueScreen)) mc.setScreen(new DialogueScreen());
            ((DialogueScreen) mc.currentScreen).update(d);
        } else if (mc.currentScreen instanceof DialogueScreen) {
            mc.setScreen(null);
        }
    }
}
