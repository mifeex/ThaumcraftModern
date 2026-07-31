package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientScanSuccessNotificationTest {
    @Test
    void aspectFlightUsesTheClassicQuadraticCurve() {
        assertEquals(
                40.0D,
                ClientScanOverlay.quadraticBezier(40.0D, 75.0D, 100.0D, 0.0D),
                0.0001D
        );
        assertEquals(
                100.0D,
                ClientScanOverlay.quadraticBezier(40.0D, 75.0D, 100.0D, 1.0D),
                0.0001D
        );
        assertEquals(
                72.5D,
                ClientScanOverlay.quadraticBezier(40.0D, 75.0D, 100.0D, 0.5D),
                0.0001D
        );
    }

    @Test
    void aspectFlightFadesInHoldsAndFadesOutLikeTheReference() {
        assertEquals(0.0F, ClientScanOverlay.flightAlpha(0.0D), 0.0001F);
        assertEquals(1.0F, ClientScanOverlay.flightAlpha(0.3D), 0.0001F);
        assertEquals(1.0F, ClientScanOverlay.flightAlpha(0.66D), 0.0001F);
        assertEquals(0.0F, ClientScanOverlay.flightAlpha(1.0D), 0.0001F);
        assertTrue(ClientScanOverlay.flightAlpha(0.15D) > 0.0F);
        assertTrue(ClientScanOverlay.flightAlpha(0.83D) > 0.0F);
    }

    @Test
    void notificationGlowShrinksAndTravelsAcrossTheTextLikeTheReference() {
        assertEquals(
                187.0F,
                ClientScanOverlay.notificationGlowX(200, 30, 1.0F),
                0.0001F
        );
        assertEquals(
                105.0F,
                ClientScanOverlay.notificationGlowX(200, 30, 0.0F),
                0.0001F
        );
        assertTrue(ClientScanOverlay.notificationGlowAlpha(1.0F) > 0.45F);
        assertTrue(ClientScanOverlay.notificationGlowAlpha(0.0F) < 0.01F);
    }

    @Test
    void finalTextFadeFrameNeverBecomesLegacyOpaqueRgb() {
        assertEquals(0, ClientScanOverlay.successTextAlpha(0));
        assertEquals(4, ClientScanOverlay.successTextAlpha(4));
        assertEquals(4, ClientScanOverlay.successTextAlpha(6));
        assertEquals(127, ClientScanOverlay.successTextAlpha(255));
    }
}
