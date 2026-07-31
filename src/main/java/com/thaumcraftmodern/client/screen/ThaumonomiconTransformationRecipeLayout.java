package com.thaumcraftmodern.client.screen;

/**
 * Physical 512px-atlas coordinates and screen geometry used by TC4's
 * {@code SMELTING} research page.
 */
final class ThaumonomiconTransformationRecipeLayout {
    static final int WIDTH = 112;
    static final int OVERLAY_TOP = 28;
    static final int OVERLAY_SOURCE_X = 0;
    static final int OVERLAY_SOURCE_Y = 384;
    static final int OVERLAY_WIDTH = 112;
    static final int OVERLAY_HEIGHT = 128;
    static final int INPUT_X = 48;
    static final int INPUT_Y = 64;
    static final int OUTPUT_X = 48;
    static final int OUTPUT_Y = 144;

    private ThaumonomiconTransformationRecipeLayout() {
    }

    static int left(int pageLeft, int pageWidth) {
        return pageLeft + (pageWidth - WIDTH) / 2;
    }
}
