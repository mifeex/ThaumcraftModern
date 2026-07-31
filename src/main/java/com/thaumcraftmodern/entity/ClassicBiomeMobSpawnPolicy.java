package com.thaumcraftmodern.entity;

/**
 * Local population limits used by the TC4 biome-spawned creatures.
 */
final class ClassicBiomeMobSpawnPolicy {
    static final int PECH_RANGE = 16;
    static final int WISP_RANGE = 16;
    static final int TAINTACLE_HORIZONTAL_RANGE = 24;
    static final int TAINTACLE_VERTICAL_RANGE = 8;

    private ClassicBiomeMobSpawnPolicy() {
    }

    static boolean allowsPopulation(LegacyMobKind kind, int nearbySameKind) {
        return switch (kind) {
            case PECH -> nearbySameKind < 4;
            case WISP -> nearbySameKind < 8;
            case TAINTACLE -> nearbySameKind < 1;
            default -> true;
        };
    }
}
