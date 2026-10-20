package com.verden.world.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lugares do mapa (cidades, vilas, marcos), carregados de sites.json (gerado por tools/map_gen.py). */
public final class Sites {
    private Sites() {}

    public record Site(String id, String name, String kind, int x, int z, int radius, int level) {
        public long seed() {
            return id.hashCode() * 0x9E3779B97F4A7C15L + 12345L;
        }
    }

    private static List<Site> all;

    public static synchronized List<Site> all() {
        if (all == null) {
            List<Site> list = new ArrayList<>();
            try (InputStream in = Sites.class.getResourceAsStream("/data/verden/map/sites.json")) {
                if (in == null) {
                    throw new IllegalStateException("sites.json não encontrado");
                }
                JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonArray arr = root.getAsJsonArray("sites");
                for (JsonElement e : arr) {
                    JsonObject o = e.getAsJsonObject();
                    list.add(new Site(o.get("id").getAsString(), o.get("name").getAsString(), o.get("kind").getAsString(),
                            o.get("x").getAsInt(), o.get("z").getAsInt(), o.get("radius").getAsInt(), o.get("level").getAsInt()));
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            all = Collections.unmodifiableList(list);
        }
        return all;
    }

    public static Site byId(String id) {
        for (Site s : all()) {
            if (s.id().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /** Extensão (em blocos) da construção a partir do centro, incluindo margem. */
    public static int extent(Site s) {
        return s.radius() + 14;
    }
}
