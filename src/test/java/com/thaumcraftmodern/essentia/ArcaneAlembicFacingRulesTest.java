package com.thaumcraftmodern.essentia;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneAlembicFacingRulesTest {
    @Test
    void panelFacesThePlayerForEveryPlacementDirection() {
        assertEquals(Direction.SOUTH,
                ArcaneAlembicFacingRules.facingPlayer(Direction.NORTH));
        assertEquals(Direction.NORTH,
                ArcaneAlembicFacingRules.facingPlayer(Direction.SOUTH));
        assertEquals(Direction.EAST,
                ArcaneAlembicFacingRules.facingPlayer(Direction.WEST));
        assertEquals(Direction.WEST,
                ArcaneAlembicFacingRules.facingPlayer(Direction.EAST));
    }

    @Test
    void pipesNeverConnectToThePanelOrBottom() {
        for (Direction panel : Direction.Plane.HORIZONTAL) {
            assertFalse(ArcaneAlembicFacingRules.isPipeConnectable(panel, panel));
            assertFalse(ArcaneAlembicFacingRules.isPipeConnectable(
                    panel, Direction.DOWN));
            assertTrue(ArcaneAlembicFacingRules.isPipeConnectable(
                    panel, Direction.UP));
            assertTrue(ArcaneAlembicFacingRules.isPipeConnectable(
                    panel, panel.getClockWise()));
        }
    }
}
