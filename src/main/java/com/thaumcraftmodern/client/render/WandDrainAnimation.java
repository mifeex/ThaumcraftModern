package com.thaumcraftmodern.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Calm first-person node-draining motion derived from gameplay captures.
 *
 * <p>The casting tool eases from its ordinary vertical placement to a
 * 72-degree screen angle. Once settled, the complete tool follows one small,
 * slow clockwise circle. There are deliberately no independent X/Z waves:
 * those rotations made long wands and staves snap across the screen.</p>
 */
final class WandDrainAnimation {
    static final float VERTICAL_HOLD_TICKS = 4.0F;
    static final float ENTRY_TICKS = 12.0F;
    static final float RETURN_TICKS = 12.0F;
    static final float FORWARD_TILT_X_DEGREES = -24.0F;
    static final float TARGET_FROM_VERTICAL_DEGREES = 18.0F;
    static final float ORBIT_RADIUS = 0.0180F;
    static final float ORBIT_PERIOD_TICKS = 120.0F;
    static final float ORBIT_EASE_TICKS = 12.0F;

    private static final float FULL_CIRCLE = Mth.TWO_PI;

    private WandDrainAnimation() {
    }

    static Transform sample(float elapsedUseTicks, HumanoidArm arm) {
        float elapsed = Math.max(0.0F, elapsedUseTicks);
        float entryTicks = Math.max(
                0.0F,
                elapsed - VERTICAL_HOLD_TICKS
        );
        float entryProgress = smoothStep(
                Mth.clamp(entryTicks / ENTRY_TICKS, 0.0F, 1.0F)
        );
        float handSign = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;

        float orbitTicks = Math.max(
                0.0F,
                elapsed - VERTICAL_HOLD_TICKS - ENTRY_TICKS
        );
        float orbitEase = smoothStep(
                Mth.clamp(orbitTicks / ORBIT_EASE_TICKS, 0.0F, 1.0F)
        );
        float phase = orbitTicks / ORBIT_PERIOD_TICKS * FULL_CIRCLE;
        float radius = ORBIT_RADIUS * orbitEase;

        /*
         * Starting at the top of the circle and moving right produces a
         * clockwise path in first-person screen coordinates. Subtracting one
         * from cos keeps the transition position continuous at orbit start.
         */
        float orbitX = Mth.sin(phase) * radius;
        float orbitY = (Mth.cos(phase) - 1.0F) * radius;
        return new Transform(
                FORWARD_TILT_X_DEGREES * entryProgress,
                -TARGET_FROM_VERTICAL_DEGREES
                        * entryProgress
                        * handSign,
                orbitX,
                orbitY
        );
    }

    static Transform sampleReturn(
            float elapsedReturnTicks,
            Transform releasePose
    ) {
        float progress = smoothStep(Mth.clamp(
                Math.max(0.0F, elapsedReturnTicks) / RETURN_TICKS,
                0.0F,
                1.0F
        ));
        float remaining = 1.0F - progress;
        return new Transform(
                releasePose.forwardTiltX() * remaining,
                releasePose.tiltZ() * remaining,
                releasePose.orbitX() * remaining,
                releasePose.orbitY() * remaining
        );
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    record Transform(
            float forwardTiltX,
            float tiltZ,
            float orbitX,
            float orbitY
    ) {
    }
}
