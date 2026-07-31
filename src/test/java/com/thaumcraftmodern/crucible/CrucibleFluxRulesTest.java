package com.thaumcraftmodern.crucible;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleFluxRulesTest {
    @Test
    void overflowMatchesTheOriginalStrictLimitAndFiveTickCadence() {
        assertFalse(CrucibleFluxRules.shouldOverflow(100, 5L));
        assertFalse(CrucibleFluxRules.shouldOverflow(101, 4L));
        assertTrue(CrucibleFluxRules.shouldOverflow(101, 5L));
        assertTrue(CrucibleFluxRules.shouldOverflow(140, -150L));
    }

    @Test
    void eachSpillAttemptHasTheOriginalOneInFourChance() {
        assertEquals(0, CrucibleFluxRules.INITIAL_FLUX_LEVEL);
        assertTrue(CrucibleFluxRules.materializesFlux(0));
        assertFalse(CrucibleFluxRules.materializesFlux(1));
        assertFalse(CrucibleFluxRules.materializesFlux(2));
        assertFalse(CrucibleFluxRules.materializesFlux(3));
    }

    @Test
    void newFluxGooUsesOriginalMetadataZeroAtBothSpillSites()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "CrucibleBlockEntity.java"
        ));
        assertEquals(
                2,
                count(
                        source,
                        "CrucibleFluxRules\n"
                                + "                                                    "
                                + ".INITIAL_FLUX_LEVEL"
                )
        );
    }

    @Test
    void emptyingMakesOneSpillAttemptPerTwoEssentia() {
        assertEquals(0, CrucibleFluxRules.remnantSpillAttempts(0));
        assertEquals(0, CrucibleFluxRules.remnantSpillAttempts(1));
        assertEquals(1, CrucibleFluxRules.remnantSpillAttempts(2));
        assertEquals(7, CrucibleFluxRules.remnantSpillAttempts(15));
    }

    private static int count(String source, String needle) {
        int matches = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            matches++;
            offset += needle.length();
        }
        return matches;
    }
}
