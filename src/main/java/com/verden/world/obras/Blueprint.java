package com.verden.world.obras;

import com.verden.entity.VerdenNpcEntity;
import com.verden.registry.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de operações de construção (preenchimentos, blocos, baús, criaturas) em coordenadas absolutas.
 * Cada chunk aplica só a parte que cai dentro dele, então a mesma planta serve a todos os chunks do lugar.
 */
public final class Blueprint {
    public enum Mob { NPC, SOLDADO, LICANTROPO, ADAPTADO, KUNORI, ZYTRA, HAIGOL, WALKER }

    private abstract static class Op {
        final int x0, x1, z0, z1;

        Op(int x0, int z0, int x1, int z1) {
            this.x0 = Math.min(x0, x1);
            this.x1 = Math.max(x0, x1);
            this.z0 = Math.min(z0, z1);
            this.z1 = Math.max(z0, z1);
        }

        abstract void apply(StructureWorldAccess w, int cx, int cz, List<BlockPos> post);
    }

    private static boolean needsPost(BlockState s) {
        Block b = s.getBlock();
        return b instanceof PaneBlock || b instanceof FenceBlock || b instanceof WallBlock || b instanceof StairsBlock;
    }

    private static final class Fill extends Op {
        final int y0, y1;
        final BlockState state;

        Fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
            super(x0, z0, x1, z1);
            this.y0 = Math.min(y0, y1);
            this.y1 = Math.max(y0, y1);
            this.state = state;
        }

        @Override
        void apply(StructureWorldAccess w, int cx, int cz, List<BlockPos> post) {
            int xa = Math.max(x0, cx), xb = Math.min(x1, cx + 15);
            int za = Math.max(z0, cz), zb = Math.min(z1, cz + 15);
            int ya = Math.max(y0, w.getBottomY()), yb = Math.min(y1, w.getTopY() - 1);
            boolean p = needsPost(state);
            BlockPos.Mutable m = new BlockPos.Mutable();
            for (int x = xa; x <= xb; x++) {
                for (int z = za; z <= zb; z++) {
                    for (int y = ya; y <= yb; y++) {
                        m.set(x, y, z);
                        w.setBlockState(m, state, Block.NOTIFY_LISTENERS);
                        if (p) {
                            post.add(m.toImmutable());
                        }
                    }
                }
            }
        }
    }

    private static final class ChestOp extends Op {
        final int y;
        final Direction facing;
        final String loot;

        ChestOp(int x, int y, int z, Direction facing, String loot) {
            super(x, z, x, z);
            this.y = y;
            this.facing = facing;
            this.loot = loot;
        }

        @Override
        void apply(StructureWorldAccess w, int cx, int cz, List<BlockPos> post) {
            BlockPos pos = new BlockPos(x0, y, z0);
            w.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), Block.NOTIFY_LISTENERS);
            RegistryKey<LootTable> key = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("verden", "chests/" + loot));
            if (w.getBlockEntity(pos) instanceof LootableContainerBlockEntity lootChest) {
                lootChest.setLootTable(key, w.getRandom().nextLong());
            }
        }
    }

    private static final class SpawnOp extends Op {
        final int y;
        final Mob kind;
        final int variant;

        SpawnOp(int x, int y, int z, Mob kind, int variant) {
            super(x, z, x, z);
            this.y = y;
            this.kind = kind;
            this.variant = variant;
        }

        @Override
        void apply(StructureWorldAccess w, int cx, int cz, List<BlockPos> post) {
            ServerWorld sw = w.toServerWorld();
            Entity e = switch (kind) {
                case NPC -> {
                    VerdenNpcEntity n = ModEntities.NPC.create(sw);
                    if (n != null) {
                        n.setVariant(variant);
                    }
                    yield n;
                }
                case SOLDADO -> ModEntities.SOLDADO_LEGIAO.create(sw);
                case LICANTROPO -> ModEntities.LICANTROPO.create(sw);
                case ADAPTADO -> ModEntities.LICANTROPO_ADAPTADO.create(sw);
                case KUNORI -> ModEntities.KUNORI.create(sw);
                case ZYTRA -> ModEntities.ZYTRA.create(sw);
                case HAIGOL -> ModEntities.HAIGOL.create(sw);
                case WALKER -> {
                    com.verden.entity.VerdenWalkerEntity wk = ModEntities.WALKER.create(sw);
                    if (wk != null) {
                        wk.setKind(com.verden.entity.VerdenWalkerEntity.Kind.byIndex(variant));
                    }
                    yield wk;
                }
            };
            if (e == null) {
                return;
            }
            e.refreshPositionAndAngles(x0 + 0.5, y, z0 + 0.5, (x0 * 31 + z0 * 17) % 360, 0f);
            if (e instanceof MobEntity m) {
                m.initialize(w, w.getLocalDifficulty(new BlockPos(x0, y, z0)), SpawnReason.STRUCTURE, null);
                m.setPersistent();
            }
            w.spawnEntity(e);
        }
    }

    private final List<Op> ops = new ArrayList<>();

    // ------------------------------------------------------------------ construção
    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        ops.add(new Fill(x1, y1, z1, x2, y2, z2, s));
    }

    public void set(int x, int y, int z, BlockState s) {
        ops.add(new Fill(x, y, z, x, y, z, s));
    }

    public void clear(int x1, int y1, int z1, int x2, int y2, int z2) {
        ops.add(new Fill(x1, y1, z1, x2, y2, z2, Blocks.AIR.getDefaultState()));
    }

    /** Caixa oca (paredes, piso e teto). */
    public void hollow(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        int xa = Math.min(x1, x2), xb = Math.max(x1, x2), ya = Math.min(y1, y2), yb = Math.max(y1, y2);
        int za = Math.min(z1, z2), zb = Math.max(z1, z2);
        fill(xa, ya, za, xb, ya, zb, s);
        fill(xa, yb, za, xb, yb, zb, s);
        walls(xa, ya, za, xb, yb, zb, s);
    }

    /** Só as quatro paredes. */
    public void walls(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        int xa = Math.min(x1, x2), xb = Math.max(x1, x2), za = Math.min(z1, z2), zb = Math.max(z1, z2);
        fill(xa, y1, za, xb, y2, za, s);
        fill(xa, y1, zb, xb, y2, zb, s);
        fill(xa, y1, za, xa, y2, zb, s);
        fill(xb, y1, za, xb, y2, zb, s);
    }

    /** Disco horizontal. */
    public void disc(int cx, int y, int cz, int r, BlockState s) {
        for (int dz = -r; dz <= r; dz++) {
            int dx = (int) Math.floor(Math.sqrt(r * r + r * 0.6 - dz * dz));
            if (dx >= 0) {
                fill(cx - dx, y, cz + dz, cx + dx, y, cz + dz, s);
            }
        }
    }

    /** Parede cilíndrica (anel de espessura 1) de y1 a y2. */
    public void ring(int cx, int y1, int y2, int cz, int r, BlockState s) {
        int n = Math.max(16, (int) (r * 8));
        java.util.HashSet<Long> seen = new java.util.HashSet<>();
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            int x = cx + (int) Math.round(Math.cos(a) * r), z = cz + (int) Math.round(Math.sin(a) * r);
            if (seen.add(((long) x << 32) ^ (z & 0xffffffffL))) {
                fill(x, y1, z, x, y2, z, s);
            }
        }
    }

    public void chest(int x, int y, int z, Direction facing, String loot) {
        ops.add(new ChestOp(x, y, z, facing, loot));
    }

    public void spawn(int x, int y, int z, Mob kind, int variant) {
        ops.add(new SpawnOp(x, y, z, kind, variant));
    }

    // ------------------------------------------------------------------ aplicação
    /** Aplica as partes que caem no chunk que começa em (cx, cz). */
    public void apply(StructureWorldAccess w, int cx, int cz) {
        List<BlockPos> post = new ArrayList<>();
        int x1 = cx + 15, z1 = cz + 15;
        for (Op op : ops) {
            if (op.x1 < cx || op.x0 > x1 || op.z1 < cz || op.z0 > z1) {
                continue;
            }
            op.apply(w, cx, cz, post);
        }
        for (BlockPos p : post) {
            BlockState s = w.getBlockState(p);
            BlockState n = Block.postProcessState(s, w, p);
            if (n != s) {
                w.setBlockState(p, n, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
    }

    public int size() {
        return ops.size();
    }
}
