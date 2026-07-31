package com.thaumcraftmodern.item;

import net.minecraft.world.InteractionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumometerInteractionEventsTest {
    @Test
    void entityScanConsumesInteractionWithoutVanillaSwing() {
        assertSame(
                InteractionResult.CONSUME,
                ThaumometerInteractionEvents.ENTITY_SCAN_RESULT
        );
        assertTrue(ThaumometerInteractionEvents.ENTITY_SCAN_RESULT.consumesAction());
        assertFalse(ThaumometerInteractionEvents.ENTITY_SCAN_RESULT.shouldSwing());
    }
}
