package com.verden.rpg;

import com.verden.rpg.core.ContentLoader;
import com.verden.rpg.core.SaveManager;
import com.verden.rpg.core.Session;
import com.verden.rpg.core.StoryState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Ponte entre o núcleo do RPG (Java puro, em {@code rpg.core}) e o servidor do Minecraft.
 * Responsabilidades, e só estas:
 *  - criar a {@link Session} quando o mundo abre (carregando a campanha salva, se houver);
 *  - carregar o conteúdo (missões/diálogos) do classpath do mod e validá-lo;
 *  - salvar periodicamente e ao fechar o mundo, sem nunca lançar exceção para o tick do servidor.
 *
 * A campanha é por mundo, em {@code <mundo>/verden_rpg/campaign.json}. Multiplayer não é suportado
 * nesta fase (um estado de campanha por mundo, não por jogador).
 */
public final class RpgRuntime {
    private RpgRuntime() {}

    private static final Logger LOG = LoggerFactory.getLogger("verden/rpg");
    private static final int AUTOSAVE_TICKS = 6000; // 5 minutos

    private static Session session;
    private static Path saveFile;
    /** Falso quando o save veio de uma versão mais nova do mod: nada é gravado para não destruí-lo. */
    private static boolean saveEnabled;

    /** Sessão ativa, ou null se nenhum mundo está aberto. */
    public static Session session() {
        return session;
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(RpgRuntime::onStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(RpgRuntime::onStop);
        ServerTickEvents.END_SERVER_TICK.register(RpgRuntime::onTick);
    }

    private static void onStart(MinecraftServer server) {
        saveFile = server.getSavePath(WorldSavePath.ROOT).resolve("verden_rpg").resolve("campaign.json");
        saveEnabled = true;
        StoryState state = null;
        try {
            SaveManager.LoadResult r = SaveManager.load(saveFile);
            if (r != null) {
                state = r.state();
                for (String w : r.warnings()) LOG.warn("Campanha: {}", w);
                LOG.info("Campanha carregada (origem: {}, capítulo {}).", r.source(), state.chapter);
            }
        } catch (SaveManager.SchemaTooNewException e) {
            saveEnabled = false;
            LOG.error("{} — a campanha ficará desativada neste mundo e o arquivo NÃO será sobrescrito.", e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Falha ao ler a campanha; iniciando uma nova (o arquivo antigo não será apagado).", e);
        }
        session = new Session(state != null ? state : new StoryState());
        try {
            ContentLoader.loadIndex(session, readResource("data/verden/rpg/index.json"), RpgRuntime::readResource);
            List<String> problems = ContentLoader.validate(session);
            for (String p : problems) LOG.warn("Conteúdo do RPG: {}", p);
        } catch (RuntimeException e) {
            LOG.error("Falha ao carregar o conteúdo do RPG; sistemas de missão ficarão vazios.", e);
        }
    }

    private static void onTick(MinecraftServer server) {
        Session s = session;
        if (s == null) return;
        if (server.getTicks() % 20 == 0) s.tick(20);
        if (server.getTicks() % AUTOSAVE_TICKS == 0) saveNow("autosave");
    }

    private static void onStop(MinecraftServer server) {
        saveNow("fechamento");
        session = null;
    }

    /** Grava a campanha agora. Nunca lança exceção; retorna se conseguiu. */
    public static boolean saveNow(String reason) {
        if (session == null || saveFile == null || !saveEnabled) return false;
        try {
            SaveManager.save(saveFile, session.state);
            LOG.debug("Campanha salva ({}).", reason);
            return true;
        } catch (IOException | RuntimeException e) {
            LOG.error("Falha ao salvar a campanha ({}).", reason, e);
            return false;
        }
    }

    private static String readResource(String path) {
        try (InputStream in = RpgRuntime.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("recurso não encontrado: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("erro lendo " + path, e);
        }
    }
}
