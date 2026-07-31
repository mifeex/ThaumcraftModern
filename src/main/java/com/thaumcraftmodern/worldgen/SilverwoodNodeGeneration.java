package com.thaumcraftmodern.worldgen;

/**
 * Pure-node limits for the classic Silverwood trunk.
 */
final class SilverwoodNodeGeneration {
    static final int MAX_NODES = 2;

    private SilverwoodNodeGeneration() {
    }

    static int initialChanceBound(int treeHeight) {
        return (int) (treeHeight * 1.5D);
    }

    static boolean shouldPlace(int height, int placedNodes, int roll) {
        return height > 0 && placedNodes < MAX_NODES && roll == 0;
    }

    /**
     * TC4's {@code worldgen} flag controls chunk-loading and flower
     * decoration, not knot/node creation. Sapling-grown trees therefore use
     * the same embedded-node roll as wild trees.
     */
    static boolean shouldPlaceForTree(
            boolean wild,
            int height,
            int placedNodes,
            int roll
    ) {
        return shouldPlace(height, placedNodes, roll);
    }

    static int nextChanceBound(int currentBound, int treeHeight) {
        return currentBound + treeHeight;
    }
}
