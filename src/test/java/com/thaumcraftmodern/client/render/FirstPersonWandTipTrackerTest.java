package com.thaumcraftmodern.client.render;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FirstPersonWandTipTrackerTest {
    private static final float EPSILON = 1.0E-4F;

    @Test
    void projectionRoundTripPreservesRenderedTip() {
        Matrix4f modelView = new Matrix4f()
                .translate(0.35F, -0.2F, -2.5F)
                .rotateXYZ(0.2F, -0.4F, 0.1F)
                .scale(0.7F);
        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(70.0D), 16.0F / 9.0F,
                        0.05F, 100.0F);
        Vector4f tip = new Vector4f(0.0F, -1.0F / 16.0F, 0.0F, 1.0F);

        Vector4f projected = FirstPersonWandTipTracker.projectToNdc(
                modelView,
                projection,
                tip
        );
        assertNotNull(projected);

        Vector4f restored = FirstPersonWandTipTracker.unprojectFromNdc(
                modelView,
                projection,
                projected
        );
        assertNotNull(restored);
        assertEquals(tip.x, restored.x, EPSILON);
        assertEquals(tip.y, restored.y, EPSILON);
        assertEquals(tip.z, restored.z, EPSILON);
    }

    @Test
    void projectionRejectsPointWithZeroClipW() {
        Vector4f projected = FirstPersonWandTipTracker.projectToNdc(
                new Matrix4f(),
                new Matrix4f().zero(),
                new Vector4f(0.0F, 0.0F, 0.0F, 1.0F)
        );

        assertNull(projected);
    }
}
