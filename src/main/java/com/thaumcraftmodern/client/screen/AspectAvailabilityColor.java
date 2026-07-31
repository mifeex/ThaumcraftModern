package com.thaumcraftmodern.client.screen;

/**
 * Shared deterministic color pulse for an aspect payment that is currently
 * unavailable. The 640 ms cycle follows the Research Table's 320 ms visual
 * beat: base color -> gray -> base color.
 */
final class AspectAvailabilityColor {
    static final int UNAVAILABLE_GRAY = 0x6B6B6B;
    static final long PULSE_PERIOD_MILLIS = 640L;

    private AspectAvailabilityColor() {
    }

    static int resolve(int aspectColor, boolean available, long timeMillis) {
        int baseColor = aspectColor & 0x00FFFFFF;
        if (available) {
            return baseColor;
        }
        long phaseMillis = Math.floorMod(timeMillis, PULSE_PERIOD_MILLIS);
        float phase = phaseMillis / (float) PULSE_PERIOD_MILLIS;
        float grayProgress = phase <= 0.5F
                ? phase * 2.0F
                : (1.0F - phase) * 2.0F;
        return interpolate(baseColor, UNAVAILABLE_GRAY, grayProgress);
    }

    private static int interpolate(int from, int to, float progress) {
        int red = channel(from, 16, to, progress);
        int green = channel(from, 8, to, progress);
        int blue = channel(from, 0, to, progress);
        return (red << 16) | (green << 8) | blue;
    }

    private static int channel(int from, int shift, int to, float progress) {
        int start = (from >> shift) & 0xFF;
        int end = (to >> shift) & 0xFF;
        return Math.round(start + (end - start) * progress);
    }
}
