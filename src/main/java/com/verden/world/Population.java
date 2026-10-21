package com.verden.world;

import com.verden.entity.VerdenWalkerEntity;
import com.verden.entity.VerdenWalkerEntity.Kind;
import com.verden.registry.ModEntities;
import com.verden.world.gen.RoadNet;
import com.verden.world.gen.Sites;
import com.verden.world.gen.Sites.Site;
import com.verden.world.gen.VerdenChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.List;
import java.util.Random;

/** Mantém o mundo vivo: moradores andando pelas cidades e caravanas nas estradas, perto dos jogadores. */
public final class Population {
    private Population() {}

    private static final Random RNG = new Random();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(Population::tick);
    }

    private static int target(Site s) {
        String k = s.kind();
        if (k.startsWith("capital")) {
            return 16;
        }
        return switch (k) {
            case "porto_pirata" -> 10;
            case "vila_neve", "vila_planicie", "vila_floresta", "vila_porto", "vila_deserto" -> 6;
            case "cidade_torres" -> 5;
            case "mosteiro" -> 5;
            case "templo_sol" -> 3;
            default -> 0;
        };
    }

    private static Kind pick(Site s) {
        String k = s.kind();
        Kind[] pool;
        if (k.startsWith("capital")) {
            pool = new Kind[]{Kind.CAMPONES, Kind.CAMPONES, Kind.CAMPONESA, Kind.CAMPONESA, Kind.VIAJANTE, Kind.GUARDA, Kind.GUARDA, Kind.GUARDA,
                    Kind.MAGO, Kind.CACADOR, Kind.MERCENARIO, Kind.MERCADOR, Kind.MERCADOR, Kind.SACERDOTE};
        } else if (k.equals("porto_pirata")) {
            pool = new Kind[]{Kind.PIRATA, Kind.PIRATA, Kind.PIRATA, Kind.PIRATA, Kind.CAMPONESA, Kind.MERCADOR, Kind.MERCENARIO, Kind.MERCENARIO};
        } else if (k.equals("mosteiro") || k.equals("templo_sol")) {
            pool = new Kind[]{Kind.SACERDOTE, Kind.SACERDOTE, Kind.SACERDOTE, Kind.VIAJANTE};
        } else if (k.equals("cidade_torres")) {
            pool = new Kind[]{Kind.MAGO, Kind.MAGO, Kind.MAGO, Kind.GUARDA, Kind.GUARDA, Kind.VIAJANTE};
        } else {
            pool = new Kind[]{Kind.CAMPONES, Kind.CAMPONES, Kind.CAMPONESA, Kind.CAMPONESA, Kind.VIAJANTE, Kind.GUARDA, Kind.CACADOR, Kind.MERCADOR};
        }
        return pool[RNG.nextInt(pool.length)];
    }

    private static VerdenWalkerEntity spawn(ServerWorld w, Kind k, double x, double y, double z) {
        VerdenWalkerEntity e = ModEntities.WALKER.create(w);
        if (e == null) {
            return null;
        }
        e.refreshPositionAndAngles(x, y, z, RNG.nextInt(360), 0f);
        e.initialize(w, w.getLocalDifficulty(new BlockPos((int) x, (int) y, (int) z)), SpawnReason.EVENT, null);
        e.setKind(k);
        w.spawnEntity(e);
        return e;
    }

    private static void tick(ServerWorld w) {
        if (w.getTime() % 100 != 0 || !(w.getChunkManager().getChunkGenerator() instanceof VerdenChunkGenerator)) {
            return;
        }
        for (ServerPlayerEntity pl : w.getPlayers()) {
            townLife(w, pl);
            caravans(w, pl);
        }
    }

    private static void townLife(ServerWorld w, ServerPlayerEntity pl) {
        for (Site s : Sites.all()) {
            int tgt = target(s);
            if (tgt == 0) {
                continue;
            }
            double d = Math.hypot(pl.getX() - s.x(), pl.getZ() - s.z());
            if (d > s.radius() + 56) {
                continue;
            }
            Box box = new Box(s.x() - s.radius(), s.level() - 4, s.z() - s.radius(), s.x() + s.radius(), s.level() + 40, s.z() + s.radius());
            int have = w.getEntitiesByClass(VerdenWalkerEntity.class, box, e -> !e.isCaravan()).size();
            if (have >= tgt || RNG.nextInt(2) != 0) {
                continue;
            }
            for (int tries = 0; tries < 8; tries++) {
                double a = RNG.nextDouble() * Math.PI * 2, r = 6 + RNG.nextDouble() * (s.radius() * 0.8 - 6);
                int x = s.x() + (int) Math.round(Math.cos(a) * r), z = s.z() + (int) Math.round(Math.sin(a) * r);
                BlockPos p = new BlockPos(x, s.level() + 1, z);
                if (!w.getChunkManager().isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                if (w.getBlockState(p).isAir() && w.getBlockState(p.up()).isAir() && w.getBlockState(p.down()).isSolid()
                        && pl.squaredDistanceTo(x, p.getY(), z) > 12 * 12) {
                    VerdenWalkerEntity e = spawn(w, pick(s), x + 0.5, p.getY(), z + 0.5);
                    if (e != null) {
                        e.setPositionTarget(new BlockPos(s.x(), s.level(), s.z()), Math.max(24, s.radius() - 4));
                    }
                    break;
                }
            }
        }
    }

    private static void caravans(ServerWorld w, ServerPlayerEntity pl) {
        List<RoadNet.Road> roads = RoadNet.all();
        if (roads.isEmpty() || RNG.nextInt(3) != 0) {
            return;
        }
        Box near = pl.getBoundingBox().expand(110);
        int have = w.getEntitiesByClass(VerdenWalkerEntity.class, near, VerdenWalkerEntity::isCaravan).size();
        if (have >= 8) {
            return;
        }
        for (int tries = 0; tries < 14; tries++) {
            int ri = RNG.nextInt(roads.size());
            RoadNet.Road r = roads.get(ri);
            if (r.length < 120) {
                continue;
            }
            double t = 40 + RNG.nextDouble() * (r.length - 80);
            double[] p = r.at(t);
            double d = Math.hypot(p[0] - pl.getX(), p[1] - pl.getZ());
            if (d < 50 || d > 90) {
                continue;
            }
            int x = (int) Math.round(p[0]), z = (int) Math.round(p[1]);
            if (!w.getChunkManager().isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            int dir = RNG.nextBoolean() ? 1 : -1;
            Kind[] party = {Kind.MERCADOR, Kind.GUARDA, Kind.GUARDA, RNG.nextBoolean() ? Kind.MAGO : Kind.CACADOR};
            int n = 3 + RNG.nextInt(2);
            for (int i = 0; i < n; i++) {
                double tt = Math.max(0, Math.min(r.length, t - dir * i * 2.5));
                double[] q = r.at(tt);
                int qx = (int) Math.round(q[0]), qz = (int) Math.round(q[1]);
                if (!w.getChunkManager().isChunkLoaded(qx >> 4, qz >> 4)) {
                    continue;
                }
                int qy = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, qx, qz);
                VerdenWalkerEntity e = spawn(w, party[i], qx + 0.5, qy, qz + 0.5);
                if (e != null) {
                    e.followRoad(ri, tt, dir);
                }
            }
            return;
        }
    }
}
