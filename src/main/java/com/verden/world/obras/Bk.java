package com.verden.world.obras;

import net.minecraft.block.BannerBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.Direction;

/** Atalhos para criar estados de bloco com orientação. */
public final class Bk {
    private Bk() {}

    public static final BlockState AIR = Blocks.AIR.getDefaultState();

    public static BlockState d(Block b) {
        return b.getDefaultState();
    }

    public static BlockState stairs(Block b, Direction facing, boolean top) {
        return b.getDefaultState().with(StairsBlock.FACING, facing).with(StairsBlock.HALF, top ? BlockHalf.TOP : BlockHalf.BOTTOM);
    }

    public static BlockState slab(Block b, boolean top) {
        return b.getDefaultState().with(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    public static BlockState pillar(Block b, Direction.Axis axis) {
        return b.getDefaultState().with(PillarBlock.AXIS, axis);
    }

    public static BlockState door(Block b, Direction facing, boolean upper) {
        return b.getDefaultState().with(DoorBlock.FACING, facing)
                .with(DoorBlock.HALF, upper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
    }

    /** Vidraça ligada ao longo do eixo x (leste-oeste) ou z (norte-sul). */
    public static BlockState pane(Block b, boolean alongX) {
        BlockState s = b.getDefaultState();
        return alongX ? s.with(PaneBlock.EAST, true).with(PaneBlock.WEST, true)
                : s.with(PaneBlock.NORTH, true).with(PaneBlock.SOUTH, true);
    }

    public static BlockState wallTorch(Direction facing) {
        return Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, facing);
    }

    public static BlockState lantern(boolean hanging) {
        return Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, hanging);
    }

    public static BlockState banner(Block b, int rotation) {
        return b.getDefaultState().with(BannerBlock.ROTATION, rotation & 15);
    }

    public static BlockState wallBanner(Block b, Direction facing) {
        return b.getDefaultState().with(WallBannerBlock.FACING, facing);
    }

    public static BlockState ladder(Direction facing) {
        return Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, facing);
    }

    public static BlockState bedPart(Block b, Direction facing, boolean head) {
        return b.getDefaultState().with(BedBlock.FACING, facing).with(BedBlock.PART, head ? BedPart.HEAD : BedPart.FOOT);
    }
}
