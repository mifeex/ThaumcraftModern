package com.thaumcraftmodern.essentia.tube;

import net.minecraft.core.Direction;

import java.util.Objects;
import java.util.function.Predicate;

/** Pure TC4 tube-facing selection used by valves and directional tubes. */
public final class TubeFacingRules {
    private TubeFacingRules() {
    }

    /**
     * TC4 advances through ForgeDirection order and stops at the first side
     * without an adjacent essentia transport. If all six sides are occupied,
     * the current facing is retained.
     */
    public static Direction nextFreeSide(Direction current,
            Predicate<Direction> occupied) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(occupied, "occupied");
        Direction[] directions = Direction.values();
        int start = current.ordinal();
        for (int step = 1; step <= directions.length; step++) {
            Direction candidate = directions[(start + step) % directions.length];
            if (!occupied.test(candidate)) {
                return candidate;
            }
        }
        return current;
    }
}
