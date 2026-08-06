package com.thaumcraftmodern.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassicUiRenderTest {
    @Test void aspectAmountsStayBelowPopupLayers() {
        assertEquals(10.0F, ClassicUiRender.ASPECT_AMOUNT_Z);
    }

    @Test void formatsInternalCentiVisAsOriginalPlayerFacingVis() {
        assertEquals("2", ClassicUiRender.formatVis(200));
        assertEquals("8", ClassicUiRender.formatVis(800));
        assertEquals("0.5", ClassicUiRender.formatVis(50));
        assertEquals("32", ClassicUiRender.formatVis(3200));
    }
}
