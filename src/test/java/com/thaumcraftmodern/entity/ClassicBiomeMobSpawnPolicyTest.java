package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClassicBiomeMobSpawnPolicyTest {
    @Test
    void pechUsesTheClassicFourWithinSixteenLimit() {
        assertTrue(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.PECH,
                3
        ));
        assertFalse(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.PECH,
                4
        ));
    }

    @Test
    void wispUsesTheClassicEightWithinSixteenLimit() {
        assertTrue(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.WISP,
                7
        ));
        assertFalse(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.WISP,
                8
        ));
    }

    @Test
    void taintacleAllowsNoSecondNearbyTaintacle() {
        assertTrue(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.TAINTACLE,
                0
        ));
        assertFalse(ClassicBiomeMobSpawnPolicy.allowsPopulation(
                LegacyMobKind.TAINTACLE,
                1
        ));
    }
}
