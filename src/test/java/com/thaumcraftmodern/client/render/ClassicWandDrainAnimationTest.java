package com.thaumcraftmodern.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClassicWandDrainAnimationTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void preservesOriginalThreeTickStartupAndBothWaves() {
        ClassicWandDrainAnimation.Transform pose =
                ClassicWandDrainAnimation.sample(
                        3.0F,
                        HumanoidArm.RIGHT,
                        true
                );

        assertEquals(10.0F, pose.contextRotationX(), EPSILON);
        assertEquals(10.0F, pose.contextRotationZ(), EPSILON);
        assertEquals(-60.0F, pose.startupRotationX(), EPSILON);
        assertEquals(
                Mth.sin(3.0F / 10.0F) * 10.0F,
                pose.waveRotationZ(),
                EPSILON
        );
        assertEquals(
                Mth.sin(3.0F / 15.0F) * 10.0F,
                pose.waveRotationX(),
                EPSILON
        );
    }

    @Test
    void preservesThirdPersonContextAndMirrorsLegacyZForLeftHand() {
        ClassicWandDrainAnimation.Transform right =
                ClassicWandDrainAnimation.sample(
                        24.0F,
                        HumanoidArm.RIGHT,
                        false
                );
        ClassicWandDrainAnimation.Transform left =
                ClassicWandDrainAnimation.sample(
                        24.0F,
                        HumanoidArm.LEFT,
                        false
                );

        assertEquals(33.0F, right.contextRotationZ(), EPSILON);
        assertEquals(-33.0F, left.contextRotationZ(), EPSILON);
        assertEquals(
                -right.waveRotationZ(),
                left.waveRotationZ(),
                EPSILON
        );
        assertEquals(
                right.waveRotationX(),
                left.waveRotationX(),
                EPSILON
        );
    }
}
