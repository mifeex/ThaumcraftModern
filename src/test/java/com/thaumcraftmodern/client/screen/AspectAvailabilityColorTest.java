package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AspectAvailabilityColorTest {
    private static final int ASPECT_COLOR = 0x12A4EF;

    @Test
    void availableAspectAlwaysKeepsItsOwnColor() {
        assertEquals(
                ASPECT_COLOR,
                AspectAvailabilityColor.resolve(ASPECT_COLOR, true, 320L)
        );
    }

    @Test
    void unavailableAspectPulsesFromItsOwnColorToGrayAndBack() {
        assertEquals(
                ASPECT_COLOR,
                AspectAvailabilityColor.resolve(ASPECT_COLOR, false, 0L)
        );
        assertEquals(
                AspectAvailabilityColor.UNAVAILABLE_GRAY,
                AspectAvailabilityColor.resolve(ASPECT_COLOR, false, 320L)
        );
        assertEquals(
                ASPECT_COLOR,
                AspectAvailabilityColor.resolve(ASPECT_COLOR, false, 640L)
        );
    }
}
