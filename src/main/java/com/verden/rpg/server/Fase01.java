package com.verden.rpg.server;

import com.verden.entity.NpcRole;
import com.verden.entity.VerdenNpcEntity;
import com.verden.registry.ModEntities;
import com.verden.registry.ModItems;
import com.verden.registry.ModSounds;
import com.verden.rpg.RpgRuntime;
import com.verden.rpg.core.GameEvent;
import com.verden.rpg.core.Notification;
import com.verden.rpg.core.Session;
import com.verden.rpg.core.StoryState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Fase 1 — fase_01_karamarraw_flerin (prólogo + capítulo 1 até a saída da sala do trono).
 * Traduz o mundo em eventos da missão (zonas), toca as cutscenes nos estágios certos e conduz NPCs e atmosfera.
 * Os estágios (índices) seguem quests/fase_01_karamarraw_flerin.json.
 */
final class Fase01 {
    private Fase01() {}

    static final String Q = "fase_01_karamarraw_flerin";
    static final int DESEMBARQUE = 0, EXPEDICAO = 1, MATA = 2, DISPAROS = 3, ESCONDER = 4, NAVIO = 5, DESTINO = 6, RELATORIO = 7, TITULO = 8,
            FERIDO = 9, POCAO = 10, CONVERSA = 11, CAMINHO = 12, CIDADE = 13, ESTABULO = 14, AUDIENCIA = 15, PROPOSTA = 16, CORREDORES = 17, FIM = 99;
    static final int FX = 3840, FZ = -584, YSX = FX - 300, YSZ = FZ;
    private static final int KX = KarBuilder.KX, KZ = KarBuilder.KZ, SEA = KarBuilder.SEA;
    private static final Random RND = new Random();
    private static final Set<String> inside = new HashSet<>();
    private static String lastTrack;
    private static int lastTrackTick;

    static int stage(Session s) {
        StoryState.QuestProgress q = s.state.quests.get(Q);
        if (q == null) return -1;
        return q.status == StoryState.QuestProgress.Status.DONE ? FIM : q.stage;
    }

    static int surface(ServerWorld w, int x, int z) {
        w.getChunk(x >> 4, z >> 4);
        return w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    static void musicSent(String id) { lastTrack = id; lastTrackTick = -100000; }

    // ------------------------------------------------------------------ NPCs (sempre com etiqueta, nunca duplicados)

    static VerdenNpcEntity spawn(ServerWorld w, NpcRole role, String tag, double x, double y, double z, float yaw, boolean still) {
        BlockPos at = BlockPos.ofFloored(x, y, z);
        w.getChunk(at.getX() >> 4, at.getZ() >> 4);
        clearTag(w, tag, at, 48);
        VerdenNpcEntity e = ModEntities.NPC.create(w);
        if (e == null) return null;
        e.setVariant(role.ordinal());
        e.refreshPositionAndAngles(x, y, z, yaw, 0f);
        e.setPersistent();
        e.setInvulnerable(true);
        e.addCommandTag(tag);
        e.setAiDisabled(still);
        w.spawnEntity(e);
        return e;
    }

    static List<VerdenNpcEntity> tagged(ServerWorld w, String tag, BlockPos around, int r) {
        w.getChunk(around.getX() >> 4, around.getZ() >> 4);
        return w.getEntitiesByType(ModEntities.NPC, new Box(around).expand(r, 400, r), e -> e.getCommandTags().contains(tag));
    }

    static void clearTag(ServerWorld w, String tag, BlockPos around, int r) {
        for (VerdenNpcEntity e : tagged(w, tag, around, r)) e.discard();
    }

    static VerdenNpcEntity one(ServerWorld w, String tag, BlockPos around, int r) {
        List<VerdenNpcEntity> l = tagged(w, tag, around, r);
        return l.isEmpty() ? null : l.get(0);
    }

    private static HorseEntity binky(ServerWorld w, BlockPos around) {
        List<HorseEntity> l = w.getEntitiesByType(EntityType.HORSE, new Box(around).expand(120, 400, 120), h -> h.getCommandTags().contains("verden_binky"));
        return l.isEmpty() ? null : l.get(0);
    }

    // ------------------------------------------------------------------ novo jogo

    static void startNewGame(ServerPlayerEntity p, boolean reset) {
        Session s = reset ? RpgRuntime.reset() : RpgRuntime.session();
        if (s == null) return;
        ServerWorld w = p.getServerWorld();
        CutsceneDirector.abort();
        RpgServer.closeDialogue();
        inside.clear();
        if (!KarBuilder.oceanoSeguro(w)) {
            p.sendMessage(Text.literal("Karamarraw: a área reservada não é oceano profundo neste mundo. Construção abortada para não alterar o terreno.").formatted(Formatting.RED));
            return;
        }
        boolean ilha = w.getBlockState(new BlockPos(KX, SEA + 1, KZ + 31)).isOf(Blocks.SAND);
        boolean sala = !w.getBlockState(new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ)).isAir();
        if (!ilha) { p.sendMessage(Text.literal("Construindo Karamarraw… (alguns segundos)").formatted(Formatting.GRAY), false); KarBuilder.buildIsland(w); KarBuilder.buildShip(w); }
        if (!sala) KarBuilder.buildRoom(w);
        s.apply(List.of("switch:narrador", "flag:switch_locked"));
        if (stage(s) < 0) s.quests.start(Q);
        w.setWeather(24000, 0, false, false);
        w.setTimeOfDay(4000);
        RpgRuntime.saveNow("novo jogo");
        RpgServer.markDirty();
        // o resto (cutscene c_chegada) é iniciado pelo autoplay no próximo tick
    }

    // ------------------------------------------------------------------ tick

    private static final String[][] AUTO = {{"0", "c_chegada"}, {"6", "c_narber_destino"}, {"7", "c_relatorio"}, {"8", "c_titulo"}, {"8", "c_yettus_intro"}, {"99", "c_fim_fase"}};

    static void tick(MinecraftServer server, Session s) {
        int t = server.getTicks();
        int st = stage(s);
        if (st < 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!CutsceneDirector.busy()) {
                String nx = nextAuto(s);
                if (nx != null) CutsceneDirector.start(s, nx, p);
            }
            if (CutsceneDirector.busy()) continue;
            if (t % 5 == 0 && !RpgServer.dialogueActive()) zones(p, s, st);
            if (!RpgServer.dialogueActive()) autoDialogs(p, s, st);
            if (t % 10 == 0) {
                atmosphere(p, s, st, t);
                expedition(p, s, st);
                narber(p, s, st);
                followDan(p, s, st);
                binkyAoEstabulo(p, s);
                music(p, st, t);
            }
        }
    }

    /** Próxima cutscene automática do estágio atual ainda não exibida (para encadear sem reabrir a tela). */
    static String nextAuto(Session s) {
        int st = stage(s);
        for (String[] a : AUTO)
            if (Integer.parseInt(a[0]) == st && !s.state.flags.contains("cs_feito:" + a[1]) && !(a[1].equals("c_fim_fase") && s.state.flags.contains("fase_01_encerrada"))) return a[1];
        return null;
    }

    // ------------------------------------------------------------------ zonas

    private record Zone(String id, double x1, double x2, double y1, double y2, double z1, double z2) {
        boolean has(double x, double y, double z) { return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2; }
    }

    private static Zone kz(String id, int x1, int x2, int z1, int z2) { return new Zone(id, KX + x1, KX + x2, SEA - 2, SEA + 70, KZ + z1, KZ + z2); }

    private static List<Zone> zones(Session s) {
        List<Zone> l = new ArrayList<>(List.of(kz("karamarraw_desembarque", -7, 7, 24, 32), kz("karamarraw_mata_entrada", -6, 6, 10, 16), kz("karamarraw_mata_funda", -8, 8, -3, 1),
                kz("karamarraw_origem_disparo", -9, 3, -10, -8), new Zone("karamarraw_navio", KX - 3, KX + 3, SEA, SEA + 6, KZ + 38, KZ + 52),
                new Zone("yettus_encontro", FX - 215, FX - 185, -64, 320, FZ - 14, FZ + 20), new Zone("yettus_caminho", FX - 170, FX - 140, -64, 320, FZ - 30, FZ + 30),
                new Zone("flerin_portao", FX - 122, FX - 94, -64, 320, FZ - 16, FZ + 16), new Zone("flerin_rua", FX - 92, FX - 60, -64, 320, FZ - 14, FZ + 14)));
        Integer ay = s.state.vars.get("slice_ay");
        if (ay != null) {
            int ax = RpgServer.AX, az = RpgServer.AZ;
            l.add(new Zone("zigurate_entrada", ax - 7, ax + 7, ay - 3, ay + 8, az - 24, az - 15));
            l.add(new Zone("trono_porta", ax - 3, ax + 3, ay, ay + 6, az - 14, az - 9));
            l.add(new Zone("corredor_saida", ax - 15, ax + 15, ay, ay + 6, az + 5, az + 6));
        }
        return l;
    }

    private static void zones(ServerPlayerEntity p, Session s, int st) {
        for (Zone z : zones(s)) {
            boolean in = z.has(p.getX(), p.getY(), p.getZ());
            String key = p.getUuidAsString() + z.id();
            if (in && inside.add(key)) onZone(z.id(), p, s, st);
            else if (!in) inside.remove(key);
        }
    }

    private static void onZone(String id, ServerPlayerEntity p, Session s, int st) {
        s.fire(new GameEvent("reach", id));
        RpgServer.markDirty();
        int now = stage(s);
        if (id.equals("yettus_caminho") && st == CAMINHO) startOnce(p, s, "dlg_c", "dan_caminho");
        if (id.equals("flerin_rua") && st == CIDADE) startOnce(p, s, "dlg_d", "flerin_rua");
        if (id.equals("trono_porta") && st == AUDIENCIA && !s.state.flags.contains("cs_feito:c_audiencia_porta")) CutsceneDirector.start(s, "c_audiencia_porta", p);
        if (id.startsWith("karamarraw_navio") && now == DESTINO) { /* c_narber_destino inicia pelo autoplay */ }
    }

    private static void startOnce(ServerPlayerEntity p, Session s, String flag, String dialogue) {
        if (s.state.flags.add(flag)) RpgServer.startDialogue(p, dialogue);
    }

    private static void autoDialogs(ServerPlayerEntity p, Session s, int st) {
        if (st == POCAO && s.state.flags.contains("dlg_a") == false && inside.contains(p.getUuidAsString() + "yettus_encontro")) startOnce(p, s, "dlg_a", "dan_yettus_1");
        if (st == CONVERSA) startOnce(p, s, "dlg_b", "dan_yettus_2");
        if (st == ESTABULO) startOnce(p, s, "dlg_e", "zigurate_binky");
        if (st == PROPOSTA) startOnce(p, s, "dlg_f", "nefertari_audiencia_2");
    }

    // ------------------------------------------------------------------ atmosfera de Karamarraw (cenas 1–3)

    private static double intensity(ServerPlayerEntity p, int st) {
        if (st < EXPEDICAO || st > ESCONDER) return 0;
        double lz = p.getZ() - KZ, k = (14 - lz) / 22.0;
        return Math.max(st >= MATA ? 0.45 : 0, Math.min(1, st >= ESCONDER ? 1 : k));
    }

    private static void atmosphere(ServerPlayerEntity p, Session s, int st, int t) {
        ServerWorld w = p.getServerWorld();
        double k = intensity(p, st);
        if (st >= ESCONDER + 1 && st <= DESTINO && k == 0) return;
        if (k <= 0.05) return;
        if (t % 40 == 0) w.setTimeOfDay(4000 + (long) (8500 * k));
        if (k > 0.4 && !w.isRaining()) w.setWeather(0, 24000, true, k > 0.8);
        w.spawnParticles(ParticleTypes.ASH, p.getX(), p.getY() + 1.2, p.getZ(), (int) (6 * k) + 1, 4, 2, 4, 0.01);
        if (k > 0.3) w.spawnParticles(ParticleTypes.WHITE_ASH, p.getX(), p.getY() + 1.5, p.getZ(), 3, 5, 2.5, 5, 0.0);
        if (RND.nextInt(10) < 1 + (int) (4 * k)) {
            double ang = RND.nextDouble() * Math.PI * 2, d = 6 + RND.nextDouble() * 5;
            w.playSound(null, p.getX() + Math.cos(ang) * d, p.getY() + 1.5, p.getZ() + Math.sin(ang) * d, ModSounds.RPG_SUSSURROS, SoundCategory.AMBIENT, 1.4f, 0.85f + RND.nextFloat() * 0.3f);
        }
        if (k > 0.5 && t % 200 == 0) p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, true, false, false));
    }

    private static void music(ServerPlayerEntity p, int st, int t) {
        String want = null; int len = 0;
        if (st >= DESEMBARQUE && st <= ESCONDER) { boolean tense = intensity(p, st) > 0.3 || st >= DISPAROS; want = tense ? "verden:rpg.tensao" : "verden:rpg.mar"; len = tense ? 28 : 38; }
        else if (st >= FERIDO && st <= CAMINHO) { want = "verden:rpg.vento"; len = 28; }
        if (want == null) return;
        if (!want.equals(lastTrack) || t - lastTrackTick > len * 20) {
            CutsceneDirector.send(p, CutsceneDirector.m("cmd", "music", "id", want));
            lastTrack = want;
            lastTrackTick = t;
        }
    }

    // ------------------------------------------------------------------ expedição e Narber

    private static void expedition(ServerPlayerEntity p, Session s, int st) {
        ServerWorld w = p.getServerWorld();
        List<VerdenNpcEntity> l = tagged(w, "verden_expedicao", p.getBlockPos(), 80);
        double k = intensity(p, st);
        for (VerdenNpcEntity e : l) {
            e.setAiDisabled(false);
            if (st == EXPEDICAO || st == MATA) {
                if (e.squaredDistanceTo(p) > 36) e.getNavigation().startMovingTo(p.getX() + RND.nextInt(5) - 2, p.getY(), p.getZ() + RND.nextInt(5) - 2, 1.0);
                if (e.squaredDistanceTo(p) > 40 * 40) e.refreshPositionAndAngles(p.getX() - 2, p.getY(), p.getZ() + 3, e.getYaw(), 0);
                e.setSneaking(k > 0.5 && RND.nextInt(3) == 0); // desconforto: encolhem-se
            } else {
                e.getNavigation().stop();
                e.setAiDisabled(true);
            }
        }
    }

    private static final double[][] WP = {{-10, -16}, {-6, -11}, {-2, -8}, {6, -12}, {14, -22}, {8, -34}};
    private static int narberWp, narberPause, narberCool;

    private static boolean escondido(ServerPlayerEntity p) {
        if (!p.isSneaking()) return false;
        int cover = 0;
        ServerWorld w = p.getServerWorld();
        BlockPos b = p.getBlockPos();
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) for (int dy = 0; dy <= 2; dy++) {
            var bs = w.getBlockState(b.add(dx, dy, dz));
            if (bs.isOf(Blocks.MOSSY_COBBLESTONE) || bs.isIn(BlockTags.LOGS) || bs.isIn(BlockTags.LEAVES)) cover++;
        }
        return cover >= 7;
    }

    private static void narber(ServerPlayerEntity p, Session s, int st) {
        ServerWorld w = p.getServerWorld();
        BlockPos kc = new BlockPos(KX, SEA + 5, KZ);
        VerdenNpcEntity n = one(w, "verden_narber_ilha", kc, 70);
        if (st == DISPAROS && n == null && !s.state.flags.contains("narber_ativo")) {
            double y = SEA + KarBuilder.ih((int) WP[0][0], (int) WP[0][1]) + 1;
            n = spawn(w, NpcRole.NARBER, "verden_narber_ilha", KX + WP[0][0] + 0.5, y, KZ + WP[0][1] + 0.5, 180f, false);
            s.state.flags.add("narber_ativo");
            narberWp = 0; narberPause = 0; narberCool = 0;
            w.playSound(null, KX + WP[0][0], y, KZ + WP[0][1], ModSounds.RPG_TIRO, SoundCategory.HOSTILE, 5f, 1f);
        }
        if (n == null) return;
        if (st != DISPAROS && st != ESCONDER) { n.discard(); return; }
        n.setAiDisabled(false);
        double dist = n.distanceTo(p);
        boolean vejo = dist < 16 && n.canSee(p) && !escondido(p);
        if (narberCool > 0) narberCool--;
        if ((dist < 4.5 || vejo) && narberCool == 0 && st >= DISPAROS) {
            narberCool = 6;
            w.playSound(null, n.getX(), n.getY() + 1, n.getZ(), ModSounds.RPG_TIRO, SoundCategory.HOSTILE, 4f, 1.1f);
            p.sendMessage(Text.literal("Narber viu você! Corra para o fundo da mata e esconda-se (agache atrás de pedras ou árvores).").formatted(Formatting.RED), true);
            p.teleport(w, KX + 0.5, SEA + KarBuilder.ih(0, 6) + 1, KZ + 6.5, 180f, 0f);
            n.refreshPositionAndAngles(KX + WP[0][0] + 0.5, n.getY(), KZ + WP[0][1] + 0.5, 180f, 0f);
            narberWp = 0; narberPause = 0;
            return;
        }
        if (narberPause > 0) {
            narberPause--;
            return;
        }
        double[] wp = WP[Math.min(narberWp, WP.length - 1)];
        double tx = KX + wp[0] + 0.5, tz = KZ + wp[1] + 0.5, ty = SEA + KarBuilder.ih((int) wp[0], (int) wp[1]) + 1;
        n.getNavigation().startMovingTo(tx, ty, tz, 0.95);
        if (Math.hypot(n.getX() - tx, n.getZ() - tz) < 2.2) {
            narberWp++;
            narberPause = 3;
            n.setYaw(RND.nextInt(360));
            w.playSound(null, n.getX(), n.getY() + 1, n.getZ(), ModSounds.RPG_TIRO, SoundCategory.HOSTILE, 4f, 0.9f + RND.nextFloat() * 0.3f);
            w.spawnParticles(ParticleTypes.POOF, n.getX(), n.getY() + 1.2, n.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
            if (narberWp >= WP.length) {
                n.discard();
                s.fire(new GameEvent("hide", "narber_afastou"));
                p.sendMessage(Text.literal("Os passos se afastam. Volte ao navio.").formatted(Formatting.GRAY), true);
                RpgServer.markDirty();
            }
        }
    }

    // ------------------------------------------------------------------ Dan, Binky

    private static void followDan(ServerPlayerEntity p, Session s, int st) {
        if (st < CAMINHO || st > CORREDORES) return;
        ServerWorld w = p.getServerWorld();
        VerdenNpcEntity dan = one(w, "verden_dan", p.getBlockPos(), 120);
        if (dan == null) return;
        dan.setAiDisabled(false);
        double d = dan.squaredDistanceTo(p);
        if (d > 40 * 40) dan.refreshPositionAndAngles(p.getX() - 2, p.getY(), p.getZ(), dan.getYaw(), 0f);
        else if (d > 3.5 * 3.5 && st != AUDIENCIA && st != PROPOSTA) dan.getNavigation().startMovingTo(p, 1.15);
        else if (st == AUDIENCIA || st == PROPOSTA) dan.getNavigation().stop();
        if (st <= CIDADE && d > 22 * 22) p.sendMessage(Text.literal("Dan vem a pé. Espere por ele.").formatted(Formatting.GRAY), true);
    }

    private static void binkyAoEstabulo(ServerPlayerEntity p, Session s) {
        if (!s.state.flags.contains("binky_no_estabulo") || s.state.flags.contains("binky_ok")) return;
        ServerWorld w = p.getServerWorld();
        HorseEntity b = binky(w, p.getBlockPos());
        p.stopRiding();
        if (b != null) {
            int bx = RpgServer.AX + 20, bz = RpgServer.AZ - 20;
            b.refreshPositionAndAngles(bx + 0.5, surface(w, bx, bz), bz + 0.5, 90f, 0f);
            b.setAiDisabled(true);
        }
        s.state.flags.add("binky_ok");
    }

    // ------------------------------------------------------------------ ações das cutscenes

    static void act(String name, ServerPlayerEntity p, Session s) {
        ServerWorld w = p.getServerWorld();
        switch (name) {
            case "posicionar_chegada" -> {
                p.teleport(w, KX + 0.5, SEA + 2, KZ + 44.5, 180f, 0f);
                w.setTimeOfDay(4000);
                w.setWeather(24000, 0, false, false);
                for (int i = 0; i < 5; i++) spawnExped(w, i, 0);
                clearTag(w, "verden_narber_ilha", new BlockPos(KX, SEA, KZ), 70);
                s.state.flags.remove("narber_ativo");
            }
            case "entregar_controle" -> p.sendMessage(Text.literal(stage(s) >= FERIDO ? "Cavalgue para o leste, rumo às muralhas de Flerin." : "Desça à praia e acompanhe a expedição.").formatted(Formatting.GOLD), true);
            case "navio_zarpa" -> {
                p.teleport(w, KX + 0.5, SEA + 2, KZ + 46.5, 180f, 0f);
                w.setWeather(24000, 0, false, false);
                clearTag(w, "verden_expedicao", new BlockPos(KX, SEA, KZ), 90);
                for (int i = 0; i < 4; i++) spawnExped(w, i, 1);
            }
            case "narber_na_praia" -> spawn(w, NpcRole.NARBER, "verden_narber_praia", KX + 0.5, SEA + 2, KZ + 30.5, 0f, true);
            case "narber_some" -> {
                VerdenNpcEntity n = one(w, "verden_narber_praia", new BlockPos(KX, SEA, KZ + 30), 30);
                if (n != null) { w.spawnParticles(ParticleTypes.POOF, n.getX(), n.getY() + 1, n.getZ(), 12, 0.3, 0.5, 0.3, 0.02); n.discard(); }
            }
            case "montar_relatorio" -> {
                int rx = KarBuilder.RX, ry = KarBuilder.RY, rz = KarBuilder.RZ;
                if (w.getBlockState(new BlockPos(rx, ry, rz)).isAir()) KarBuilder.buildRoom(w);
                clearTag(w, "verden_expedicao", new BlockPos(KX, SEA, KZ), 90);
                clearTag(w, "verden_narber_praia", new BlockPos(KX, SEA, KZ + 30), 40);
                p.teleport(w, rx + 0.5, ry + 1, rz + 7.5, 180f, 0f);
                w.setWeather(24000, 0, false, false);
                spawn(w, NpcRole.MAJESTADE, "verden_rel_majestade", rx + 0.5, ry + 1, rz - 1.5, 0f, true);
                VerdenNpcEntity re = spawn(w, NpcRole.REEVES, "verden_rel_reeves", rx + 0.5, ry + 1, rz + 2.5, 180f, true);
                if (re != null) re.setPose(net.minecraft.entity.EntityPose.CROUCHING);
                spawn(w, NpcRole.SOLDADO, "verden_rel_soldado", rx + 3.5, ry + 1, rz - 1.5, 0f, true);
            }
            case "soldado_entrega" -> {
                VerdenNpcEntity so = one(w, "verden_rel_soldado", new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ), 20);
                if (so != null) { so.setAiDisabled(false); so.getNavigation().startMovingTo(KarBuilder.RX + 1.5, KarBuilder.RY + 1, KarBuilder.RZ + 1.8, 0.8); }
            }
            case "reeves_sai" -> {
                VerdenNpcEntity re = one(w, "verden_rel_reeves", new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ), 20);
                if (re != null) { re.setPose(net.minecraft.entity.EntityPose.STANDING); re.setAiDisabled(false); re.getNavigation().startMovingTo(KarBuilder.RX + 0.5, KarBuilder.RY + 1, KarBuilder.RZ + 8.5, 0.9); }
            }
            case "posicionar_yettus" -> posicionarYettus(p, s, w);
            case "iniciar_audiencia" -> RpgServer.startDialogue(p, "nefertari_audiencia_1");
            case "liberar_proxima" -> {
                s.state.flags.add("fase_01_encerrada");
                p.sendMessage(Text.literal("FIM DA PRIMEIRA FASE").formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
            default -> { }
        }
    }

    private static void spawnExped(ServerWorld w, int i, int onDeck) {
        double x = KX + (i - 2) * 1.4 + 0.5, z = onDeck == 1 ? KZ + 44.5 + (i % 2) * 2 : KZ + 41.5 + (i % 2);
        double y = SEA + 2;
        VerdenNpcEntity e = ModEntities.NPC.create(w);
        if (e == null) return;
        w.getChunk((int) x >> 4, (int) z >> 4);
        e.setVariant(NpcRole.EXPEDICIONARIO.ordinal());
        e.refreshPositionAndAngles(x, y, z, 180f, 0f);
        e.setPersistent();
        e.setInvulnerable(true);
        e.addCommandTag("verden_expedicao");
        w.spawnEntity(e);
    }

    private static void posicionarYettus(ServerPlayerEntity p, Session s, ServerWorld w) {
        clearTag(w, "verden_rel_majestade", new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ), 20);
        clearTag(w, "verden_rel_reeves", new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ), 20);
        clearTag(w, "verden_rel_soldado", new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ), 20);
        Integer ay = s.state.vars.get("slice_ay");
        if (ay == null) {
            int a = surface(w, RpgServer.AX, RpgServer.AZ) - 1;
            new ZigurateBuilder(w, RpgServer.AX, a, RpgServer.AZ).build();
            s.state.vars.put("slice_ay", a);
            ay = a;
        }
        spawn(w, NpcRole.NEFERTARI, "verden_nefertari", RpgServer.AX + 0.5, ay + 1, RpgServer.AZ + 0.5, 180f, true);
        spawn(w, NpcRole.SOLDADO, "verden_soldado_entrada", RpgServer.AX + 3.5, ay + 1, RpgServer.AZ - 19.5, 0f, true);
        int dx = FX - 200, sy = surface(w, YSX, YSZ);
        spawn(w, NpcRole.DAN, "verden_dan", dx + 0.5, surface(w, dx, YSZ + 4), YSZ + 4.5, 90f, true);
        w.setWeather(24000, 0, false, false);
        w.setTimeOfDay(11800);
        p.teleport(w, YSX + 0.5, sy, YSZ + 0.5, 270f, 0f);
        for (HorseEntity h : w.getEntitiesByType(EntityType.HORSE, new Box(p.getBlockPos()).expand(150, 400, 150), h -> h.getCommandTags().contains("verden_binky"))) h.discard();
        HorseEntity b = EntityType.HORSE.create(w);
        if (b != null) {
            b.refreshPositionAndAngles(YSX + 0.5, sy, YSZ + 0.5, 270f, 0f);
            b.setCustomName(Text.literal("Binky"));
            b.setPersistent();
            b.setTame(true);
            b.bondWithPlayer(p);
            b.saddle(new ItemStack(Items.SADDLE), SoundCategory.NEUTRAL);
            b.addCommandTag("verden_binky");
            w.spawnEntity(b);
            p.startRiding(b);
        }
        if (!p.getInventory().contains(new ItemStack(ModItems.POCAO_RECUPERACAO))) p.getInventory().insertStack(new ItemStack(ModItems.POCAO_RECUPERACAO));
    }

    /** Onde reaparecer se o jogador morrer (por estágio). */
    static double[] checkpoint(Session s) {
        int st = stage(s);
        if (st >= FERIDO && st < FIM) return new double[]{YSX + 0.5, 0, YSZ + 0.5, 270};
        return new double[]{KX + 0.5, SEA + KarBuilder.ih(0, 28) + 1, KZ + 28.5, 180};
    }
}
