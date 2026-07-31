package com.thaumcraftmodern.crucible;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class CrucibleFluidPresentationTest {
    private static final int CAPACITY = 1000;
    private static final int MAX_ESSENTIA = 100;

    @Test
    void cleanWaterKeepsTheBiomeWaterTint() {
        assertEquals(
                0x3F76E4,
                CrucibleFluidPresentation.color(
                        0x3F76E4,
                        0,
                        MAX_ESSENTIA
                )
        );
        assertEquals(
                0xE82B2B,
                CrucibleFluidPresentation.color(
                        0xE82B2B,
                        0,
                        MAX_ESSENTIA
                )
        );
    }

    @Test
    void essentiaAppliesTheOriginalTc4SaturationModifier() {
        int clean = CrucibleFluidPresentation.color(
                0x3F76E4,
                0,
                MAX_ESSENTIA
        );
        int halfSaturated = CrucibleFluidPresentation.color(
                0x3F76E4,
                50,
                MAX_ESSENTIA
        );
        int saturated = CrucibleFluidPresentation.color(
                0x3F76E4,
                100,
                MAX_ESSENTIA
        );

        assertEquals(0x2F1D8E, halfSaturated);
        assertEquals(0x290071, saturated);
        assertNotEquals(clean, halfSaturated);
        assertNotEquals(halfSaturated, saturated);
    }

    @Test
    void waterVolumeProducesMultipleClassicSurfaceLevels() {
        assertEquals(
                0.8F,
                CrucibleFluidPresentation.height(
                        1000,
                        CAPACITY,
                        0,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
        assertEquals(
                0.775F,
                CrucibleFluidPresentation.height(
                        950,
                        CAPACITY,
                        0,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
        assertEquals(
                0.55F,
                CrucibleFluidPresentation.height(
                        500,
                        CAPACITY,
                        0,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
    }

    @Test
    void essentiaRaisesTheSurfaceAndKeepsTc4BoundaryOffsets() {
        assertEquals(
                0.9F,
                CrucibleFluidPresentation.height(
                        1000,
                        CAPACITY,
                        50,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
        assertEquals(
                0.9999F,
                CrucibleFluidPresentation.height(
                        1000,
                        CAPACITY,
                        100,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
        assertEquals(
                1.001F,
                CrucibleFluidPresentation.height(
                        1000,
                        CAPACITY,
                        101,
                        MAX_ESSENTIA
                ),
                0.00001F
        );
    }
}
