package com.thaumcraftmodern.client.render;

import net.minecraft.world.entity.HumanoidArm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WandDrainAnimationTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void startsAtRestWithoutAJump() {
        WandDrainAnimation.Transform pose =
                WandDrainAnimation.sample(0.0F, HumanoidArm.RIGHT);

        assertEquals(0.0F, pose.forwardTiltX(), EPSILON);
        assertEquals(0.0F, pose.tiltZ(), EPSILON);
        assertEquals(0.0F, pose.orbitX(), EPSILON);
        assertEquals(0.0F, pose.orbitY(), EPSILON);
    }

    @Test
    void visiblyHoldsTheVerticalPoseBeforeTilting() {
        WandDrainAnimation.Transform pose =
                WandDrainAnimation.sample(
                        WandDrainAnimation.VERTICAL_HOLD_TICKS,
                        HumanoidArm.RIGHT
                );

        assertEquals(0.0F, pose.forwardTiltX(), EPSILON);
        assertEquals(0.0F, pose.tiltZ(), EPSILON);
        assertEquals(0.0F, pose.orbitX(), EPSILON);
        assertEquals(0.0F, pose.orbitY(), EPSILON);
    }

    @Test
    void smoothlyReachesSeventyTwoDegreeScreenAngle() {
        WandDrainAnimation.Transform halfway =
                WandDrainAnimation.sample(
                        WandDrainAnimation.VERTICAL_HOLD_TICKS
                                + WandDrainAnimation.ENTRY_TICKS / 2.0F,
                        HumanoidArm.RIGHT
                );
        WandDrainAnimation.Transform settled =
                WandDrainAnimation.sample(
                        WandDrainAnimation.VERTICAL_HOLD_TICKS
                                + WandDrainAnimation.ENTRY_TICKS,
                        HumanoidArm.RIGHT
                );

        assertEquals(-12.0F, halfway.forwardTiltX(), EPSILON);
        assertEquals(-9.0F, halfway.tiltZ(), EPSILON);
        assertEquals(-24.0F, settled.forwardTiltX(), EPSILON);
        assertEquals(-18.0F, settled.tiltZ(), EPSILON);
        assertEquals(0.0F, settled.orbitX(), EPSILON);
        assertEquals(0.0F, settled.orbitY(), EPSILON);
    }

    @Test
    void clockwiseOrbitStartsRightAndDown() {
        WandDrainAnimation.Transform pose =
                WandDrainAnimation.sample(
                        WandDrainAnimation.VERTICAL_HOLD_TICKS
                                + WandDrainAnimation.ENTRY_TICKS
                                + 6.0F,
                        HumanoidArm.RIGHT
                );

        assertTrue(pose.orbitX() > 0.0F);
        assertTrue(pose.orbitY() < 0.0F);
    }

    @Test
    void orbitIsSlowSmallAndClosed() {
        float start = WandDrainAnimation.VERTICAL_HOLD_TICKS
                + WandDrainAnimation.ENTRY_TICKS;
        WandDrainAnimation.Transform quarter =
                WandDrainAnimation.sample(
                        start
                                + WandDrainAnimation.ORBIT_PERIOD_TICKS
                                / 4.0F,
                        HumanoidArm.RIGHT
                );
        WandDrainAnimation.Transform complete =
                WandDrainAnimation.sample(
                        start + WandDrainAnimation.ORBIT_PERIOD_TICKS,
                        HumanoidArm.RIGHT
                );

        assertEquals(
                WandDrainAnimation.ORBIT_RADIUS,
                quarter.orbitX(),
                EPSILON
        );
        assertEquals(
                -WandDrainAnimation.ORBIT_RADIUS,
                quarter.orbitY(),
                EPSILON
        );
        assertEquals(0.0F, complete.orbitX(), EPSILON);
        assertEquals(0.0F, complete.orbitY(), EPSILON);
    }

    @Test
    void leftHandMirrorsOnlyTheSettledTilt() {
        WandDrainAnimation.Transform right =
                WandDrainAnimation.sample(60.0F, HumanoidArm.RIGHT);
        WandDrainAnimation.Transform left =
                WandDrainAnimation.sample(60.0F, HumanoidArm.LEFT);

        assertEquals(-right.tiltZ(), left.tiltZ(), EPSILON);
        assertEquals(right.orbitX(), left.orbitX(), EPSILON);
        assertEquals(right.orbitY(), left.orbitY(), EPSILON);
    }

    @Test
    void releasePlaysTheCapturedEntryPoseBackwards() {
        WandDrainAnimation.Transform releasePose =
                new WandDrainAnimation.Transform(
                        -24.0F,
                        -18.0F,
                        0.018F,
                        -0.009F
                );
        WandDrainAnimation.Transform halfway =
                WandDrainAnimation.sampleReturn(
                        WandDrainAnimation.RETURN_TICKS / 2.0F,
                        releasePose
                );
        WandDrainAnimation.Transform complete =
                WandDrainAnimation.sampleReturn(
                        WandDrainAnimation.RETURN_TICKS,
                        releasePose
                );

        assertEquals(-12.0F, halfway.forwardTiltX(), EPSILON);
        assertEquals(-9.0F, halfway.tiltZ(), EPSILON);
        assertEquals(0.009F, halfway.orbitX(), EPSILON);
        assertEquals(-0.0045F, halfway.orbitY(), EPSILON);
        assertEquals(0.0F, complete.forwardTiltX(), EPSILON);
        assertEquals(0.0F, complete.tiltZ(), EPSILON);
        assertEquals(0.0F, complete.orbitX(), EPSILON);
        assertEquals(0.0F, complete.orbitY(), EPSILON);
    }
}
