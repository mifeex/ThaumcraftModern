package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClassicBiomeMobSpawnPolicyTest {
    @Test
    void pechUsesTheClassicFourWithinSixteenLimit() {
        assertTrue(LegacyMobKind.PECH.allowsClassicBiomePopulation(3));
        assertFalse(LegacyMobKind.PECH.allowsClassicBiomePopulation(4));
    }

    @Test
    void wispUsesTheClassicEightWithinSixteenLimit() {
        assertTrue(LegacyMobKind.WISP.allowsClassicBiomePopulation(7));
        assertFalse(LegacyMobKind.WISP.allowsClassicBiomePopulation(8));
    }

    @Test
    void taintacleAllowsNoSecondNearbyTaintacle() {
        assertTrue(LegacyMobKind.TAINTACLE.allowsClassicBiomePopulation(0));
        assertFalse(LegacyMobKind.TAINTACLE.allowsClassicBiomePopulation(1));
    }
}
