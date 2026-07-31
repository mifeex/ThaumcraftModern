package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.world.block.FluxWaterInteraction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluxWaterInteractionTest {
    @Test
    void flowingWaterOnlyWashesLevelsZeroThroughFour() {
        assertTrue(FluxWaterInteraction.mayWash(0, false, true));
        assertTrue(FluxWaterInteraction.mayWash(4, false, true));
        assertFalse(FluxWaterInteraction.mayWash(5, false, true));
        assertFalse(FluxWaterInteraction.mayWash(7, false, true));
    }

    @Test
    void sourceWaterWashesEveryFluxLevel() {
        assertTrue(FluxWaterInteraction.mayWash(0, true, false));
        assertTrue(FluxWaterInteraction.mayWash(5, true, false));
        assertTrue(FluxWaterInteraction.mayWash(7, true, false));
    }
}
