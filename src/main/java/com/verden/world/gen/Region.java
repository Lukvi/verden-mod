package com.verden.world.gen;

/** Regiões do mapa. A ordem DEVE ser igual à lista BIOMES de tools/map_gen.py e do world_preset. */
public enum Region {
    OCEAN, DEEP_OCEAN, BEACH, SNOWY_PLAINS, FROZEN_PEAKS, TAIGA, DARK_FOREST, FOREST, PLAINS, SWAMP, DESERT, BADLANDS,
    JUNGLE, SAVANNA, SNOWY_TAIGA, SUNFLOWER_PLAINS, RIVER, FROZEN_RIVER, STONY_SHORE, SNOWY_BEACH, CHERRY_GROVE,
    STONY_PEAKS, COLD_OCEAN, DEEP_COLD_OCEAN, WARM_OCEAN;

    private static final Region[] ALL = values();

    public static Region of(int id) {
        return id >= 0 && id < ALL.length ? ALL[id] : OCEAN;
    }

    public boolean isOcean() {
        return this == OCEAN || this == DEEP_OCEAN || this == COLD_OCEAN || this == DEEP_COLD_OCEAN || this == WARM_OCEAN;
    }

    public boolean isCold() {
        return this == SNOWY_PLAINS || this == FROZEN_PEAKS || this == SNOWY_TAIGA || this == SNOWY_BEACH
                || this == FROZEN_RIVER || this == COLD_OCEAN || this == DEEP_COLD_OCEAN || this == CHERRY_GROVE;
    }

    /** Amplitude do relevo extra (blocos) sobre a altura base do mapa. */
    public double roughness() {
        return switch (this) {
            case FROZEN_PEAKS, STONY_PEAKS -> 14;
            case JUNGLE, TAIGA, SNOWY_TAIGA, DARK_FOREST, CHERRY_GROVE -> 6;
            case DESERT, BADLANDS -> 5;
            case FOREST, SNOWY_PLAINS, SAVANNA -> 4;
            case SWAMP -> 0.8;
            case PLAINS, SUNFLOWER_PLAINS -> 3;
            case STONY_SHORE -> 5;
            default -> 1.5;
        };
    }
}
