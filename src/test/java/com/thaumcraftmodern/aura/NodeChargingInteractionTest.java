package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NodeChargingInteractionTest {
    @Test
    void clientHoldConsumesUseWithoutVanillaInteractionSwing() {
        assertSame(
                net.minecraft.world.InteractionResult.CONSUME,
                NodeChargingService.CLIENT_HOLD_RESULT
        );
        assertTrue(
                NodeChargingService.CLIENT_HOLD_RESULT.consumesAction()
        );
        assertFalse(
                NodeChargingService.CLIENT_HOLD_RESULT.shouldSwing()
        );
    }
}
