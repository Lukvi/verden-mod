package com.verden.world;

import com.mojang.serialization.Codec;
import com.verden.VerdenMod;
import com.verden.entity.NpcRole;
import com.verden.entity.SoldadoLegiaoEntity;
import com.verden.entity.VerdenNpcEntity;
import com.verden.registry.ModEntities;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Acampamento da Legião das Espadas: tendas, fogueira, baú com suprimentos e soldados de guarda. */
public class LegionCampFeature extends Feature<DefaultFeatureConfig> {
    public static final RegistryKey<LootTable> LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, VerdenMod.id("chests/acampamento_legiao"));

    public LegionCampFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        BlockPos o = ctx.getOrigin();
        Random random = ctx.getRandom();

        if (!world.getBlockState(o.down()).isSolidBlock(world, o.down()) || !world.getFluidState(o).isEmpty()
                || !world.getFluidState(o.down()).isEmpty()) {
            return false;
        }

        // Terreno: chão de terra batida e espaço livre
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx * dx + dz * dz > 27) {
                    continue;
                }
                BlockPos floor = o.add(dx, -1, dz);
                world.setBlockState(floor, random.nextInt(3) == 0 ? Blocks.COARSE_DIRT.getDefaultState() : Blocks.DIRT_PATH.getDefaultState(), Block.NOTIFY_LISTENERS);
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos under = floor.down(dy);
                    if (!world.getBlockState(under).isSolidBlock(world, under)) {
                        world.setBlockState(under, Blocks.DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
                for (int dy = 0; dy <= 4; dy++) {
                    world.setBlockState(o.add(dx, dy, dz), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }

        // Fogueira central
        world.setBlockState(o, Blocks.CAMPFIRE.getDefaultState(), Block.NOTIFY_LISTENERS);

        // Tenda principal (A-frame de lã vermelha e branca), aberta para a fogueira
        BlockPos t = o.add(-4, 0, -2);
        for (int z = 0; z < 4; z++) {
            Block side = (z % 2 == 0) ? Blocks.RED_WOOL : Blocks.WHITE_WOOL;
            world.setBlockState(t.add(0, 0, z), side.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.setBlockState(t.add(0, 1, z), side.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.setBlockState(t.add(1, 2, z), Blocks.RED_WOOL.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.setBlockState(t.add(2, 1, z), side.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.setBlockState(t.add(2, 0, z), side.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        world.setBlockState(t.add(1, 0, 3), Blocks.RED_WOOL.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(t.add(1, 1, 3), Blocks.RED_WOOL.getDefaultState(), Block.NOTIFY_LISTENERS);

        // Baú de suprimentos dentro da tenda
        BlockPos chest = t.add(1, 0, 2);
        world.setBlockState(chest, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
        LootableContainerBlockEntity.setLootTable(world, random, chest, LOOT);
        world.setBlockState(t.add(1, 0, 0), Blocks.RED_CARPET.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(t.add(1, 0, 1), Blocks.RED_CARPET.getDefaultState(), Block.NOTIFY_LISTENERS);

        // Estandarte e tochas
        BlockPos pole = o.add(4, 0, 3);
        world.setBlockState(pole, Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pole.up(), Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pole.up(2), Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pole.up(3), Blocks.RED_BANNER.getDefaultState().with(BannerBlock.ROTATION, 0), Block.NOTIFY_LISTENERS);
        for (int[] c : new int[][]{{3, -3}, {-3, 3}, {3, 3}}) {
            BlockPos p = o.add(c[0], 0, c[1]);
            world.setBlockState(p, Blocks.OAK_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.setBlockState(p.up(), Blocks.TORCH.getDefaultState(), Block.NOTIFY_LISTENERS);
        }

        // Guarnição
        for (int i = 0; i < 2; i++) {
            SoldadoLegiaoEntity s = ModEntities.SOLDADO_LEGIAO.create(world.toServerWorld());
            if (s != null) {
                s.refreshPositionAndAngles(o.getX() + 0.5 + (i == 0 ? 2 : -2), o.getY(), o.getZ() + 0.5 + 2, random.nextFloat() * 360f, 0f);
                s.initialize(world, world.getLocalDifficulty(o), SpawnReason.STRUCTURE, null);
                s.setPersistent();
                world.spawnEntity(s);
            }
        }
        if (random.nextInt(3) == 0) {
            VerdenNpcEntity npc = ModEntities.NPC.create(world.toServerWorld());
            if (npc != null) {
                npc.setVariant(NpcRole.MENETRYX.ordinal());
                npc.refreshPositionAndAngles(o.getX() + 0.5, o.getY(), o.getZ() + 3.5, random.nextFloat() * 360f, 0f);
                npc.initialize(world, world.getLocalDifficulty(o), SpawnReason.STRUCTURE, null);
                world.spawnEntity(npc);
            }
        }
        return true;
    }
}
