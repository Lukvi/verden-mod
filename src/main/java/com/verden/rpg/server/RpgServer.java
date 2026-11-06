package com.verden.rpg.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.verden.entity.NpcRole;
import com.verden.entity.VerdenNpcEntity;
import com.verden.registry.ModItems;
import com.verden.rpg.RpgRuntime;
import com.verden.rpg.core.CharacterDef;
import com.verden.rpg.core.CharacterSheet;
import com.verden.rpg.core.DialogueRunner;
import com.verden.rpg.core.GameEvent;
import com.verden.rpg.core.Json;
import com.verden.rpg.core.Notification;
import com.verden.rpg.core.Quest;
import com.verden.rpg.core.Session;
import com.verden.rpg.net.RpgNet;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Infraestrutura do RPG no servidor: rede, diálogo, interação, respawn, comandos e sincronização com o cliente.
 * A história da Fase 1 vive em {@link Fase01}; as cutscenes em {@link CutsceneDirector}.
 * Zigurate (provisório): fica a leste da cidade (o layout de Flerin do mod não tem zigurate).
 */
public final class RpgServer {
    private RpgServer() {}

    static final int AX = Fase01.FX + 140, AZ = Fase01.FZ;

    private static DialogueRunner dlg;
    private static final List<Map<String, Object>> toasts = new ArrayList<>();
    private static final Map<UUID, Integer> pendingJoin = new HashMap<>();
    private static boolean dirty;

    static void markDirty() { dirty = true; }

    static boolean dialogueActive() { return dlg != null; }

    static void closeDialogue() { dlg = null; }

    public static void init() {
        RpgNet.registerTypes();
        ServerPlayNetworking.registerGlobalReceiver(RpgNet.Action.ID, (payload, ctx) -> ctx.server().execute(() -> handleAction(ctx.player(), payload.cmd())));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> pendingJoin.put(handler.player.getUuid(), 60));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> { CutsceneDirector.abort(); dlg = null; });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> {
            Session s = RpgRuntime.session();
            if (s == null || Fase01.stage(s) < 0) return;
            double[] c = Fase01.checkpoint(s);
            ServerWorld w = newP.getServerWorld();
            double y = c[1] == 0 ? Fase01.surface(w, (int) c[0], (int) c[2]) : c[1];
            newP.teleport(w, c[0], y, c[2], (float) c[3], 0f);
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient || hand != Hand.MAIN_HAND || !(entity instanceof VerdenNpcEntity npc) || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (npc.getRole().ordinal() < NpcRole.DAN.ordinal()) return ActionResult.PASS; // NPCs de loja seguem o fluxo antigo
            Session s = RpgRuntime.session();
            if (s == null || CutsceneDirector.busy()) return ActionResult.SUCCESS;
            int st = Fase01.stage(s);
            if (npc.getRole() == NpcRole.DAN && st == Fase01.POCAO && s.state.flags.contains("pocao_em_maos")) {
                if (sp.getMainHandStack().isOf(ModItems.POCAO_RECUPERACAO)) {
                    sp.getMainHandStack().decrement(1);
                    ((ServerWorld) world).spawnParticles(net.minecraft.particle.ParticleTypes.HEART, npc.getX(), npc.getY() + 1.6, npc.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
                    s.fire(new GameEvent("use_item", "dan"));
                    afterChange(sp);
                } else sp.sendMessage(Text.literal("Segure a poção avermelhada e use-a em Dan."), true);
                return ActionResult.SUCCESS;
            }
            String pend = pendingDialogue(s, npc.getRole(), st);
            if (pend != null && dlg == null) startDialogue(sp, pend);
            else if (pend == null) sp.sendMessage(Text.literal(npc.getDisplayName().getString() + " não tem mais nada a dizer por ora."), true);
            return ActionResult.SUCCESS;
        });
        ServerTickEvents.END_SERVER_TICK.register(RpgServer::tick);
        CommandRegistrationCallback.EVENT.register(RpgServer::commands);
    }

    /** Diálogo de estágio que pode ser (re)aberto clicando no NPC, caso o jogador tenha fechado com ESC. */
    private static String pendingDialogue(Session s, NpcRole r, int st) {
        if (r == NpcRole.DAN && st == Fase01.POCAO && !s.state.flags.contains("pocao_em_maos")) return "dan_yettus_1";
        if (r == NpcRole.DAN && st == Fase01.CONVERSA) return "dan_yettus_2";
        if (r == NpcRole.NEFERTARI && st == Fase01.AUDIENCIA && s.state.flags.contains("cs_feito:c_audiencia_porta")) return "nefertari_audiencia_1";
        if (r == NpcRole.NEFERTARI && st == Fase01.PROPOSTA) return "nefertari_audiencia_2";
        if (r == NpcRole.SOLDADO && st == Fase01.ESTABULO) return "zigurate_binky";
        return null;
    }

    static void startDialogue(ServerPlayerEntity p, String id) {
        Session s = RpgRuntime.session();
        if (s == null || !s.dialogues.containsKey(id) || dlg != null) return;
        dlg = new DialogueRunner(s, s.dialogues.get(id));
        afterChange(p);
    }

    // ------------------------------------------------------------------ ações do cliente

    static void handleAction(ServerPlayerEntity p, String cmd) {
        Session s = RpgRuntime.session();
        if (s == null) return;
        if (cmd.equals("cine_skip")) { CutsceneDirector.skip(); return; }
        if (CutsceneDirector.busy()) return; // durante cutscene só "pular" é aceito
        try {
            if (cmd.equals("advance") && dlg != null) dlg.advance();
            else if (cmd.startsWith("choose:") && dlg != null) dlg.choose(Integer.parseInt(cmd.substring(7)));
            else if (cmd.equals("close")) dlg = null;
            else if (cmd.startsWith("switch:")) {
                Session.SwitchResult r = s.switchTo(cmd.substring(7), false);
                String msg = switch (r) {
                    case OK -> null;
                    case ALREADY -> "Esta personagem já está sendo controlada.";
                    case LOCKED -> "Ainda não disponível: a história precisa liberar esta personagem.";
                    case BLOCKED_BY_STORY -> "A história não permite trocar de personagem agora.";
                    case NOT_PLAYABLE -> "Esta personagem não é jogável.";
                    default -> "Personagem desconhecida.";
                };
                if (msg != null) p.sendMessage(Text.literal(msg), true);
            } else if (cmd.equals("start")) {
                if (s.state.quests.isEmpty()) Fase01.startNewGame(p, false);
                else p.sendMessage(Text.literal("A campanha deste mundo já foi iniciada. Use \"Reiniciar\" para recomeçar."), true);
                return;
            } else if (cmd.equals("restart")) { Fase01.startNewGame(p, true); return; }
        } catch (RuntimeException e) {
            dlg = null;
            p.sendMessage(Text.literal("Erro no diálogo: " + e.getMessage()));
        }
        afterChange(p);
    }

    private static void afterChange(ServerPlayerEntity p) {
        if (dlg != null && dlg.finished()) dlg = null;
        dirty = true;
        pushDialogue(p);
        push(p);
    }

    // ------------------------------------------------------------------ tick

    private static void tick(net.minecraft.server.MinecraftServer server) {
        Session s = RpgRuntime.session();
        if (s == null) return;
        var it = pendingJoin.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (e.setValue(e.getValue() - 1) - 1 > 0) continue;
            it.remove();
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            if (p == null) continue;
            if (s.state.quests.isEmpty()) Fase01.startNewGame(p, false); // novo jogo: começa pelo prólogo
            else push(p);                                                  // save existente: continua de onde parou
        }
        CutsceneDirector.tick();
        Fase01.tick(server, s);
        if (dlg != null && dlg.finished()) { dlg = null; dirty = true; for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) pushDialogue(p); }
        while (!s.notifications.isEmpty()) {
            Notification n = s.notifications.poll();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", n.kind().name());
            m.put("text", n.text());
            toasts.add(m);
            dirty = true;
        }
        if (dirty) {
            dirty = false;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) push(p);
        }
    }

    // ------------------------------------------------------------------ sincronização

    private static String name(Session s, String speaker) {
        CharacterDef c = s.characters.get(speaker);
        if (c != null) return c.name();
        return switch (speaker) {
            case "nefertari" -> "Rainha Nefertari";
            case "soldado" -> "Soldado";
            default -> "";
        };
    }

    private static final Set<String> PORTRAITS = Set.of("trinity", "dan", "nefertari", "homem_parapeito", "marie", "lena", "narrador");

    static void pushDialogue(ServerPlayerEntity p) {
        Session s = RpgRuntime.session();
        Map<String, Object> m = new LinkedHashMap<>();
        if (dlg == null || s == null || dlg.finished()) m.put("open", false);
        else {
            String sp = dlg.speaker();
            m.put("open", true);
            m.put("speaker", sp);
            m.put("name", name(s, sp));
            m.put("portrait", PORTRAITS.contains(sp) ? sp : "narrador");
            m.put("mood", dlg.mood());
            m.put("text", dlg.text());
            List<Object> opts = new ArrayList<>();
            dlg.options().forEach(o -> opts.add(o.text()));
            m.put("options", opts);
        }
        ServerPlayNetworking.send(p, new RpgNet.Dialogue(Json.write(m)));
    }

    static void push(ServerPlayerEntity p) {
        Session s = RpgRuntime.session();
        if (s == null) return;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("protagonist", s.state.protagonist);
        m.put("chapter", s.state.chapter);
        m.put("started", !s.state.quests.isEmpty());
        m.put("locked", s.state.flags.contains("switch_locked"));
        List<Object> chars = new ArrayList<>();
        for (CharacterDef c : s.characters.values()) {
            if (!c.playable() || c.id().equals("narrador")) continue; // o narrador de Karamarraw não tem identidade/painel
            CharacterSheet sh = s.state.sheet(c.id());
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", c.id());
            o.put("name", c.name());
            o.put("available", s.eval(c.unlock()));
            o.put("active", c.id().equals(s.state.protagonist));
            o.put("level", sh.level);
            o.put("xp", sh.xp);
            o.put("xpNext", CharacterSheet.xpToNext(sh.level));
            o.put("points", sh.skillPoints);
            var of = s.currentOutfit(c.id());
            o.put("outfit", of == null ? "" : of.name());
            chars.add(o);
        }
        m.put("chars", chars);
        List<Object> quests = new ArrayList<>();
        for (Quest q : s.quests.all()) {
            String st = s.quests.statusOf(q.id());
            if (st.equals("none")) continue;
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", q.id());
            o.put("title", q.title());
            o.put("category", q.category().name());
            o.put("status", st);
            o.put("summary", q.summary());
            o.put("lines", new ArrayList<>(s.quests.journalLines(q.id())));
            quests.add(o);
        }
        m.put("quests", quests);
        m.put("codex", new ArrayList<>(s.state.codex));
        m.put("points", new ArrayList<>());
        Object[] g = Fase01.goal(s, p);
        if (g != null) { Map<String, Object> go = new LinkedHashMap<>(); go.put("x", g[0]); go.put("z", g[1]); go.put("label", g[2]); m.put("goal", go); }
        m.put("toasts", new ArrayList<>(toasts));
        toasts.clear();
        ServerPlayNetworking.send(p, new RpgNet.State(Json.write(m)));
    }

    // ------------------------------------------------------------------ comandos

    private static void commands(CommandDispatcher<ServerCommandSource> d, CommandRegistryAccess reg, CommandManager.RegistrationEnvironment env) {
        d.register(CommandManager.literal("verden").then(CommandManager.literal("rpg")
                .then(CommandManager.literal("start").executes(c -> { handleAction(c.getSource().getPlayerOrThrow(), "start"); return 1; }))
                .then(CommandManager.literal("restart").executes(c -> { handleAction(c.getSource().getPlayerOrThrow(), "restart"); return 1; }))
                .then(CommandManager.literal("save").executes(c -> {
                    boolean ok = RpgRuntime.saveNow("comando");
                    c.getSource().sendFeedback(() -> Text.literal(ok ? "Campanha salva." : "Falha ao salvar (veja o log)."), false);
                    return ok ? 1 : 0;
                }))
                .then(CommandManager.literal("status").executes(c -> {
                    Session s = RpgRuntime.session();
                    String txt = s == null ? "Sem campanha ativa." : "Protagonista: " + s.state.protagonist + " | estágio " + Fase01.stage(s) + "/17 | cutscene ativa: " + CutsceneDirector.busy()
                            + " | flags: " + s.state.flags;
                    c.getSource().sendFeedback(() -> Text.literal(txt), false);
                    return 1;
                }))
                .then(CommandManager.literal("skip").executes(c -> { CutsceneDirector.skip(); return 1; }))
                .then(CommandManager.literal("tp").requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("lugar", StringArgumentType.word()).executes(c -> {
                            Session s = RpgRuntime.session();
                            ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                            ServerWorld w = p.getServerWorld();
                            Integer ay = s == null ? null : s.state.vars.get("slice_ay");
                            double[] pos = switch (StringArgumentType.getString(c, "lugar")) {
                                case "praia" -> new double[]{KarBuilder.LX + 0.5, Fase01.surface(w, KarBuilder.LX, KarBuilder.cz + 4), KarBuilder.cz + 4.5};
                                case "mata" -> new double[]{KarBuilder.LX + 14.5, Fase01.surface(w, KarBuilder.LX + 14, KarBuilder.cz + 85), KarBuilder.cz + 85.5};
                                case "navio" -> new double[]{KarBuilder.LX + 0.5, KarBuilder.SEA + 1, KarBuilder.cz - 24.5};
                                case "yettus" -> new double[]{Fase01.YSX + 0.5, Fase01.surface(w, Fase01.YSX, Fase01.YSZ), Fase01.YSZ + 0.5};
                                case "portao" -> new double[]{Fase01.FX - 100.5, Fase01.surface(w, Fase01.FX - 100, Fase01.FZ), Fase01.FZ + 0.5};
                                case "zigurate" -> ay == null ? null : new double[]{AX + 0.5, ay + 1, AZ - 20};
                                case "salao" -> ay == null ? null : new double[]{AX + 0.5, ay + 1, AZ - 10};
                                default -> null;
                            };
                            if (pos == null) { p.sendMessage(Text.literal("Lugares: praia, mata, navio, yettus, portao, zigurate, salao")); return 0; }
                            p.teleport(w, pos[0], pos[1], pos[2], p.getYaw(), 0f);
                            return 1;
                        })))));
    }
}
