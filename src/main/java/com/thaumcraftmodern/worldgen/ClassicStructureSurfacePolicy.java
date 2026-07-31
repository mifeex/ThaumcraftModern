package com.thaumcraftmodern.worldgen;

/** Pure TC4 4.2.3.5 surface-block rules for standalone structures. */
final class ClassicStructureSurfacePolicy {
    static final int MAX_UPWARD_SEARCH = 2;

    private ClassicStructureSurfacePolicy() {
    }

    static boolean acceptsMoundOrHilltop(
            boolean stone,
            boolean grass,
            boolean dirt,
            boolean surfaceCover,
            boolean validBlockBelowCover
    ) {
        return stone || grass || dirt
                || surfaceCover && validBlockBelowCover;
    }

    static boolean acceptsEldritchRing(
            boolean stone,
            boolean sand,
            boolean terracotta,
            boolean grass,
            boolean gravel,
            boolean dirt,
            boolean surfaceCover,
            boolean validBlockBelowCover
    ) {
        return stone || sand || terracotta || grass || gravel || dirt
                || surfaceCover && validBlockBelowCover;
    }
}
