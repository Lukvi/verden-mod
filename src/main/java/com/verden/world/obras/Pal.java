package com.verden.world.obras;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/** Paleta de materiais de um estilo de arquitetura. */
public final class Pal {
    public Block wall, wallAlt, corner, floor, foundation, roofStairs, roofBlock, roofSlab, glass, door, path, path2, trim, banner, pillar, light;
    public boolean flatRoof;

    private Pal() {}

    public BlockState wallS() {
        return wall.getDefaultState();
    }

    /** Casas de pedra clara de Koold: Veroogh, Geerd, Cambehy... */
    public static Pal norte() {
        Pal p = new Pal();
        p.wall = Blocks.STONE_BRICKS; p.wallAlt = Blocks.SPRUCE_PLANKS; p.corner = Blocks.SPRUCE_LOG;
        p.floor = Blocks.SPRUCE_PLANKS; p.foundation = Blocks.COBBLESTONE;
        p.roofStairs = Blocks.SPRUCE_STAIRS; p.roofBlock = Blocks.SPRUCE_PLANKS; p.roofSlab = Blocks.SPRUCE_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.SPRUCE_DOOR; p.path = Blocks.COBBLESTONE; p.path2 = Blocks.STONE;
        p.trim = Blocks.SMOOTH_STONE; p.banner = Blocks.PURPLE_BANNER; p.pillar = Blocks.QUARTZ_PILLAR; p.light = Blocks.LANTERN;
        return p;
    }

    /** Pedra branca do Partenon. */
    public static Pal nobreNorte() {
        Pal p = norte();
        p.wall = Blocks.QUARTZ_BRICKS; p.wallAlt = Blocks.SMOOTH_QUARTZ; p.corner = Blocks.QUARTZ_PILLAR;
        p.floor = Blocks.SMOOTH_QUARTZ; p.foundation = Blocks.STONE_BRICKS;
        p.roofStairs = Blocks.QUARTZ_STAIRS; p.roofBlock = Blocks.QUARTZ_BLOCK; p.roofSlab = Blocks.QUARTZ_SLAB;
        p.trim = Blocks.CHISELED_QUARTZ_BLOCK; p.path = Blocks.QUARTZ_BRICKS;
        return p;
    }

    /** Nagdaar: pedra negra e vermelho. */
    public static Pal oeste() {
        Pal p = new Pal();
        p.wall = Blocks.POLISHED_BLACKSTONE_BRICKS; p.wallAlt = Blocks.RED_NETHER_BRICKS; p.corner = Blocks.BLACKSTONE;
        p.floor = Blocks.DARK_OAK_PLANKS; p.foundation = Blocks.BLACKSTONE;
        p.roofStairs = Blocks.DEEPSLATE_TILE_STAIRS; p.roofBlock = Blocks.DEEPSLATE_TILES; p.roofSlab = Blocks.DEEPSLATE_TILE_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.DARK_OAK_DOOR; p.path = Blocks.POLISHED_BLACKSTONE_BRICKS; p.path2 = Blocks.BLACKSTONE;
        p.trim = Blocks.CHISELED_POLISHED_BLACKSTONE; p.banner = Blocks.RED_BANNER; p.pillar = Blocks.POLISHED_BASALT; p.light = Blocks.SOUL_LANTERN;
        return p;
    }

    /** Jørd: pedra e madeira, bandeiras roxas. */
    public static Pal sul() {
        Pal p = new Pal();
        p.wall = Blocks.WHITE_TERRACOTTA; p.wallAlt = Blocks.OAK_PLANKS; p.corner = Blocks.DARK_OAK_LOG;
        p.floor = Blocks.OAK_PLANKS; p.foundation = Blocks.STONE_BRICKS;
        p.roofStairs = Blocks.BRICK_STAIRS; p.roofBlock = Blocks.BRICKS; p.roofSlab = Blocks.BRICK_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.OAK_DOOR; p.path = Blocks.COBBLESTONE; p.path2 = Blocks.GRAVEL;
        p.trim = Blocks.STONE_BRICKS; p.banner = Blocks.PURPLE_BANNER; p.pillar = Blocks.STONE_BRICKS; p.light = Blocks.LANTERN;
        return p;
    }

    /** Flerin / Yettus: arenito e terracota. */
    public static Pal leste() {
        Pal p = new Pal();
        p.wall = Blocks.SMOOTH_SANDSTONE; p.wallAlt = Blocks.CUT_SANDSTONE; p.corner = Blocks.CHISELED_SANDSTONE;
        p.floor = Blocks.SMOOTH_SANDSTONE; p.foundation = Blocks.SANDSTONE;
        p.roofStairs = Blocks.SANDSTONE_STAIRS; p.roofBlock = Blocks.CUT_SANDSTONE; p.roofSlab = Blocks.SANDSTONE_SLAB;
        p.glass = Blocks.ORANGE_STAINED_GLASS_PANE; p.door = Blocks.JUNGLE_DOOR; p.path = Blocks.SMOOTH_SANDSTONE; p.path2 = Blocks.SAND;
        p.trim = Blocks.ORANGE_TERRACOTTA; p.banner = Blocks.GREEN_BANNER; p.pillar = Blocks.CUT_SANDSTONE; p.light = Blocks.LANTERN;
        p.flatRoof = true;
        return p;
    }

    /** Maariz: madeira escura de navio. */
    public static Pal pirata() {
        Pal p = new Pal();
        p.wall = Blocks.DARK_OAK_PLANKS; p.wallAlt = Blocks.SPRUCE_PLANKS; p.corner = Blocks.DARK_OAK_LOG;
        p.floor = Blocks.SPRUCE_PLANKS; p.foundation = Blocks.STONE_BRICKS;
        p.roofStairs = Blocks.DARK_OAK_STAIRS; p.roofBlock = Blocks.DARK_OAK_PLANKS; p.roofSlab = Blocks.DARK_OAK_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.DARK_OAK_DOOR; p.path = Blocks.SPRUCE_PLANKS; p.path2 = Blocks.COARSE_DIRT;
        p.trim = Blocks.SPRUCE_LOG; p.banner = Blocks.BLACK_BANNER; p.pillar = Blocks.STRIPPED_DARK_OAK_LOG; p.light = Blocks.LANTERN;
        return p;
    }

    /** Vilas de floresta e planície. */
    public static Pal planicie() {
        Pal p = new Pal();
        p.wall = Blocks.OAK_PLANKS; p.wallAlt = Blocks.WHITE_TERRACOTTA; p.corner = Blocks.OAK_LOG;
        p.floor = Blocks.OAK_PLANKS; p.foundation = Blocks.COBBLESTONE;
        p.roofStairs = Blocks.COBBLESTONE_STAIRS; p.roofBlock = Blocks.COBBLESTONE; p.roofSlab = Blocks.COBBLESTONE_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.OAK_DOOR; p.path = Blocks.DIRT_PATH; p.path2 = Blocks.COARSE_DIRT;
        p.trim = Blocks.STRIPPED_OAK_LOG; p.banner = Blocks.WHITE_BANNER; p.pillar = Blocks.OAK_LOG; p.light = Blocks.LANTERN;
        return p;
    }

    public static Pal floresta() {
        Pal p = planicie();
        p.wall = Blocks.SPRUCE_PLANKS; p.wallAlt = Blocks.STRIPPED_SPRUCE_LOG; p.corner = Blocks.SPRUCE_LOG;
        p.floor = Blocks.SPRUCE_PLANKS; p.roofStairs = Blocks.SPRUCE_STAIRS; p.roofBlock = Blocks.SPRUCE_PLANKS; p.roofSlab = Blocks.SPRUCE_SLAB;
        p.door = Blocks.SPRUCE_DOOR;
        return p;
    }

    public static Pal porto() {
        Pal p = planicie();
        p.wall = Blocks.WHITE_TERRACOTTA; p.wallAlt = Blocks.OAK_PLANKS; p.corner = Blocks.STRIPPED_OAK_LOG;
        p.roofStairs = Blocks.DARK_OAK_STAIRS; p.roofBlock = Blocks.DARK_OAK_PLANKS; p.roofSlab = Blocks.DARK_OAK_SLAB;
        p.path = Blocks.SPRUCE_PLANKS;
        return p;
    }

    public static Pal deserto() {
        Pal p = leste();
        p.banner = Blocks.ORANGE_BANNER;
        return p;
    }

    /** Mosteiro de Montainezz. */
    public static Pal mosteiro() {
        Pal p = new Pal();
        p.wall = Blocks.WHITE_TERRACOTTA; p.wallAlt = Blocks.STONE_BRICKS; p.corner = Blocks.STRIPPED_CHERRY_LOG;
        p.floor = Blocks.CHERRY_PLANKS; p.foundation = Blocks.STONE_BRICKS;
        p.roofStairs = Blocks.CHERRY_STAIRS; p.roofBlock = Blocks.CHERRY_PLANKS; p.roofSlab = Blocks.CHERRY_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.CHERRY_DOOR; p.path = Blocks.STONE_BRICKS; p.path2 = Blocks.COBBLESTONE;
        p.trim = Blocks.STRIPPED_CHERRY_LOG; p.banner = Blocks.PINK_BANNER; p.pillar = Blocks.STRIPPED_CHERRY_LOG; p.light = Blocks.LANTERN;
        return p;
    }

    public static Pal ruina() {
        Pal p = new Pal();
        p.wall = Blocks.MOSSY_STONE_BRICKS; p.wallAlt = Blocks.CRACKED_STONE_BRICKS; p.corner = Blocks.STONE_BRICKS;
        p.floor = Blocks.MOSSY_COBBLESTONE; p.foundation = Blocks.MOSSY_COBBLESTONE;
        p.roofStairs = Blocks.STONE_BRICK_STAIRS; p.roofBlock = Blocks.STONE_BRICKS; p.roofSlab = Blocks.STONE_BRICK_SLAB;
        p.glass = Blocks.GLASS_PANE; p.door = Blocks.OAK_DOOR; p.path = Blocks.MOSSY_COBBLESTONE; p.path2 = Blocks.COBBLESTONE;
        p.trim = Blocks.STONE_BRICKS; p.banner = Blocks.GRAY_BANNER; p.pillar = Blocks.STONE_BRICKS; p.light = Blocks.SOUL_LANTERN;
        return p;
    }
}
