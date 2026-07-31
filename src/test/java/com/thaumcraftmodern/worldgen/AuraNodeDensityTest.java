package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class AuraNodeDensityTest {
    @Test
    void runtimeRollMatchesOneAttemptPerThirtySixEligibleChunks() {
        RandomSource random = RandomSource.create(0x544841554D435241L);
        int chunks = 360_000;
        int candidates = 0;
        for (int chunk = 0; chunk < chunks; chunk++) {
            if (AuraNodeSpawnRoll.roll(random)) {
                candidates++;
            }
        }
        double frequency = candidates / (double) chunks;
        assertTrue(
                frequency >= 1.0D / 37.0D
                        && frequency <= 1.0D / 35.0D,
                "Expected the TC4 1/36 roll, got "
                        + candidates + "/" + chunks
        );
    }

    @Test
    void taintedLandGetsThreeTimesTheVisibleNodeDensity() {
        RandomSource random = RandomSource.create(0x71A17EDL);
        int chunks = 120_000;
        int candidates = 0;
        for (int chunk = 0; chunk < chunks; chunk++) {
            if (AuraNodeSpawnRoll.roll(random, true)) {
                candidates++;
            }
        }
        double frequency = candidates / (double) chunks;
        assertTrue(
                frequency >= 1.0D / 12.5D
                        && frequency <= 1.0D / 11.5D,
                "Expected the Tainted Land 1/12 roll, got "
                        + candidates + "/" + chunks
        );
    }
}
