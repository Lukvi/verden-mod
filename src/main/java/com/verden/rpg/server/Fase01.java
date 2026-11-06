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
    private static final Random RND = new Random();
    private static final Set<String> inside = new HashSet<>();
    private static String lastTrack;
    private static int lastTrackTick;

    static int stage(Session s) {
        StoryState.QuestProgress q = s.state.quests.get(Q);
        if (q == null) return -1;
        return q.status == StoryState.QuestProgress.Status.DONE ? FIM : q.stage;
    }

    /** Gera/carrega os chunks em volta ANTES do fade-in (evita mundo vazio/tela preta depois de um teleporte para área nova). */
    static void preload(ServerWorld w, int bx, int bz, int r) {
        for (int cx = (bx >> 4) - r; cx <= (bx >> 4) + r; cx++) for (int cz2 = (bz >> 4) - r; cz2 <= (bz >> 4) + r; cz2++) w.getChunk(cx, cz2);
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
        orient(e, yaw);
        e.setPersistent();
        e.setInvulnerable(true);
        e.addCommandTag(tag);
        e.setAiDisabled(still);
        if (role == NpcRole.NARBER || role == NpcRole.NARBER_COMUM) { e.setCustomName(Text.literal("Narber")); e.setCustomNameVisible(true); }
        w.spawnEntity(e);
        return e;
    }

    /** NPCs são desenhados com headYaw/bodyYaw (não só yaw): sem isto todo NPC estático nasce olhando para o sul. */
    static void orient(net.minecraft.entity.LivingEntity e, float yaw) {
        e.setYaw(yaw);
        e.setHeadYaw(yaw);
        e.setBodyYaw(yaw);
        e.prevYaw = yaw;
        e.prevHeadYaw = yaw;
        e.prevBodyYaw = yaw;
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
        lastStage = -1;
        narberReset();
        KarBuilder.locate(w); // acha a costa norte da Karamarraw REAL (o terreno não é alterado)
        preload(w, KarBuilder.LX, KarBuilder.cz - 24, 7);
        if (!s.state.vars.containsKey("kar_built")) {
            p.sendMessage(Text.literal("Preparando Karamarraw… (navio, praia e mata; alguns segundos)").formatted(Formatting.GRAY), false);
            KarBuilder.buildAll(w);
            s.state.vars.put("kar_built", 1);
        }
        if (w.getBlockState(new BlockPos(KarBuilder.RX, KarBuilder.RY, KarBuilder.RZ)).isAir()) KarBuilder.buildRoom(w);
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

    private static int lastStage = -1, lastStageTick;
    private static final String[] HINT = {"Desça pela prancha até a praia.", "Siga a trilha para o interior com a expedição (veja a bússola no topo).", "Continue pela trilha: algo está errado na mata.",
            "Os tiros vêm de mais à frente. Aproxime-se com cuidado e agache-se (Shift) perto do ponto marcado.", "Fique agachado (Shift), longe da vista de Narber, até ele se afastar.", "Narber se foi. Volte pela trilha até o navio."};

    static void tick(MinecraftServer server, Session s) {
        int t = server.getTicks();
        int st = stage(s);
        if (st < 0) return;
        if (st != lastStage) { lastStage = st; lastStageTick = t; }
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!CutsceneDirector.busy()) {
                String nx = nextAuto(s);
                if (nx != null) CutsceneDirector.start(s, nx, p);
            }
            if (CutsceneDirector.busy()) continue;
            KarBuilder.locate(p.getServerWorld());
            expedicao(p, s, st, t);
            narberTick(p, s, st, t);
            if (t % 5 == 0 && !RpgServer.dialogueActive()) zones(p, s, st);
            if (!RpgServer.dialogueActive()) autoDialogs(p, s, st);
            if (t % 10 == 0) {
                atmosphere(p, s, st, t);
                followDan(p, s, st);
                binkyAoEstabulo(p, s);
                music(p, st, t);
                if (st >= 0 && st < HINT.length && t - lastStageTick > 1200 && (t - lastStageTick) % 600 < 10)
                    p.sendMessage(Text.literal("Dica: " + HINT[st]).formatted(Formatting.GOLD), true);
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

    private static Zone kz(String id, double lx, double lz, double r) { return new Zone(id, KarBuilder.LX + lx - r, KarBuilder.LX + lx + r, -64, 320, KarBuilder.cz + lz - r, KarBuilder.cz + lz + r); }

    private static List<Zone> zones(Session s) {
        int[][] T = KarBuilder.TRAIL;
        List<Zone> l = new ArrayList<>(List.of(kz("karamarraw_desembarque", 0, 8, 12), kz("karamarraw_mata_entrada", T[1][0], T[1][1], 14), kz("karamarraw_mata_funda", T[2][0], T[2][1], 14),
                kz("karamarraw_origem_disparo", T[3][0], T[3][1], 12),
                new Zone("karamarraw_navio", KarBuilder.LX - 4, KarBuilder.LX + 5, KarBuilder.SEA - 2, KarBuilder.SEA + 10, KarBuilder.cz - 34, KarBuilder.cz - 12),
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

    private static double lx(ServerPlayerEntity p) { return p.getX() - KarBuilder.LX; }

    private static double lz(ServerPlayerEntity p) { return p.getZ() - KarBuilder.cz; }

    /** Tensão 0..1 conforme o jogador avança pela trilha (z 40→130). Cai sozinha ao voltar para a praia. */
    private static double intensity(ServerPlayerEntity p, int st) {
        if (st < EXPEDICAO || st > NAVIO) return 0;
        double k = Math.max(0, Math.min(1, (lz(p) - 40) / 90.0));
        if (st == ESCONDER) k = Math.max(k, 0.8);
        return k;
    }

    private static int gritoCd, visaoCd;

    /** Escalada: sussurros → gritos distantes → vultos → mal-estar/escuridão → (estágio 3) primeiro tiro. */
    private static void atmosphere(ServerPlayerEntity p, Session s, int st, int t) {
        ServerWorld w = p.getServerWorld();
        double k = intensity(p, st);
        if (k <= 0.05) return;
        if (t % 40 == 0) w.setTimeOfDay(4000 + (long) (8000 * k));
        if (k > 0.4 && !w.isRaining()) w.setWeather(0, 24000, true, k > 0.8);
        w.spawnParticles(ParticleTypes.ASH, p.getX(), p.getY() + 1.2, p.getZ(), (int) (6 * k) + 1, 4, 2, 4, 0.01);
        if (k > 0.3) w.spawnParticles(ParticleTypes.WHITE_ASH, p.getX(), p.getY() + 1.5, p.getZ(), 3, 5, 2.5, 5, 0.0);
        if (RND.nextInt(10) < 1 + (int) (4 * k)) {
            double ang = RND.nextDouble() * Math.PI * 2, d = 6 + RND.nextDouble() * 5;
            w.playSound(null, p.getX() + Math.cos(ang) * d, p.getY() + 1.5, p.getZ() + Math.sin(ang) * d, ModSounds.RPG_SUSSURROS, SoundCategory.AMBIENT, 1.4f, 0.85f + RND.nextFloat() * 0.3f);
        }
        if (k > 0.3 && --gritoCd <= 0) { // gritos longínquos
            gritoCd = 14 + RND.nextInt(14);
            double ang = RND.nextDouble() * Math.PI * 2, d = 30 + RND.nextDouble() * 30;
            w.playSound(null, p.getX() + Math.cos(ang) * d, p.getY() + 2, p.getZ() + Math.sin(ang) * d, ModSounds.RPG_GRITO, SoundCategory.AMBIENT, 3f, 0.8f + RND.nextFloat() * 0.4f);
        }
        if (k > 0.45 && --visaoCd <= 0) { // vultos: colunas de almas que surgem e somem entre as árvores
            visaoCd = 10 + RND.nextInt(10);
            double ang = Math.toRadians(p.getYaw() + 90 + RND.nextInt(120) - 60), d = 12 + RND.nextDouble() * 10;
            double vx = p.getX() + Math.cos(ang) * d, vz = p.getZ() + Math.sin(ang) * d;
            int vy = (int) Math.floor(KarBuilder.top(w, vx - KarBuilder.LX, vz - KarBuilder.cz));
            w.spawnParticles(ParticleTypes.SOUL, vx, vy + 1.0, vz, 14, 0.25, 0.9, 0.25, 0.01);
            w.spawnParticles(ParticleTypes.SCULK_SOUL, vx, vy + 1.6, vz, 4, 0.2, 0.5, 0.2, 0.02);
        }
        double rad = Math.toRadians(p.getYaw() + 180);
        if (k > 0.35 && RND.nextInt(22) == 0) { // passos atrás do jogador (sempre a mesma distância: quem anda é "algo" que o segue)
            w.playSound(null, p.getX() - Math.sin(rad) * 3.5, p.getY(), p.getZ() + Math.cos(rad) * 3.5, SoundEvents.BLOCK_GRASS_STEP, SoundCategory.AMBIENT, 1.6f, 0.7f + RND.nextFloat() * 0.2f);
        }
        if (k > 0.4 && RND.nextInt(40) == 0) { // galho que se parte ao longe
            double a2 = RND.nextDouble() * Math.PI * 2, d2 = 8 + RND.nextDouble() * 8;
            w.playSound(null, p.getX() + Math.cos(a2) * d2, p.getY() + 1, p.getZ() + Math.sin(a2) * d2, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.AMBIENT, 1.4f, 0.6f);
        }
        if (k > 0.5 && t % 30 == 0) w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, (float) (0.3 + 0.9 * k), 1f); // batimento cardíaco
        if (k > 0.55 && t % 200 == 0) p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, true, false, false));
        if (k > 0.8 && t % 400 == 0) p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0, true, false, false));
    }

    private static void music(ServerPlayerEntity p, int st, int t) {
        String want = null; int len = 0;
        if (st >= DESEMBARQUE && st <= NAVIO) { boolean tense = intensity(p, st) > 0.3 || st == DISPAROS || st == ESCONDER; want = tense ? "verden:rpg.tensao" : "verden:rpg.mar"; len = tense ? 28 : 38; }
        else if (st >= FERIDO && st <= CAMINHO) { want = "verden:rpg.vento"; len = 28; }
        if (want == null) return;
        if (!want.equals(lastTrack) || t - lastTrackTick > len * 20) {
            CutsceneDirector.send(p, CutsceneDirector.m("cmd", "music", "id", want));
            lastTrack = want;
            lastTrackTick = t;
        }
    }

    // ------------------------------------------------------------------ expedição: NPCs por script (sem pathfinding)

    /** Move o NPC em linha reta até (tx,tz) acompanhando o relevo; a animação de andar vem da própria variação de posição. */
    static boolean step(ServerWorld w, VerdenNpcEntity e, double tx, double tz, double speed) {
        double dx = tx - e.getX(), dz = tz - e.getZ(), d = Math.hypot(dx, dz);
        if (d < 0.05) return true;
        double m = Math.min(speed, d), nx = e.getX() + dx / d * m, nz = e.getZ() + dz / d * m;
        int bx = (int) Math.floor(nx), bz = (int) Math.floor(nz);
        w.getChunk(bx >> 4, bz >> 4);
        double ny = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        e.refreshPositionAndAngles(nx, ny, nz, yaw, 0f);
        orient(e, yaw);
        return d - m < 0.05;
    }

    static void face(VerdenNpcEntity e, double tx, double tz) {
        orient(e, (float) Math.toDegrees(Math.atan2(-(tx - e.getX()), tz - e.getZ())));
    }

    private static final double[][][] ROTINA = {
            {{3, 11}},                               // 0 guia: espera no começo da trilha, olhando para o interior
            {{0.5, -12}, {-5.5, 6}},                 // 1 leva carga do navio aos suprimentos
            {{1.5, -12}, {5.5, 7}},                  // 2 idem, lado leste
            {{-12, 3}, {12, 3}},                     // 3 vigia a linha d'água
            {{-8, 9}, {-5, 5}},                      // 4 confere os caixotes
            {{-2, 9}, {-1, 5}}};                     // 5 Narber: também trabalha com a carga
    private static final java.util.Map<java.util.UUID, int[]> RS = new java.util.HashMap<>();
    private static int leadIdx = 1;
    /** 0 = membro do grupo; 1 = afastando-se; 2 = na clareira (possuído); 3 = saindo. */
    private static int narberPhase;
    private static boolean firstShot;
    private static int nShot, nAlertCd, nPath, nTimer, nPause, nWait;
    private static double[] nTarget;

    private static int expIndex(VerdenNpcEntity e) {
        for (String tg : e.getCommandTags()) if (tg.startsWith("exp") && tg.length() == 4) return tg.charAt(3) - '0';
        return 0;
    }

    private static void narberReset() { narberPhase = 0; firstShot = false; nShot = 60; nAlertCd = 0; nPath = 0; nTimer = 0; nPause = 0; nWait = 0; nTarget = null; leadIdx = 1; RS.clear(); }

    private static void expedicao(ServerPlayerEntity p, Session s, int st, int t) {
        if (st < DESEMBARQUE || st > ESCONDER) return;
        ServerWorld w = p.getServerWorld();
        BlockPos mid = new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 60);
        List<VerdenNpcEntity> l = new ArrayList<>(tagged(w, "verden_expedicao", mid, 260));
        if (narberPhase == 0) l.addAll(tagged(w, "verden_narber_ilha", mid, 260)); // Narber é do grupo até a influência da ilha
        if (l.isEmpty()) return;
        double k = intensity(p, st);
        VerdenNpcEntity leader = null;
        for (VerdenNpcEntity e : l) if (expIndex(e) == 0) leader = e;
        double lpx = KarBuilder.LX, lpz = KarBuilder.cz;
        for (VerdenNpcEntity e : l) {
            e.setAiDisabled(true);
            int i = expIndex(e);
            double ex = e.getX() - lpx, ez = e.getZ() - lpz;
            if (firstShot || st >= DISPAROS) { // primeiro tiro: congelam, agachados, voltados para o som
                e.setSneaking(true);
                face(e, lpx + KarBuilder.NARBER_HOME[0], lpz + KarBuilder.NARBER_HOME[1]);
                continue;
            }
            if (st >= EXPEDICAO && leader != null) { // marcha pela trilha
                if (i == 0) {
                    double[] wp = {KarBuilder.TRAIL[leadIdx][0], KarBuilder.TRAIL[leadIdx][1]};
                    if (leader.distanceTo(p) > 16) { e.setSneaking(false); face(e, p.getX(), p.getZ()); } // espera o jogador
                    else if (step(w, e, lpx + wp[0], lpz + wp[1], 0.15 * (1 - 0.45 * k)) && leadIdx < 2) leadIdx++;
                } else if (i == 5 && k > 0.2) { // Narber fica para trás, parado, encarando a mata
                    face(e, lpx + KarBuilder.NARBER_HOME[0], lpz + KarBuilder.NARBER_HOME[1]);
                    e.setSneaking(false);
                } else {
                    double lx0 = leader.getX() + (i % 2 == 0 ? -2.2 : 2.2), lz0 = leader.getZ() - 2.2 * i;
                    if (Math.hypot(e.getX() - lx0, e.getZ() - lz0) > 1.2) step(w, e, lx0, lz0, 0.17);
                    else if (t % 40 == 0) orient(e, e.getYaw() + RND.nextInt(120) - 60); // olha ao redor
                    e.setSneaking(k > 0.55);
                }
                continue;
            }
            // vida na praia (antes de partir): rotinas de trabalho
            int[] r = RS.computeIfAbsent(e.getUuid(), u -> new int[]{0, 0});
            double[][] pts = ROTINA[Math.min(i, ROTINA.length - 1)];
            double[] tg = pts[r[0] % pts.length];
            if (ez < -13.5 && tg[1] > -3) { // ainda no convés: vai à prancha primeiro
                if (Math.abs(ex - 0.5) > 0.4) step(w, e, lpx + 0.5, e.getZ(), 0.12);
                else step(w, e, lpx + 0.5, lpz - 1.5, 0.12);
                continue;
            }
            if (i == 0) { if (p.distanceTo(e) < 12) face(e, p.getX(), p.getZ()); else if (t % 200 == 0) face(e, lpx, lpz + 140); step(w, e, lpx + tg[0], lpz + tg[1], 0.1); continue; }
            if (Math.hypot(ex - tg[0], ez - tg[1]) > 0.5) { step(w, e, lpx + tg[0], lpz + tg[1], 0.1); e.setSneaking(false); }
            else {
                e.setSneaking(i == 4 || i == 1 || i == 2 || i == 5); // abaixam para pegar/conferir carga
                if (r[1] == 0) r[1] = 50 + RND.nextInt(60);
                if (--r[1] <= 0) { r[0]++; r[1] = 0; }
            }
        }
    }

    // ------------------------------------------------------------------ Narber (movimento por script; presente desde o início; nunca bloqueia a missão)

    private static void narberFire(ServerWorld w, VerdenNpcEntity n) {
        orient(n, RND.nextInt(360));
        w.playSound(null, n.getX(), n.getY() + 1, n.getZ(), ModSounds.RPG_TIRO, SoundCategory.HOSTILE, 8f, 0.9f + RND.nextFloat() * 0.25f);
        w.spawnParticles(ParticleTypes.POOF, n.getX(), n.getY() + 1.3, n.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
        w.spawnParticles(ParticleTypes.SMOKE, n.getX(), n.getY() + 1.4, n.getZ(), 4, 0.2, 0.1, 0.2, 0.01);
    }

    private static void narberTick(ServerPlayerEntity p, Session s, int st, int t) {
        ServerWorld w = p.getServerWorld();
        double hx = KarBuilder.LX + KarBuilder.NARBER_HOME[0] + 0.5, hz = KarBuilder.cz + KarBuilder.NARBER_HOME[1] + 0.5;
        BlockPos mid = new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 60);
        if (st < DESEMBARQUE || st > ESCONDER) {
            VerdenNpcEntity old = one(w, "verden_narber_ilha", mid, 260);
            if (old != null && st > ESCONDER) old.discard();
            return;
        }
        VerdenNpcEntity n = one(w, "verden_narber_ilha", mid, 260);
        if (n == null) {
            if (s.state.flags.contains("narber_fim") || t % 20 != 0) return;
            // recuperação: se por qualquer motivo ele não existe, recria já na fase correta (nunca fica ausente)
            boolean pos = narberPhase >= 2 || st >= DISPAROS;
            double y = KarBuilder.top(w, KarBuilder.NARBER_HOME[0], KarBuilder.NARBER_HOME[1]);
            n = spawn(w, pos ? NpcRole.NARBER : NpcRole.NARBER_COMUM, "verden_narber_ilha", hx, y, hz, 180f, true);
            if (n == null) return;
            if (pos) { narberPhase = Math.max(narberPhase, 2); firstShot = true; }
        }
        n.setAiDisabled(true);
        double k = intensity(p, st);
        if (narberPhase == 0 && (k > 0.38 || st >= MATA)) narberPhase = 1; // a influência o chama para dentro da mata
        if (narberPhase == 1) {
            if (st >= DISPAROS) nWait++;
            if (step(w, n, hx, hz, 0.11) || nWait > 400 || st >= ESCONDER) {
                if (st >= ESCONDER || nWait > 400) n.refreshPositionAndAngles(hx, KarBuilder.top(w, KarBuilder.NARBER_HOME[0], KarBuilder.NARBER_HOME[1]), hz, 180f, 0f);
                narberPhase = 2;
                n.setVariant(NpcRole.NARBER.ordinal()); // pele pálida e olhos vermelhos (texto do prólogo)
                n.setSneaking(false);
                firstShot = true;
                nShot = 5;
                p.sendMessage(Text.literal("Um disparo ecoa pela mata.").formatted(Formatting.DARK_RED), true);
            }
            return;
        }
        if (narberPhase == 0) return;
        if (st == ESCONDER && narberPhase == 2) narberPhase = 3;
        if (--nShot <= 0) { narberFire(w, n); nShot = 60 + RND.nextInt(60); }
        if (narberPhase == 2) { // vaga pela clareira atirando em qualquer coisa
            if (nPause > 0) nPause--;
            else {
                if (nTarget == null || Math.hypot(n.getX() - nTarget[0], n.getZ() - nTarget[1]) < 0.6) {
                    nTarget = new double[]{hx + RND.nextInt(15) - 7, hz + RND.nextInt(15) - 7};
                    nPause = RND.nextInt(50);
                }
                step(w, n, nTarget[0], nTarget[1], 0.09);
            }
        } else { // fase 3: afasta-se; termina por tempo mesmo que o jogador falhe em se esconder
            nTimer++;
            int[] pt = KarBuilder.NARBER_PATH[Math.min(nPath, KarBuilder.NARBER_PATH.length - 1)];
            if (step(w, n, KarBuilder.LX + pt[0], KarBuilder.cz + pt[1], 0.16)) nPath++;
            if (nPath >= KarBuilder.NARBER_PATH.length || nTimer > 1500) {
                n.discard();
                s.state.flags.add("narber_fim");
                s.fire(new GameEvent("hide", "narber_afastou"));
                p.sendMessage(Text.literal("Os passos se afastam. Volte ao navio.").formatted(Formatting.GRAY), true);
                RpgServer.markDirty();
                return;
            }
        }
        if (t % 10 == 0) {
            if (nAlertCd > 0) { nAlertCd -= 10; return; }
            double d = n.distanceTo(p);
            boolean visto = d < 4.5 || (d < 22 && !p.isSneaking() && n.canSee(p));
            if (visto) {
                nAlertCd = 120;
                narberFire(w, n);
                var away = p.getPos().subtract(n.getPos()).normalize();
                p.setVelocity(away.x * 1.1, 0.45, away.z * 1.1);
                p.velocityModified = true;
                p.sendMessage(Text.literal("Narber atirou em sua direção! Agache-se (Shift) e fique fora da vista dele.").formatted(Formatting.RED), true);
            }
        }
    }

    /** Alvo da bússola da HUD (null = nenhum). Devolve {x, z, rótulo}. */
    static Object[] goal(Session s, ServerPlayerEntity p) {
        KarBuilder.locate(p.getServerWorld());
        int st = stage(s);
        int[][] T = KarBuilder.TRAIL;
        Integer ay = s.state.vars.get("slice_ay");
        double ax = RpgServer.AX, az = RpgServer.AZ;
        return switch (st) {
            case DESEMBARQUE -> new Object[]{KarBuilder.LX + 0.5, KarBuilder.cz + 6.0, "Praia"};
            case EXPEDICAO -> new Object[]{KarBuilder.LX + T[1][0] + 0.5, KarBuilder.cz + T[1][1] + 0.5, "Trilha"};
            case MATA -> new Object[]{KarBuilder.LX + T[2][0] + 0.5, KarBuilder.cz + T[2][1] + 0.5, "Trilha"};
            case DISPAROS -> new Object[]{KarBuilder.LX + T[3][0] + 0.5, KarBuilder.cz + T[3][1] + 0.5, "Origem dos tiros"};
            case NAVIO -> new Object[]{KarBuilder.LX + 0.5, KarBuilder.cz - 13.0, "Navio"};
            case FERIDO, POCAO -> new Object[]{FX - 200.0, FZ + 4.0, "Homem ferido"};
            case CAMINHO -> new Object[]{FX - 108.0, (double) FZ, "Flerin"};
            case CIDADE -> new Object[]{ax, az - 19.0, "Zigurate"};
            case ESTABULO -> new Object[]{ax, az - 19.0, "Soldado"};
            case AUDIENCIA -> new Object[]{ax, az - 11.0, "Sala do trono"};
            case CORREDORES -> new Object[]{ax - 13.0, az + 5.0, "Corredores"};
            default -> null;
        };
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
                p.teleport(w, KarBuilder.LX + 0.5, KarBuilder.SEA + 1, KarBuilder.cz - 24.5, 0f, 0f);
                w.setTimeOfDay(4000);
                w.setWeather(24000, 0, false, false);
                clearTag(w, "verden_narber_ilha", new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 60), 260);
                narberReset();
                for (int i = 0; i < 5; i++) spawnExped(w, i, 0);
                spawn(w, NpcRole.NARBER_COMUM, "verden_narber_ilha", KarBuilder.LX + 1.7, KarBuilder.SEA + 1, KarBuilder.cz - 20.5, 0f, true).addCommandTag("exp5"); // Narber embarca com a expedição
                s.state.flags.remove("narber_fim");
            }
            case "entregar_controle" -> p.sendMessage(Text.literal(stage(s) >= FERIDO ? "Cavalgue para o leste, rumo às muralhas de Flerin." : "Desça à praia e acompanhe a expedição.").formatted(Formatting.GOLD), true);
            case "navio_zarpa" -> {
                p.teleport(w, KarBuilder.LX + 0.5, KarBuilder.SEA + 1, KarBuilder.cz - 22.5, 0f, 0f);
                w.setWeather(24000, 0, false, false);
                clearTag(w, "verden_expedicao", new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 40), 200);
                for (int i = 0; i < 4; i++) spawnExped(w, i, 1);
            }
            case "narber_na_praia" -> spawn(w, NpcRole.NARBER, "verden_narber_praia", KarBuilder.LX + 0.5, KarBuilder.top(w, 0, 3), KarBuilder.cz + 3.5, 180f, true);
            case "narber_some" -> {
                VerdenNpcEntity n = one(w, "verden_narber_praia", new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 3), 40);
                if (n != null) { w.spawnParticles(ParticleTypes.POOF, n.getX(), n.getY() + 1, n.getZ(), 12, 0.3, 0.5, 0.3, 0.02); n.discard(); }
            }
            case "montar_relatorio" -> {
                int rx = KarBuilder.RX, ry = KarBuilder.RY, rz = KarBuilder.RZ;
                if (w.getBlockState(new BlockPos(rx, ry, rz)).isAir()) KarBuilder.buildRoom(w);
                clearTag(w, "verden_expedicao", new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 40), 200);
                clearTag(w, "verden_narber_praia", new BlockPos(KarBuilder.LX, KarBuilder.SEA, KarBuilder.cz + 3), 40);
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
        double x = KarBuilder.LX + (i - 2) * 1.2 + 0.5, z = KarBuilder.cz - 23.5 + (i % 2) * 3 + (onDeck == 1 ? 2 : 0);
        double y = KarBuilder.SEA + 1;
        VerdenNpcEntity e = ModEntities.NPC.create(w);
        if (e == null) return;
        w.getChunk((int) x >> 4, (int) z >> 4);
        e.setVariant(NpcRole.EXPEDICIONARIO.ordinal());
        e.refreshPositionAndAngles(x, y, z, onDeck == 1 ? 180f : 0f, 0f);
        e.setPersistent();
        e.setInvulnerable(true);
        e.setAiDisabled(true);
        e.addCommandTag("verden_expedicao");
        e.addCommandTag("exp" + i);
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
        preload(w, YSX, YSZ, 7);
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
        return new double[]{KarBuilder.LX + 0.5, 0, KarBuilder.cz + 4.5, 0};
    }
}
