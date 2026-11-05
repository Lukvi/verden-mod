package com.verden.client.rpg;

import com.verden.rpg.core.Json;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

/**
 * Estado e execução das cutscenes no cliente. O servidor manda instruções ({@code RpgNet.Cine}); aqui elas viram
 * câmera (entidade-marcador só do cliente, sem Mixin), letterbox, fades, legendas, título e música.
 * A câmera é interpolada A CADA QUADRO (não a cada tick), por isso os movimentos ficam suaves.
 */
final class CineController {
    private CineController() {}

    static boolean active, skippable;
    static float letter, letterTarget, fadeFrom, fadeTo;
    static long fadeStart, fadeDur;
    static String speaker = "", text = "", titleId;
    static long lineStart, lineDur, titleStart, titleDur;
    private static double[] p0, p1, t0, t1;
    private static long shotStart, shotDur;
    private static MarkerEntity cam;
    private static boolean hudWas;

    @SuppressWarnings("unchecked")
    static void handle(String json) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Map<String, Object> m = Json.parseObject(json);
        long now = System.currentTimeMillis();
        switch (String.valueOf(m.get("cmd"))) {
            case "begin" -> begin(mc, Boolean.TRUE.equals(m.get("skippable")));
            case "end" -> end(mc);
            case "fade" -> {
                fadeFrom = fadeAlpha(now);
                fadeTo = ((Number) m.get("to")).floatValue();
                fadeStart = now;
                fadeDur = ((Number) m.get("ticks")).longValue() * 50;
            }
            case "letterbox" -> letterTarget = Boolean.TRUE.equals(m.get("on")) ? 1f : 0f;
            case "line" -> {
                speaker = String.valueOf(m.get("speaker"));
                text = String.valueOf(m.get("text"));
                lineStart = now;
                lineDur = ((Number) m.get("ticks")).longValue() * 50;
            }
            case "title" -> {
                titleId = String.valueOf(m.get("id"));
                titleStart = now;
                titleDur = ((Number) m.get("ticks")).longValue() * 50;
            }
            case "shot" -> {
                p0 = vec(m.get("p0")); p1 = vec(m.get("p1")); t0 = vec(m.get("t0")); t1 = vec(m.get("t1"));
                shotStart = now;
                shotDur = Math.max(1, ((Number) m.get("ticks")).longValue() * 50);
            }
            case "music" -> {
                mc.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                mc.getSoundManager().play(PositionedSoundInstance.music(SoundEvent.of(Identifier.of(String.valueOf(m.get("id"))))));
            }
            case "stopmusic" -> mc.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
            default -> { }
        }
    }

    private static double[] vec(Object o) {
        List<?> l = (List<?>) o;
        return new double[]{((Number) l.get(0)).doubleValue(), ((Number) l.get(1)).doubleValue(), ((Number) l.get(2)).doubleValue()};
    }

    static float fadeAlpha(long now) {
        if (fadeDur <= 0) return fadeTo;
        float k = Math.min(1f, (now - fadeStart) / (float) fadeDur);
        return fadeFrom + (fadeTo - fadeFrom) * k;
    }

    private static void begin(MinecraftClient mc, boolean skip) {
        if (mc.player == null || mc.world == null) return;
        active = true;
        skippable = skip;
        letter = letterTarget = 0f;
        fadeFrom = fadeTo = 0f;
        fadeDur = 0;
        speaker = text = "";
        titleId = null;
        p0 = null;
        if (cam != null) mc.world.removeEntity(cam.getId(), Entity.RemovalReason.DISCARDED); // cena encadeada: troca a câmera
        else hudWas = mc.options.hudHidden;
        mc.options.hudHidden = true;
        cam = new MarkerEntity(EntityType.MARKER, mc.world);
        cam.setPosition(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        mc.world.addEntity(cam);
        mc.setCameraEntity(cam);
        if (!(mc.currentScreen instanceof CineScreen)) mc.setScreen(new CineScreen());
    }

    static void end(MinecraftClient mc) {
        if (!active && cam == null) return;
        active = false;
        mc.setCameraEntity(mc.player);
        if (cam != null && mc.world != null) mc.world.removeEntity(cam.getId(), Entity.RemovalReason.DISCARDED);
        cam = null;
        p0 = null;
        titleId = null;
        mc.options.hudHidden = hudWas;
        if (mc.currentScreen instanceof CineScreen) mc.setScreen(null);
    }

    /** Desconexão/troca de mundo: nunca deixar a câmera presa. */
    static void reset() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (active || cam != null) {
            active = false;
            cam = null;
            if (mc.player != null) mc.setCameraEntity(mc.player);
            mc.options.hudHidden = hudWas;
        }
    }

    /** Chamado a cada quadro pela tela: move a câmera. */
    static void frame() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (cam == null || p0 == null || mc.world == null) return;
        double k = Math.min(1.0, (System.currentTimeMillis() - shotStart) / (double) shotDur);
        k = k * k * (3 - 2 * k);
        double px = p0[0] + (p1[0] - p0[0]) * k, py = p0[1] + (p1[1] - p0[1]) * k, pz = p0[2] + (p1[2] - p0[2]) * k;
        double tx = t0[0] + (t1[0] - t0[0]) * k, ty = t0[1] + (t1[1] - t0[1]) * k, tz = t0[2] + (t1[2] - t0[2]) * k;
        double dx = tx - px, dy = ty - py, dz = tz - pz;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        cam.setPosition(px, py, pz);
        cam.setYaw(yaw);
        cam.setPitch(pitch);
        cam.prevX = cam.lastRenderX = px;
        cam.prevY = cam.lastRenderY = py;
        cam.prevZ = cam.lastRenderZ = pz;
        cam.prevYaw = yaw;
        cam.prevPitch = pitch;
    }
}
