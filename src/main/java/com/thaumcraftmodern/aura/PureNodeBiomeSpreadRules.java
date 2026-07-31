package com.thaumcraftmodern.aura;

/**
 * Exact timing and triangular target window used by TC4 pure nodes.
 */
public final class PureNodeBiomeSpreadRules {
    public static final int INTERVAL_TICKS = 50;
    public static final int BIOME_OFFSET_BOUND = 8;

    private PureNodeBiomeSpreadRules() {
    }

    public static int biomeOffset(int firstRoll, int secondRoll) {
        if (firstRoll < 0 || firstRoll >= BIOME_OFFSET_BOUND
                || secondRoll < 0 || secondRoll >= BIOME_OFFSET_BOUND) {
            throw new IllegalArgumentException(
                    "pure-node biome rolls must be in [0, 8)"
            );
        }
        return firstRoll - secondRoll;
    }

    public static boolean mayPaint(
            int ticks,
            boolean embeddedInSilverwood,
            boolean targetIsTainted
    ) {
        return ticks % INTERVAL_TICKS == 0
                && (embeddedInSilverwood || targetIsTainted);
    }
}
