package com.thaumcraftmodern.client;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;

/**
 * Carries the exact transform sampled by {@code RenderHandEvent} into the HUD
 * overlay rendered later in the same client frame.
 */
public final class ThaumometerFirstPersonPose {
    private static final long MAX_AGE_MILLIS = 500L;
    private static Snapshot latest;

    private ThaumometerFirstPersonPose() {
    }

    static void capture(
            InteractionHand owner,
            ThaumometerSwingAnimation.Transform transform
    ) {
        latest = new Snapshot(owner, transform, Util.getMillis());
    }

    static ThaumometerSwingAnimation.Transform currentOr(
            InteractionHand owner,
            ThaumometerSwingAnimation.Transform fallback
    ) {
        Snapshot snapshot = latest;
        if (snapshot == null
                || snapshot.owner != owner
                || Util.getMillis() - snapshot.capturedAtMillis > MAX_AGE_MILLIS) {
            return fallback;
        }
        return snapshot.transform;
    }

    private record Snapshot(
            InteractionHand owner,
            ThaumometerSwingAnimation.Transform transform,
            long capturedAtMillis
    ) {
    }
}
