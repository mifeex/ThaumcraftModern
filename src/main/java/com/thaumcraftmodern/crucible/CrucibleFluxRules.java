package com.thaumcraftmodern.crucible;

/**
 * Runtime-independent constants and rolls copied from TC4
 * {@code TileCrucible}.
 */
public final class CrucibleFluxRules {
    public static final int MAX_ESSENTIA = 100;
    public static final int OVERFLOW_INTERVAL_TICKS = 5;
    public static final int SPILL_CHANCE_DENOMINATOR = 4;
    public static final int INITIAL_FLUX_LEVEL = 0;

    private CrucibleFluxRules() {
    }

    public static boolean shouldOverflow(int essentia, long counter) {
        return essentia > MAX_ESSENTIA
                && counter % OVERFLOW_INTERVAL_TICKS == 0L;
    }

    public static boolean materializesFlux(int spillRoll) {
        return spillRoll == 0;
    }

    public static int remnantSpillAttempts(int essentia) {
        return Math.max(0, essentia) / 2;
    }
}
