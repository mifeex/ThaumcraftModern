package com.thaumcraftmodern.crucible;

/**
 * Water-container amounts for the Crucible interaction layer.
 */
public final class CrucibleWaterFillRules {
    public static final int CAPACITY_MB = 1000;
    public static final int CAULDRON_LEVELS = 3;
    public static final int BOTTLE_FILL_MB =
            (CAPACITY_MB + CAULDRON_LEVELS - 1) / CAULDRON_LEVELS;

    private CrucibleWaterFillRules() {
    }

    public static int fillFromBucket(int currentWater) {
        return currentWater < CAPACITY_MB ? CAPACITY_MB : currentWater;
    }

    public static int fillFromBottle(int currentWater) {
        if (currentWater >= CAPACITY_MB) {
            return currentWater;
        }
        return Math.min(CAPACITY_MB, currentWater + BOTTLE_FILL_MB);
    }
}
