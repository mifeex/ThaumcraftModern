package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconResearchTintTest {
    @Test
    void completedHiddenResearchNeverReportsItselfLocked() {
        assertEquals(true,
                ThaumonomiconScreen.researchIsUnlocked(true, false));
        assertEquals(true,
                ThaumonomiconScreen.researchIsUnlocked(false, true));
        assertEquals(false,
                ThaumonomiconScreen.researchIsUnlocked(false, false));
    }

    @Test
    void availableResearchUsesRequestedEightHundredMillisecondGrayPulse() {
        assertEquals(
                0xFFBFBFBF,
                ThaumonomiconScreen.availableResearchTint(0L)
        );
        assertEquals(
                0xFFFFFFFF,
                ThaumonomiconScreen.availableResearchTint(200L)
        );
        assertEquals(
                0xFFBFBFBF,
                ThaumonomiconScreen.availableResearchTint(400L)
        );
        assertEquals(
                0xFF808080,
                ThaumonomiconScreen.availableResearchTint(600L)
        );
        assertEquals(
                ThaumonomiconScreen.availableResearchTint(17L),
                ThaumonomiconScreen.availableResearchTint(817L)
        );
    }

    @Test
    void researchHitTestingStopsAtTheInnerBookViewport() {
        assertTrue(ThaumonomiconScreen.isWithinResearchViewport(16, 16));
        assertTrue(ThaumonomiconScreen.isWithinResearchViewport(239.999, 212.999));
        assertFalse(ThaumonomiconScreen.isWithinResearchViewport(15.999, 16));
        assertFalse(ThaumonomiconScreen.isWithinResearchViewport(240, 16));
        assertFalse(ThaumonomiconScreen.isWithinResearchViewport(16, 15.999));
        assertFalse(ThaumonomiconScreen.isWithinResearchViewport(16, 213));
    }
}
