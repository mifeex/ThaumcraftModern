package com.thaumcraftmodern.client;

/**
 * Shared screen-space bounds for the Thaumometer readout and other equipment
 * that presents the same aspect HUD.
 */
final class ThaumometerReadoutLayout {
    private static final int FRAME_MAX_WIDTH = 292;
    private static final int FRAME_MAX_HEIGHT = 316;
    private static final int FRAME_MIN_WIDTH = 200;
    private static final int FRAME_MIN_HEIGHT_LIMIT = 174;
    private static final float FRAME_SCREEN_WIDTH_RATIO = 0.8F;
    private static final float FRAME_SCREEN_HEIGHT_RATIO = 1.00F;
    private static final int FRAME_OFFSET_X = 0;
    private static final int FRAME_OFFSET_Y = -10;

    private static final float READOUT_X_RATIO = 0.14F;
    private static final float READOUT_Y_RATIO = 0.20F;
    private static final float READOUT_WIDTH_RATIO = 0.72F;
    private static final float READOUT_HEIGHT_RATIO = 0.62F;

    private ThaumometerReadoutLayout() {
    }

    static Layout calculate(int screenWidth, int screenHeight) {
        int frameWidth = Math.min(FRAME_MAX_WIDTH, Math.min(
                Math.max(FRAME_MIN_WIDTH, (int) (screenWidth * FRAME_SCREEN_WIDTH_RATIO)),
                Math.max(
                        FRAME_MIN_HEIGHT_LIMIT,
                        (int) (screenHeight * FRAME_SCREEN_HEIGHT_RATIO)
                )
        ));
        int frameHeight = Math.min(
                FRAME_MAX_HEIGHT,
                Math.max(1, Math.round(
                        frameWidth * FRAME_MAX_HEIGHT / (float) FRAME_MAX_WIDTH
                ))
        );
        int frameX = (screenWidth - frameWidth) / 2 + FRAME_OFFSET_X;
        int frameY = (screenHeight - frameHeight) / 2 + FRAME_OFFSET_Y;

        return new Layout(
                frameX,
                frameY,
                frameWidth,
                frameHeight,
                frameX + Math.round(frameWidth * READOUT_X_RATIO),
                frameY + Math.round(frameHeight * READOUT_Y_RATIO),
                Math.round(frameWidth * READOUT_WIDTH_RATIO),
                Math.round(frameHeight * READOUT_HEIGHT_RATIO)
        );
    }

    record Layout(
            int frameX,
            int frameY,
            int frameWidth,
            int frameHeight,
            int readoutX,
            int readoutY,
            int readoutWidth,
            int readoutHeight
    ) {
    }
}
