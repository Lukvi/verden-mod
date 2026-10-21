package com.verden.world.obras;

import com.verden.world.gen.Region;
import com.verden.world.gen.RoadNet;
import com.verden.world.gen.Sites;
import com.verden.world.gen.Terrain;
import com.verden.world.gen.VerdenChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

/** Desenha as estradas (piso, pontes sobre rios) e os marcos com placas nas estradas de Verden. */
public final class RoadBuilder {
    private RoadBuilder() {}

    private static final double HALF = 1.6;
    private static final int MILE = 500;

    private static int hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    public static void placeInChunk(StructureWorldAccess world, Chunk chunk, VerdenChunkGenerator gen) {
        int x0 = chunk.getPos().getStartX(), z0 = chunk.getPos().getStartZ();
        if (!RoadNet.near(x0 - 4, z0 - 4, x0 + 19, z0 + 19)) {
            return;
        }
        Terrain t = gen.terrain();
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = x0; x < x0 + 16; x++) {
            for (int z = z0; z < z0 + 16; z++) {
                double d = RoadNet.distance(x + 0.5, z + 0.5);
                if (d > HALF + 0.9) {
                    continue;
                }
                Terrain.Column c = t.column(x, z);
                if (c.coast < 0 && c.water > c.solid && !c.river) {
                    continue; // mar aberto
                }
                boolean core = d <= HALF;
                if (!core && (hash(x, z) & 3) != 0) {
                    continue;
                }
                if (c.water > c.solid) {
                    bridge(world, m, x, z, c.water, core);
                    continue;
                }
                int y = c.solid;
                m.set(x, y, z);
                BlockState cur = world.getBlockState(m);
                if (cur.isAir() || !cur.getFluidState().isEmpty() || cur.isIn(BlockTags.LEAVES) || cur.isIn(BlockTags.LOGS)) {
                    continue;
                }
                for (int dy = 1; dy <= 5; dy++) {
                    m.set(x, y + dy, z);
                    BlockState s = world.getBlockState(m);
                    if (!s.isAir() && (s.isIn(BlockTags.LEAVES) || s.isIn(BlockTags.LOGS) || s.isReplaceable())) {
                        world.setBlockState(m, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
                m.set(x, y, z);
                world.setBlockState(m, roadBlock(c.surface, core, x, z), Block.NOTIFY_LISTENERS);
            }
        }
        markers(world, chunk, t, x0, z0);
    }

    private static BlockState roadBlock(Region r, boolean core, int x, int z) {
        int h = hash(x, z) & 7;
        if (r == Region.DESERT || r == Region.BADLANDS) {
            return (core && h > 1 ? Blocks.SMOOTH_SANDSTONE : Blocks.SANDSTONE).getDefaultState();
        }
        if (!core) {
            return (h < 4 ? Blocks.COARSE_DIRT : Blocks.GRAVEL).getDefaultState();
        }
        return (h == 0 ? Blocks.GRAVEL : h == 1 ? Blocks.COARSE_DIRT : Blocks.DIRT_PATH).getDefaultState();
    }

    private static void bridge(StructureWorldAccess world, BlockPos.Mutable m, int x, int z, int water, boolean core) {
        if (core) {
            for (int dy = 1; dy <= 4; dy++) {
                m.set(x, water + dy, z);
                if (world.getBlockState(m).isIn(BlockTags.LEAVES) || world.getBlockState(m).isIn(BlockTags.LOGS)) {
                    world.setBlockState(m, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
            m.set(x, water, z);
            world.setBlockState(m, Blocks.SPRUCE_PLANKS.getDefaultState(), Block.NOTIFY_LISTENERS);
            if ((x + z) % 5 == 0) {
                for (int dy = 1; dy <= 6; dy++) {
                    m.set(x, water - dy, z);
                    if (!world.getFluidState(m).isEmpty()) {
                        world.setBlockState(m, Blocks.SPRUCE_LOG.getDefaultState(), Block.NOTIFY_LISTENERS);
                    } else {
                        break;
                    }
                }
            }
        } else {
            m.set(x, water + 1, z);
            if (world.getBlockState(m).isAir()) {
                world.setBlockState(m, Blocks.SPRUCE_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }
    }

    // ------------------------------------------------------------------ marcos
    private static String nameOf(String id) {
        Sites.Site s = Sites.byId(id);
        String n = s == null ? id : s.name();
        return n.length() > 15 ? n.substring(0, 15) : n;
    }

    private record Marker(int px, int pz, int rot, String to, int toB, String from, int toA) {}

    private static java.util.List<Marker> markerList;

    private static synchronized java.util.List<Marker> markerList() {
        if (markerList == null) {
            java.util.List<Marker> out = new java.util.ArrayList<>();
            for (RoadNet.Road r : RoadNet.all()) {
                for (double pos = 18; pos < r.length - 10; pos += MILE) {
                    double[] p = r.at(pos);
                    // posto do lado direito da estrada, a 3 blocos do eixo
                    int px = (int) Math.round(p[0] - p[3] * 3), pz = (int) Math.round(p[1] + p[2] * 3);
                    // a placa fica de frente para quem anda na estrada: perpendicular ao eixo
                    int rot = Math.abs(p[2]) > Math.abs(p[3]) ? (p[2] > 0 ? 8 : 0) : (p[3] > 0 ? 12 : 4);
                    int toB = (int) Math.round((r.length - pos) / 10.0) * 10, toA = (int) Math.round(pos / 10.0) * 10;
                    out.add(new Marker(px, pz, rot, nameOf(r.b), toB, nameOf(r.a), toA));
                }
            }
            markerList = out;
        }
        return markerList;
    }

    private static void markers(StructureWorldAccess world, Chunk chunk, Terrain t, int x0, int z0) {
        for (Marker mk : markerList()) {
            int px = mk.px(), pz = mk.pz();
            if (px < x0 || px >= x0 + 16 || pz < z0 || pz >= z0 + 16) {
                continue;
            }
            Terrain.Column c = t.column(px, pz);
            if (c.water > c.solid) {
                continue;
            }
            int y = c.solid;
            BlockPos.Mutable m = new BlockPos.Mutable(px, y, pz);
            if (world.getBlockState(m).isAir()) {
                continue;
            }
            for (int dy = 1; dy <= 5; dy++) {
                m.set(px, y + dy, pz);
                world.setBlockState(m, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            m.set(px, y + 1, pz);
            world.setBlockState(m, Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
            m.set(px, y + 2, pz);
            world.setBlockState(m, Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
            m.set(px, y + 3, pz);
            world.setBlockState(m, Blocks.OAK_SIGN.getDefaultState().with(SignBlock.ROTATION, mk.rot()), Block.NOTIFY_LISTENERS);
            if (world.getBlockEntity(m) instanceof SignBlockEntity sign) {
                SignText text = sign.getFrontText()
                        .withMessage(0, Text.literal("-> " + mk.to()))
                        .withMessage(1, Text.literal(mk.toB() + " m"))
                        .withMessage(2, Text.literal("<- " + mk.from()))
                        .withMessage(3, Text.literal(mk.toA() + " m"));
                sign.setText(text, true);
            }
            m.set(px, y + 4, pz);
            world.setBlockState(m, Blocks.LANTERN.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }
}
