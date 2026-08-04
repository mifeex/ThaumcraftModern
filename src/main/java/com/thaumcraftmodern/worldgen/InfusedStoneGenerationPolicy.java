package com.thaumcraftmodern.worldgen;

import net.minecraft.util.RandomSource;

/** Height-band density policy for adapting TC4's eight attempts to 1.20 worlds. */
public final class InfusedStoneGenerationPolicy {
    public static final int UPPER_PERCENT = 90;
    public static final int DEEPSLATE_PERCENT = 80;

    private InfusedStoneGenerationPolicy() {
    }

    public static int scaledAttemptCount(int baseAttempts, int percent,
            RandomSource random) {
        if (baseAttempts <= 0 || percent <= 0) return 0;
        int hundredths = baseAttempts * percent;
        int wholeAttempts = hundredths / 100;
        int remainder = hundredths % 100;
        return wholeAttempts + (remainder > 0 && random.nextInt(100) < remainder
                ? 1 : 0);
    }
}
