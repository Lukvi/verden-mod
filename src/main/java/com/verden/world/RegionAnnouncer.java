package com.verden.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.verden.world.gen.Region;
import com.verden.world.gen.Sites;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.VerdenChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Mostra o nome do lugar ao entrar numa região, reino, cidade ou mar de Verden (dados do Guia de Cartografia). */
public final class RegionAnnouncer {
    private RegionAnnouncer() {}

    private record Area(String name, boolean realm, int[] xs, int[] zs) {
        boolean contains(double x, double z) {
            boolean in = false;
            for (int i = 0, j = xs.length - 1; i < xs.length; j = i++) {
                if ((zs[i] > z) != (zs[j] > z) && x < (double) (xs[j] - xs[i]) * (z - zs[i]) / (zs[j] - zs[i]) + xs[i]) {
                    in = !in;
                }
            }
            return in;
        }
    }

    private static final Map<UUID, String> LAST = new HashMap<>();
    private static final Map<UUID, Long> WHEN = new HashMap<>();
    private static List<Area> areas, seas;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(RegionAnnouncer::tick);
    }

    private static synchronized void load() {
        if (areas != null) {
            return;
        }
        List<Area> a = new ArrayList<>(), s = new ArrayList<>();
        try (InputStream in = RegionAnnouncer.class.getResourceAsStream("/data/verden/map/regions.json")) {
            if (in != null) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                for (JsonElement e : root.getAsJsonArray("areas")) {
                    JsonObject o = e.getAsJsonObject();
                    a.add(poly(o, o.get("kind").getAsString().equals("reino")));
                }
                for (JsonElement e : root.getAsJsonArray("seas")) {
                    s.add(poly(e.getAsJsonObject(), false));
                }
            }
        } catch (IOException ex) {
            // sem avisos de região
        }
        seas = s;
        areas = a;
    }

    private static Area poly(JsonObject o, boolean realm) {
        JsonArray pts = o.getAsJsonArray("poly");
        int[] xs = new int[pts.size()], zs = new int[pts.size()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = pts.get(i).getAsJsonArray().get(0).getAsInt();
            zs[i] = pts.get(i).getAsJsonArray().get(1).getAsInt();
        }
        return new Area(o.get("name").getAsString(), realm, xs, zs);
    }

    private static void tick(ServerWorld w) {
        if (w.getTime() % 20 != 7 || !(w.getChunkManager().getChunkGenerator() instanceof VerdenChunkGenerator gen)) {
            return;
        }
        load();
        for (ServerPlayerEntity pl : w.getPlayers()) {
            check(w, gen, pl);
        }
    }

    private static void check(ServerWorld w, VerdenChunkGenerator gen, ServerPlayerEntity pl) {
        double x = pl.getX(), z = pl.getZ();
        String realm = null, local = null;
        for (Area a : areas) {
            if (a.contains(x, z)) {
                if (a.realm()) {
                    realm = a.name();
                } else {
                    local = a.name();      // os últimos da lista têm prioridade
                }
            }
        }
        String place = null, sub = realm;
        for (Site s : Sites.all()) {
            if (Math.hypot(x - s.x(), z - s.z()) <= s.radius() + 6 && !s.kind().equals("caverna")) {
                place = s.name();
            }
        }
        boolean ocean = gen.terrain().biomeAt((int) Math.floor(x), (int) Math.floor(z)).isOcean();
        if (place == null) {
            if (ocean) {
                for (Area a : seas) {
                    if (a.contains(x, z)) {
                        place = a.name();
                        sub = null;
                    }
                }
            } else if (local != null) {
                place = local;
            } else if (realm != null) {
                place = realm;
                sub = null;
            }
        }
        if (place == null) {
            return;
        }
        UUID id = pl.getUuid();
        String prev = LAST.get(id);
        long now = w.getTime();
        if (place.equals(prev) || now - WHEN.getOrDefault(id, -1000L) < 160) {
            return;
        }
        LAST.put(id, place);
        WHEN.put(id, now);
        if (prev == null) {
            return;          // primeira leitura da sessão: só memoriza, sem mostrar
        }
        pl.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 50, 20));
        pl.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(sub == null ? "" : sub).formatted(Formatting.GRAY)));
        pl.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(place).formatted(Formatting.GOLD)));
    }
}
