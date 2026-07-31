package com.thaumcraftmodern.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThaumometerModelCoordinatesTest {
    private static final float EPSILON = 0.00001F;

    @Test
    void guiCoordinatesKeepDirectOriginCalibration() {
        ThaumometerModelCoordinates.Position position =
                ThaumometerModelCoordinates.transform(
                        0.0F,
                        0.0F,
                        0.0F,
                        false
                );

        assertPosition(position, 0.5F, 0.5F, 0.5F);
    }

    @Test
    void firstPersonCoordinatesMatchLegacyBlockCenteredObjTransform() {
        ThaumometerModelCoordinates.Position position =
                ThaumometerModelCoordinates.transform(
                        0.0F,
                        0.0F,
                        0.0F,
                        true
                );

        assertPosition(position, 0.84F, 0.84F, 1.16F);
    }

    private static void assertPosition(
            ThaumometerModelCoordinates.Position actual,
            float expectedX,
            float expectedY,
            float expectedZ
    ) {
        assertEquals(expectedX, actual.x(), EPSILON);
        assertEquals(expectedY, actual.y(), EPSILON);
        assertEquals(expectedZ, actual.z(), EPSILON);
    }
}
