package com.verden.rpg.server;

import com.verden.registry.ModSounds;
import com.verden.rpg.RpgRuntime;
import com.verden.rpg.core.Cutscene;
import com.verden.rpg.core.CutsceneRunner;
import com.verden.rpg.core.GameEvent;
import com.verden.rpg.core.Json;
import com.verden.rpg.core.Session;
import com.verden.rpg.net.RpgNet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diretor de cutscenes (servidor). Liga o {@link CutsceneRunner} do núcleo ao jogo: a cada tick executa as instruções
 * devidas — visuais (fade, letterbox, câmera, falas, título, música) viram pacotes {@link RpgNet.Cine} para o cliente;
 * ações do mundo ("act") chamam {@link Fase01#act}. Garantias:
 *  - pular só executa as ações essenciais restantes (o mundo fica coerente) e só se a cena for pulável;
 *  - sair do jogo/morrer no meio ABORTA sem marcar a cena como feita: ao voltar, ela recomeça do início;
 *  - ao terminar: efeitos de fim, flag cs_feito:ID, evento cutscene:ID (avança a missão) e salvamento.
 */
final class CutsceneDirector {
    private CutsceneDirector() {}

    private static final Logger LOG = LoggerFactory.getLogger("verden/cine");
    private static CutsceneRunner active;
    private static ServerPlayerEntity actor;

    static boolean busy() { return active != null; }

    static void send(ServerPlayerEntity p, Map<String, Object> m) {
        ServerPlayNetworking.send(p, new RpgNet.Cine(Json.write(m)));
    }

    static Map<String, Object> m(Object... kv) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    static boolean start(Session s, String id, ServerPlayerEntity p) {
        Cutscene cs = s.cutscenes.get(id);
        if (active != null || cs == null) return false;
        active = new CutsceneRunner(cs);
        actor = p;
        p.setInvulnerable(true);
        send(p, m("cmd", "begin", "skippable", cs.skippable()));
        return true;
    }

    static void tick() {
        if (active == null) return;
        Session s = RpgRuntime.session();
        if (s == null || actor == null || actor.isRemoved() || !actor.isAlive()) { abort(); return; }
        actor.setVelocity(0, actor.getVelocity().y, 0);
        for (CutsceneRunner.Cue c : active.tick()) safeExec(c, s);
        if (active != null && active.finished()) end(s);
    }

    static void skip() {
        if (active == null) return;
        Session s = RpgRuntime.session();
        List<CutsceneRunner.Cue> rest = active.skip();
        if (!active.finished() || s == null) return; // cena não pulável: ignora o pedido
        for (CutsceneRunner.Cue c : rest) safeExec(c, s);
        send(actor, m("cmd", "fade", "to", 0.0, "ticks", 0));
        end(s);
    }

    /** Interrompe sem concluir (desconexão, morte...). A cena NÃO é marcada como feita. */
    static void abort() {
        if (active == null) return;
        if (actor != null) {
            actor.setInvulnerable(false);
            if (!actor.isRemoved()) send(actor, m("cmd", "end"));
        }
        active = null;
        actor = null;
    }

    /** Uma ação que falhe NUNCA pode travar a cena nem derrubar o servidor: registra e segue. */
    private static void safeExec(CutsceneRunner.Cue c, Session s) {
        try { exec(c, s); } catch (RuntimeException e) { LOG.error("Falha na instrução de cutscene '{}' ({})", c.kind(), c.params(), e); }
    }

    private static void end(Session s) {
        Cutscene cs = active.cutscene();
        ServerPlayerEntity p = actor;
        active = null;
        actor = null;
        p.setInvulnerable(false);
        s.state.flags.add("cs_feito:" + cs.id());
        try { s.apply(cs.onEnd()); } catch (RuntimeException e) { LOG.error("Falha nos efeitos finais de {}", cs.id(), e); }
        s.fire(new GameEvent("cutscene", cs.id()));
        RpgRuntime.saveNow("cutscene " + cs.id());
        RpgServer.markDirty();
        String nx = Fase01.nextAuto(s); // encadeia sem fechar a tela: evita 1–2 quadros do mundo entre as cenas
        if (nx != null && start(s, nx, p)) return;
        send(p, m("cmd", "end"));
    }

    private static double num(Object o, double def) { return o instanceof Number n ? n.doubleValue() : def; }

    private static String name(Session s, String speaker) {
        return switch (speaker) {
            case "majestade" -> "Majestade";
            case "reeves" -> "Tenente Reeves";
            default -> s.characters.containsKey(speaker) ? s.characters.get(speaker).name() : speaker;
        };
    }

    private static final Map<String, String> MUSIC = Map.of("karamarraw_mar", "verden:rpg.mar", "relatorio_tema", "verden:rpg.relatorio", "titulo_tema", "verden:rpg.titulo");

    @SuppressWarnings("unchecked")
    private static void exec(CutsceneRunner.Cue c, Session s) {
        ServerPlayerEntity p = actor;
        Map<String, Object> a = c.params();
        switch (c.kind()) {
            case "fade" -> send(p, m("cmd", "fade", "to", num(a.get("to"), 0), "ticks", (int) num(a.get("ticks"), 0)));
            case "letterbox" -> send(p, m("cmd", "letterbox", "on", Boolean.TRUE.equals(a.get("on"))));
            case "line" -> {
                String text = String.valueOf(a.get("text"));
                send(p, m("cmd", "line", "speaker", name(s, String.valueOf(a.get("speaker"))), "text", text, "ticks", (int) num(a.get("ticks"), 50 + text.length() * 2)));
            }
            case "title" -> send(p, m("cmd", "title", "id", String.valueOf(a.get("id")), "ticks", (int) num(a.get("ticks"), 400)));
            case "music" -> {
                String id = String.valueOf(a.get("id"));
                send(p, id.equals("stop") ? m("cmd", "stopmusic") : m("cmd", "music", "id", MUSIC.getOrDefault(id, id)));
                Fase01.musicSent(id.equals("stop") ? null : MUSIC.getOrDefault(id, id));
            }
            case "shot" -> {
                Integer ay = s.state.vars.get("slice_ay");
                Shots.Shot sh = Shots.get(String.valueOf(a.get("id")), p.getServerWorld(), ay == null ? 64 : ay);
                if (sh != null) send(p, m("cmd", "shot", "p0", Json.parse(Json.write(java.util.Arrays.stream(sh.p0()).boxed().toList())), "p1", Json.parse(Json.write(java.util.Arrays.stream(sh.p1()).boxed().toList())),
                        "t0", Json.parse(Json.write(java.util.Arrays.stream(sh.t0()).boxed().toList())), "t1", Json.parse(Json.write(java.util.Arrays.stream(sh.t1()).boxed().toList())), "ticks", sh.ticks()));
            }
            case "sound" -> {
                String id = String.valueOf(a.get("id"));
                SoundEvent ev = id.equals("tiro_distante") ? ModSounds.RPG_TIRO : id.equals("vento_deserto") ? ModSounds.RPG_VENTO : null;
                if (ev != null) {
                    boolean tr = id.equals("tiro_distante");
                    double x = tr ? KarBuilder.LX + 0.5 : p.getX(), y = tr ? KarBuilder.SEA + 2 : p.getY(), z = tr ? KarBuilder.cz + 3.5 : p.getZ();
                    p.getServerWorld().playSound(null, x, y, z, ev, SoundCategory.PLAYERS, id.equals("tiro_distante") ? 6f : 1.2f, 1f);
                }
            }
            case "act" -> Fase01.act(String.valueOf(a.get("name")), p, s);
            case "effect" -> s.apply((List<String>) a.get("effects"));
            default -> { }
        }
    }
}
