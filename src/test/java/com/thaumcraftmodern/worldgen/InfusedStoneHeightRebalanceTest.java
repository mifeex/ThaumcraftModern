package com.thaumcraftmodern.worldgen;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfusedStoneHeightRebalanceTest {
    @Test
    void defaultTc4AttemptsBecomeSevenOrEightAboveAndSixOrSevenBelow() {
        RandomSource random = RandomSource.create(0x544334L);
        for (int chunk = 0; chunk < 10_000; chunk++) {
            int upper = InfusedStoneGenerationPolicy.scaledAttemptCount(8,
                    InfusedStoneGenerationPolicy.UPPER_PERCENT, random);
            int deep = InfusedStoneGenerationPolicy.scaledAttemptCount(8,
                    InfusedStoneGenerationPolicy.DEEPSLATE_PERCENT, random);
            assertTrue(upper == 7 || upper == 8);
            assertTrue(deep == 6 || deep == 7);
        }
    }

    @Test
    void stochasticRemaindersPreserveRequestedExpectedDensity() {
        RandomSource random = RandomSource.create(0x1F053DL);
        int chunks = 100_000;
        long upper = 0;
        long deep = 0;
        for (int chunk = 0; chunk < chunks; chunk++) {
            upper += InfusedStoneGenerationPolicy.scaledAttemptCount(8, 90, random);
            deep += InfusedStoneGenerationPolicy.scaledAttemptCount(8, 80, random);
        }
        assertEquals(7.2D, upper / (double) chunks, 0.01D);
        assertEquals(6.4D, deep / (double) chunks, 0.01D);
    }

    @Test
    void disabledGenerationRemainsDisabled() {
        assertEquals(0, InfusedStoneGenerationPolicy.scaledAttemptCount(
                0, 90, RandomSource.create(1L)));
    }
}
