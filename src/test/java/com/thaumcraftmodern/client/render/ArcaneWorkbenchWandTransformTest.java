package com.thaumcraftmodern.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArcaneWorkbenchWandTransformTest {
    @Test
    void centresTheToolOnTheWorkbenchGrid() {
        assertEquals(1.0625D, ArcaneWorkbenchWandTransform.Y);
        assertEquals(90.0F, ArcaneWorkbenchWandTransform.X_ROTATION);
        assertEquals(45.0F, ArcaneWorkbenchWandTransform.Z_ROTATION);
        assertEquals(0.6D, ArcaneWorkbenchWandTransform.TOOL_Y_OFFSET);
        assertEquals(1.0D, ArcaneWorkbenchWandTransform.STAFF_Y_OFFSET);
        assertEquals(0.5F, ArcaneWorkbenchWandTransform.TOOL_SCALE);
        assertEquals(0.45F, ArcaneWorkbenchWandTransform.STAFF_SCALE);

        double angle = Math.toRadians(
                ArcaneWorkbenchWandTransform.Z_ROTATION
        );
        double centreX = ArcaneWorkbenchWandTransform.X
                - ArcaneWorkbenchWandTransform.TOOL_CENTER_FROM_PIVOT
                * Math.sin(angle);
        double centreZ = ArcaneWorkbenchWandTransform.Z
                + ArcaneWorkbenchWandTransform.TOOL_CENTER_FROM_PIVOT
                * Math.cos(angle);
        assertEquals(0.5D, centreX, 1.0E-12D);
        assertEquals(0.5D, centreZ, 1.0E-12D);
    }
}
