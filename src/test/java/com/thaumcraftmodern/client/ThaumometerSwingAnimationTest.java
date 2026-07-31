package com.thaumcraftmodern.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumometerSwingAnimationTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void animationStartsAndEndsAtRest() {
        ThaumometerSwingAnimation.Transform start =
                ThaumometerSwingAnimation.sample(0.0F, 1.0F);
        ThaumometerSwingAnimation.Transform end =
                ThaumometerSwingAnimation.sample(1.0F, 1.0F);

        assertAtRest(start);
        assertAtRest(end);
    }

    @Test
    void overlayAndHandsUseTheSameSwingDirectionAndPhase() {
        ThaumometerSwingAnimation.Transform right =
                ThaumometerSwingAnimation.sample(0.35F, 1.0F);
        ThaumometerSwingAnimation.Transform left =
                ThaumometerSwingAnimation.sample(0.35F, -1.0F);

        assertTrue(right.guiOffsetX() < 0.0F);
        assertTrue(right.handOffsetX() < 0.0F);
        assertTrue(left.guiOffsetX() > 0.0F);
        assertTrue(left.handOffsetX() > 0.0F);
        assertEquals(-right.guiOffsetX(), left.guiOffsetX(), EPSILON);
        assertEquals(-right.handOffsetX(), left.handOffsetX(), EPSILON);
        assertEquals(right.guiOffsetY(), left.guiOffsetY(), EPSILON);
        assertEquals(right.handOffsetY(), left.handOffsetY(), EPSILON);
    }

    @Test
    void ownerSideRespectsMainArmAndHeldHand() {
        assertEquals(
                1.0F,
                ThaumometerSwingAnimation.sideFor(
                        HumanoidArm.RIGHT,
                        InteractionHand.MAIN_HAND
                )
        );
        assertEquals(
                -1.0F,
                ThaumometerSwingAnimation.sideFor(
                        HumanoidArm.RIGHT,
                        InteractionHand.OFF_HAND
                )
        );
    }

    @Test
    void equipProgressMovesHandsAndInstrumentTogetherAtTheExistingScale() {
        ThaumometerSwingAnimation.Transform equipped =
                ThaumometerSwingAnimation.sample(0.0F, 1.0F, 1.0F);

        assertEquals(120.0F, equipped.guiOffsetY(), EPSILON);
        assertEquals(-1.2F, equipped.handOffsetY(), EPSILON);
        assertEquals(
                -equipped.guiOffsetY(),
                equipped.handOffsetY() * 100.0F,
                EPSILON
        );
    }

    private static void assertAtRest(ThaumometerSwingAnimation.Transform transform) {
        assertEquals(0.0F, transform.guiOffsetX(), EPSILON);
        assertEquals(0.0F, transform.guiOffsetY(), EPSILON);
        assertEquals(0.0F, transform.guiRotationDegrees(), EPSILON);
        assertEquals(0.0F, transform.handOffsetX(), EPSILON);
        assertEquals(0.0F, transform.handOffsetY(), EPSILON);
        assertEquals(0.0F, transform.handRotationDegrees(), EPSILON);
    }
}
