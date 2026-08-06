package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ThaumonomiconDurabilityPreviewTest {
    @Test
    void cyclesFromFullToEmptyAtTheClassicFortressHelmetPace() {
        assertEquals(0, ThaumonomiconDurabilityPreview.damageAtTime(0, 440));
        assertEquals(1, ThaumonomiconDurabilityPreview.damageAtTime(10, 440));
        assertEquals(440, ThaumonomiconDurabilityPreview.damageAtTime(4_400, 440));
        assertEquals(0, ThaumonomiconDurabilityPreview.damageAtTime(4_410, 440));
    }

    @Test
    void differentMaximumDurabilitiesShareTheSameVisualPercentage() {
        long halfway = ThaumonomiconDurabilityPreview.CYCLE_MILLIS / 2;
        assertEquals(220,
                ThaumonomiconDurabilityPreview.damageAtTime(halfway, 440));
        assertEquals(50,
                ThaumonomiconDurabilityPreview.damageAtTime(halfway, 100));
        assertEquals(780,
                ThaumonomiconDurabilityPreview.damageAtTime(halfway, 1_560));
    }

    @Test
    void handlesClockWrapAndNonDamageableItems() {
        assertEquals(440, ThaumonomiconDurabilityPreview.damageAtTime(-10, 440));
        assertEquals(0, ThaumonomiconDurabilityPreview.damageAtTime(500, 0));
    }
}
