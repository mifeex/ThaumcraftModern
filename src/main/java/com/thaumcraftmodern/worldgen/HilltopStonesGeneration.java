package com.thaumcraftmodern.worldgen;

import java.util.function.IntPredicate;

/**
 * Pure TC4 hilltop-stones geometry and terrain constants.
 */
final class HilltopStonesGeneration {
    static final int MINIMUM_Y = 85;
    static final int FLOOR_OFFSET = -1;
    static final int FOUNDATION_RADIUS = 3;
    static final int BACKFILL_DEPTH = 4;
    static final int NODE_HEIGHT = 5;
    static final int MINIMUM_PILLAR_HEIGHT = 2;
    static final int MAXIMUM_PILLAR_HEIGHT = 4;
    static final int REQUIRED_SUPPORT_DEPTH = BACKFILL_DEPTH + 1;
    static final int CLEARANCE_RADIUS = FOUNDATION_RADIUS + 1;
    static final int CLEARANCE_HEIGHT = MAXIMUM_PILLAR_HEIGHT + 1;

    private HilltopStonesGeneration() {
    }

    static boolean isFoundationPosition(int x, int z) {
        return Math.abs(x) <= FOUNDATION_RADIUS
                && Math.abs(z) <= FOUNDATION_RADIUS
                && !(Math.abs(x) == FOUNDATION_RADIUS
                        && Math.abs(z) == FOUNDATION_RADIUS);
    }

    static boolean isPillarPosition(int x, int z) {
        return (Math.abs(x) == FOUNDATION_RADIUS
                        && Math.abs(z % 2) == 1
                        && Math.abs(z) != FOUNDATION_RADIUS)
                || (Math.abs(z) == FOUNDATION_RADIUS
                        && Math.abs(x % 2) == 1
                        && Math.abs(x) != FOUNDATION_RADIUS);
    }

    static PillarPlan planPillar(IntPredicate stopRollAtHeight) {
        for (int height = MINIMUM_PILLAR_HEIGHT;
                height <= MAXIMUM_PILLAR_HEIGHT;
                height++) {
            if (stopRollAtHeight.test(height)) {
                return new PillarPlan(height, true);
            }
        }
        return new PillarPlan(MAXIMUM_PILLAR_HEIGHT, false);
    }

    static boolean compatibleSurfaceHeight(
            int requestedAirY,
            int actualAirY
    ) {
        return requestedAirY >= MINIMUM_Y
                && actualAirY >= requestedAirY
                && actualAirY <= requestedAirY + 2;
    }

    static int[][] cardinalSupportSamples() {
        return new int[][]{
                {0, 0},
                {-FOUNDATION_RADIUS, 0},
                {FOUNDATION_RADIUS, 0},
                {0, -FOUNDATION_RADIUS},
                {0, FOUNDATION_RADIUS}
        };
    }

    static int floorY(int requestedAirY) {
        return requestedAirY + FLOOR_OFFSET;
    }

    record PillarPlan(int height, boolean growVines) {
    }
}
