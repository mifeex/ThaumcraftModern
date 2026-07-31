package com.thaumcraftmodern.worldgen;

/**
 * TC4 4.2.3.5 Tainted Land biome settings, kept outside registry bootstrap so
 * the extracted legacy values and the modern overlay adaptation stay directly
 * regression-testable.
 */
final class TaintedLandsGenerationPolicy {
    static final int BIOME_WEIGHT = 2;
    static final int PATCH_NOISE_SCALE_QUARTS = 160;
    static final double WEIGHT_THRESHOLD_STEP = 0.045D;
    static final double MAXIMUM_THRESHOLD_OFFSET = 0.30D;

    static final int FLOWER_ATTEMPTS = 2;
    static final int GRASS_ATTEMPTS = 2;
    static final int TAINT_BLOB_VARIANTS = 3;
    static final int SURFACE_FIBRE_ATTEMPTS = 10;
    static final int SPREAD_FIBRE_ATTEMPTS = 8;
    static final int INFECTED_TREE_ATTEMPTS = 3;
    static final int INFECTED_TREE_STAGES = 3;
    static final int TAINTED_SOIL_PATCH_ATTEMPTS = 18;
    static final int TAINTED_PLANT_ATTEMPTS = 14;
    static final int SPORE_STALK_ATTEMPTS = 3;
    static final int TAINTED_NODE_RARITY = 12;

    static final int TAINTACLE_WEIGHT = 1;
    static final int TAINTACLE_MINIMUM = 1;
    static final int TAINTACLE_MAXIMUM = 1;

    static final int GRASS_COLOR = 0x6D4189;
    static final int FOLIAGE_COLOR = 0x4F8A55;
    static final int SKY_COLOR = 0x7C44FF;
    static final int WATER_COLOR = 0xCC1188;

    private TaintedLandsGenerationPolicy() {
    }

    /**
     * TC4 delegated biome shape to the vanilla biome layers, where weight
     * controlled occurrence without shrinking every selected biome. The
     * overlay therefore uses an independent, coarser field and a default
     * threshold equal in magnitude to Magical Forest's default threshold.
     */
    static double patchThreshold(int configuredWeight) {
        int weight = Math.max(0, configuredWeight);
        return -1.0D + Math.min(
                MAXIMUM_THRESHOLD_OFFSET,
                weight * WEIGHT_THRESHOLD_STEP
        );
    }
}
