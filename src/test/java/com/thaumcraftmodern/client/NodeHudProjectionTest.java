package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeHudProjectionTest {
    @Test
    void straightAheadProjectsToScreenCenter() {
        NodeHudProjection.ScreenPoint point = project(0.0D, 0.0D, 5.0D);

        assertEquals(160, point.x());
        assertEquals(90, point.y());
    }

    @Test
    void pointAboveViewProjectsAboveCenter() {
        NodeHudProjection.ScreenPoint point = project(0.0D, 1.0D, 5.0D);

        assertEquals(160, point.x());
        assertTrue(point.y() < 90);
    }

    @Test
    void pointBehindCameraIsNotProjected() {
        assertTrue(NodeHudProjection.project(
                0.0D, 0.0D, -1.0D,
                0.0D, 0.0D, 1.0D,
                0.0D, 1.0D, 0.0D,
                -1.0D, 0.0D, 0.0D,
                70.0D, 320, 180
        ).isEmpty());
    }

    @Test
    void visibleHudKeepsWorldAnchor() {
        NodeHudProjection.HudAnchor anchor = NodeHudProjection.pinToViewport(
                new NodeHudProjection.ScreenPoint(160, 90), 320, 180);

        assertEquals(NodeHudProjection.AnchorMode.WORLD, anchor.mode());
        assertEquals(160, anchor.x());
        assertEquals(90, anchor.y());
    }

    @Test
    void offscreenHudPinsToCameraSafeEdge() {
        NodeHudProjection.HudAnchor anchor = NodeHudProjection.pinToViewport(
                new NodeHudProjection.ScreenPoint(160, -80), 320, 180);

        assertEquals(NodeHudProjection.AnchorMode.CAMERA_PINNED, anchor.mode());
        assertEquals(160, anchor.x());
        assertEquals(44, anchor.y());
    }

    private static NodeHudProjection.ScreenPoint project(
            double x,
            double y,
            double z
    ) {
        return NodeHudProjection.project(
                x, y, z,
                0.0D, 0.0D, 1.0D,
                0.0D, 1.0D, 0.0D,
                -1.0D, 0.0D, 0.0D,
                70.0D, 320, 180
        ).orElseThrow();
    }
}
