package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeVisibilityServiceTest {
    @Test
    void unrevealedNodeKeepsOnlySubtleVisibility() {
        NodeVisibilityService.Visibility visibility = NodeVisibilityService.decide(
                new NodeVisibilityService.Facts(false, false)
        );

        assertEquals(NodeVisibilityService.Visibility.SUBTLE, visibility);
        assertFalse(visibility.revealed());
    }

    @Test
    void heldThaumometerRevealsNodeBeforeScanning() {
        assertTrue(NodeVisibilityService.decide(
                new NodeVisibilityService.Facts(true, false)
        ).revealed());
        assertFalse(NodeVisibilityService.decide(
                new NodeVisibilityService.Facts(false, false)
        ).revealed());
    }

    @Test
    void vanillaHeadSlotGogglesRevealContinuouslyAndTakePriority() {
        assertEquals(
                NodeVisibilityService.Visibility.REVEALED_BY_GOGGLES,
                NodeVisibilityService.decide(
                        new NodeVisibilityService.Facts(true, true)
                )
        );
    }
}
