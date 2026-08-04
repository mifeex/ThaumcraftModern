package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ThaumonomiconResearchTintTest {
    @Test
    void availableResearchUsesExactTc4SixHundredMillisecondGrayPulse() {
        assertEquals(
                0xFFBFBFBF,
                ThaumonomiconScreen.availableResearchTint(0L)
        );
        assertEquals(
                0xFFFFFFFF,
                ThaumonomiconScreen.availableResearchTint(150L)
        );
        assertEquals(
                0xFFBFBFBF,
                ThaumonomiconScreen.availableResearchTint(300L)
        );
        assertEquals(
                0xFF808080,
                ThaumonomiconScreen.availableResearchTint(450L)
        );
        assertEquals(
                ThaumonomiconScreen.availableResearchTint(17L),
                ThaumonomiconScreen.availableResearchTint(617L)
        );
    }
}
