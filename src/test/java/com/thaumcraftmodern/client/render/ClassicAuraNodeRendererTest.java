package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.aura.NodeVisibilityService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClassicAuraNodeRendererTest {
    @Test
    void gogglesAndJarredNodesUseTheClassicSixtyFourBlockDistance() {
        assertEquals(64, ClassicAuraNodeRenderer.VIEW_DISTANCE);
        assertEquals(
                64,
                ClassicAuraNodeRenderer.viewDistanceFor(
                        NodeVisibilityService.Visibility.REVEALED_BY_GOGGLES
                )
        );
        assertEquals(
                64,
                ClassicAuraNodeRenderer.viewDistanceFor(
                        NodeVisibilityService.Visibility.SUBTLE
                )
        );
    }

    @Test
    void heldThaumometerUsesTheClassicFortyEightBlockDistance() {
        assertEquals(48, ClassicAuraNodeRenderer.THAUMOMETER_VIEW_DISTANCE);
        assertEquals(
                48,
                ClassicAuraNodeRenderer.viewDistanceFor(
                        NodeVisibilityService.Visibility
                                .REVEALED_BY_THAUMOMETER
                )
        );
    }

    @Test
    void unrevealedNodeUsesTenPercentOpacity() {
        assertEquals(
                0.10F,
                ClassicAuraNodeRenderer.SUBTLE_NODE_ALPHA,
                0.0001F
        );
    }
}
