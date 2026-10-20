package com.verden.world.obras;

import com.verden.entity.NpcRole;
import com.verden.world.gen.Sites;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.Terrain;
import com.verden.world.gen.VerdenChunkGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/** Constrói os lugares do mapa de Verden: cada chunk aplica a parte da planta que o atinge. */
public final class SiteBuilder {
    private SiteBuilder() {}

    private static final Map<String, Blueprint> CACHE = new ConcurrentHashMap<>();

    public static void placeInChunk(StructureWorldAccess world, Chunk chunk, VerdenChunkGenerator gen) {
        int cx = chunk.getPos().getStartX(), cz = chunk.getPos().getStartZ();
        for (Site s : Sites.all()) {
            int e = Sites.extent(s) + 8;
            if (cx + 15 < s.x() - e || cx > s.x() + e || cz + 15 < s.z() - e || cz > s.z() + e) {
                continue;
            }
            Blueprint bp = CACHE.computeIfAbsent(s.id(), k -> build(s, gen.terrain()));
            bp.apply(world, cx, cz);
        }
    }

    private static void npc(Blueprint b, Site s, int dx, int dz, NpcRole role) {
        b.spawn(s.x() + dx, s.level() + 1, s.z() + dz, Blueprint.Mob.NPC, role.ordinal());
    }

    private static void inHome(Blueprint b, Lay.Homes h, int idx, NpcRole role) {
        if (h.list.isEmpty()) {
            return;
        }
        int[] q = h.list.get(Math.min(idx, h.list.size() - 1));
        b.spawn(q[0] + 3, q[2] + 1, q[1] + 3, Blueprint.Mob.NPC, role.ordinal());
    }

    static Blueprint build(Site s, Terrain t) {
        Random r = new Random(s.seed());
        Blueprint b = new Blueprint();
        int cx = s.x(), cz = s.z(), g = s.level();
        switch (s.kind()) {
            case "capital_norte" -> {
                Pal p = Pal.norte(), nb = Pal.nobreNorte();
                Lay.Homes h = Lay.capital(b, s, p, nb, r, s.radius() - 8, 9, 38);
                Landmarks.partenon(b, cx, cz, g, nb);
                inHome(b, h, 2, NpcRole.TAVERNEIRO);
                for (int k = -2; k <= 2; k++) {
                    b.spawn(cx + k * 12, g + 1, cz + 44, Blueprint.Mob.SOLDADO, 0);
                }
            }
            case "capital_oeste" -> {
                Pal p = Pal.oeste();
                Lay.capital(b, s, p, p, r, s.radius() - 8, 9, 38);
                Landmarks.fortress(b, cx, cz, g, p, 28, 26, net.minecraft.block.Blocks.RED_CARPET, net.minecraft.block.Blocks.RED_WALL_BANNER,
                        p.roofBlock, "castelo", null);
            }
            case "capital_sul" -> {
                Pal p = Pal.sul();
                Lay.capital(b, s, p, p, r, s.radius() - 8, 9, 38);
                Landmarks.guildhall(b, cx, cz, g, p);
            }
            case "capital_leste" -> {
                Pal p = Pal.leste();
                Lay.capital(b, s, p, p, r, s.radius() - 6, 10, 60);
                Landmarks.palace(b, cx, cz, g, p);
            }
            case "porto_pirata" -> {
                Pal p = Pal.pirata();
                Lay.town(b, s, p, r);
                Landmarks.pirateHarbor(b, s, p, t, r);
            }
            case "vila_neve" -> {
                Lay.town(b, s, Pal.norte(), r);
                switch (s.id()) {
                    case "cambehy" -> npc(b, s, -3, 2, NpcRole.LUCY);
                    case "oerr" -> npc(b, s, -3, 2, NpcRole.RAONI);
                    case "geerd" -> npc(b, s, -3, 2, NpcRole.LIAM);
                    default -> { }
                }
            }
            case "vila_planicie" -> {
                Lay.town(b, s, Pal.planicie(), r);
                if (s.id().equals("dejury")) {
                    npc(b, s, -3, 2, NpcRole.MILLY);
                }
            }
            case "vila_floresta" -> Lay.town(b, s, Pal.floresta(), r);
            case "vila_porto" -> {
                Lay.town(b, s, Pal.porto(), r);
                if (s.id().equals("orse")) {
                    npc(b, s, -3, 2, NpcRole.LUKE);
                    npc(b, s, -4, 3, NpcRole.VANESSA);
                }
            }
            case "vila_deserto" -> Lay.town(b, s, Pal.deserto(), r);
            case "mosteiro" -> Landmarks.monastery(b, cx, cz, g, Pal.mosteiro());
            case "torre" -> Landmarks.merlinTower(b, cx, cz, g);
            case "templo_sol" -> Landmarks.sunTemple(b, cx, cz, g);
            case "cidade_torres" -> {
                Pal p = Pal.norte();
                Lay.clearDisc(b, cx, cz, s.radius() + 4, g, 60);
                Landmarks.spires(b, cx, cz, g, p, r);
            }
            case "ruinas" -> {
                if (s.id().equals("karamarraw_acampamento")) {
                    Landmarks.camp(b, s, r);
                } else {
                    int lic = s.id().equals("karamarraw_ruinas") ? 4 : s.id().equals("runes") ? 3 : 2;
                    Landmarks.ruins(b, s, Pal.ruina(), r, lic);
                }
            }
            case "caverna" -> Landmarks.lair(b, s, !s.id().equals("caverna_orju"));
            default -> { }
        }
        return b;
    }
}
