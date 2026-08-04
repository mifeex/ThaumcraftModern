package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoundGuardianSpawnRulesTest {
    @Test
    void eerieSpreadUsesOriginalTriangularTwentyThreeBlockWindow() {
        assertEquals(-11, MoundGuardianSpawnRules.biomeOffset(0, 11));
        assertEquals(0, MoundGuardianSpawnRules.biomeOffset(7, 7));
        assertEquals(11, MoundGuardianSpawnRules.biomeOffset(11, 0));
    }

    @Test
    void matchesClassicFiftyTickPlayerAndPopulationGates() {
        assertTrue(MoundGuardianSpawnRules.mayAttempt(
                true,
                50,
                true,
                true,
                2
        ));
        assertTrue(MoundGuardianSpawnRules.mayAttempt(
                true,
                50,
                true,
                true,
                3
        ));
        assertFalse(MoundGuardianSpawnRules.mayAttempt(
                true,
                49,
                true,
                true,
                3
        ));
        assertFalse(MoundGuardianSpawnRules.mayAttempt(
                true,
                50,
                false,
                true,
                3
        ));
        assertFalse(MoundGuardianSpawnRules.mayAttempt(
                true,
                50,
                true,
                false,
                3
        ));
        assertFalse(MoundGuardianSpawnRules.mayAttempt(
                true,
                50,
                true,
                true,
                4
        ));
        assertFalse(MoundGuardianSpawnRules.mayAttempt(
                false,
                50,
                true,
                true,
                3
        ));
    }
}
