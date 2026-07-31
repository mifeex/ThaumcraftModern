package com.thaumcraftmodern.client;

import net.minecraft.util.Mth;

/**
 * Time-based counterpart of TC4's {@code tagscale}: it preserves the classic
 * scale-in/scale-out motion and adds the requested alpha fade.
 */
final class NodeHudFade {
    static final long FADE_IN_MILLIS = 180L;
    static final long FADE_OUT_MILLIS = 320L;

    private long lastUpdateMillis = Long.MIN_VALUE;
    private float visibility;

    Frame update(boolean targeted, long nowMillis) {
        if (lastUpdateMillis == Long.MIN_VALUE) {
            lastUpdateMillis = nowMillis;
        }
        long elapsed = Math.max(0L, Math.min(1_000L, nowMillis - lastUpdateMillis));
        lastUpdateMillis = nowMillis;

        float duration = targeted ? FADE_IN_MILLIS : FADE_OUT_MILLIS;
        float direction = targeted ? 1.0F : -1.0F;
        visibility = Mth.clamp(
                visibility + direction * elapsed / duration,
                0.0F,
                1.0F
        );
        float eased = visibility * visibility * (3.0F - 2.0F * visibility);
        return new Frame(eased, 0.78F + 0.22F * eased);
    }

    void reset() {
        lastUpdateMillis = Long.MIN_VALUE;
        visibility = 0.0F;
    }

    record Frame(float alpha, float scale) {
        boolean visible() {
            return alpha > 0.001F;
        }
    }
}
