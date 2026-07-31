package com.thaumcraftmodern.essentia.tube;

import net.minecraft.core.Direction;

/** Pure branch selection shared by tube interaction and regression tests. */
public final class TubeWandTargetResolver {
    private static final double CORE_MIN = 5.5D / 16.0D;
    private static final double CORE_MAX = 10.5D / 16.0D;

    private TubeWandTargetResolver() {
    }

    public static Direction resolve(double x, double y, double z,
            Direction centreFace) {
        double dx=x-0.5D, dy=y-0.5D, dz=z-0.5D;
        double ax=Math.abs(dx), ay=Math.abs(dy), az=Math.abs(dz);
        if (Math.max(ax, Math.max(ay, az)) <= 2.0D/16.0D + 1.0E-6D) {
            return centreFace;
        }
        if (ax >= ay && ax >= az) return dx < 0 ? Direction.WEST : Direction.EAST;
        if (ay >= az) return dy < 0 ? Direction.DOWN : Direction.UP;
        return dz < 0 ? Direction.NORTH : Direction.SOUTH;
    }

    /** TC4 ray-tracer sub-hit 6: the 5x5x5 central control cube. */
    public static boolean hitsCore(double x, double y, double z) {
        return withinCore(x) && withinCore(y) && withinCore(z);
    }

    private static boolean withinCore(double coordinate) {
        return coordinate >= CORE_MIN - 1.0E-6D
                && coordinate <= CORE_MAX + 1.0E-6D;
    }
}
