package com.thaumcraftmodern.aura;

/** Pure TC4 timing and stabilization rules used by the server ticker. */
final class AuraNodeRegenerationPolicy {
    static final long CLASSIC_CATCH_UP_MILLIS_PER_TICK = 75L;

    private AuraNodeRegenerationPolicy() {
    }

    static int interval(AuraNodeModifier modifier, int stabilizerLock) {
        int interval = switch (modifier) {
            case BRIGHT -> 400;
            case PALE -> 900;
            case FADING -> 0;
            case NORMAL -> 600;
        };
        if (stabilizerLock == 1) {
            interval *= 2;
        } else if (stabilizerLock == 2) {
            interval *= 20;
        }
        return interval;
    }

    static int missedCycles(
            long nowMillis,
            long lastActiveMillis,
            int intervalTicks,
            int maximumCycles
    ) {
        if (lastActiveMillis <= 0L || nowMillis <= lastActiveMillis
                || intervalTicks <= 0 || maximumCycles <= 0) {
            return 0;
        }
        long cycleMillis = Math.multiplyExact(
                intervalTicks,
                CLASSIC_CATCH_UP_MILLIS_PER_TICK
        );
        long elapsedCycles = (nowMillis - lastActiveMillis) / cycleMillis;
        return (int) Math.min(elapsedCycles, maximumCycles);
    }

    static long advanceLastActive(long lastActiveMillis, int intervalTicks,
                                  int elapsedCycles) {
        if (lastActiveMillis <= 0L || intervalTicks <= 0
                || elapsedCycles <= 0) {
            return lastActiveMillis;
        }
        long cycleMillis = Math.multiplyExact(
                intervalTicks,
                CLASSIC_CATCH_UP_MILLIS_PER_TICK
        );
        return Math.addExact(
                lastActiveMillis,
                Math.multiplyExact(cycleMillis, elapsedCycles)
        );
    }

    static int unstableImprovementBound(int stabilizerLock) {
        return stabilizerLock > 0 ? 10_000 / stabilizerLock : 0;
    }

    static int fadingImprovementBound(int stabilizerLock) {
        return stabilizerLock > 0 ? 12_500 / stabilizerLock : 0;
    }
}
