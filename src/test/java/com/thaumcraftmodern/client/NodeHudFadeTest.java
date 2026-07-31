package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeHudFadeTest {
    @Test
    void fadesAndScalesInThenOut() {
        NodeHudFade fade = new NodeHudFade();
        assertFalse(fade.update(true, 1_000L).visible());

        NodeHudFade.Frame halfwayIn = fade.update(
                true,
                1_000L + NodeHudFade.FADE_IN_MILLIS / 2
        );
        assertTrue(halfwayIn.visible());
        assertTrue(halfwayIn.alpha() > 0.0F);
        assertTrue(halfwayIn.alpha() < 1.0F);
        assertTrue(halfwayIn.scale() > 0.78F);
        assertTrue(halfwayIn.scale() < 1.0F);

        NodeHudFade.Frame fullyIn = fade.update(
                true,
                1_000L + NodeHudFade.FADE_IN_MILLIS
        );
        assertEquals(1.0F, fullyIn.alpha(), 0.001F);

        fade.update(false, 1_000L + NodeHudFade.FADE_IN_MILLIS + 100L);
        NodeHudFade.Frame fullyOut = fade.update(
                false,
                1_000L + NodeHudFade.FADE_IN_MILLIS
                        + NodeHudFade.FADE_OUT_MILLIS + 100L
        );
        assertFalse(fullyOut.visible());
    }
}
