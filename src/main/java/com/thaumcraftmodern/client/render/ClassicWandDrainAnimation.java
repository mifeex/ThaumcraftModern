package com.thaumcraftmodern.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Exact TC4 4.2.3.5 node-draining wand matrix values, retained as an
 * optional comparison mode.
 */
final class ClassicWandDrainAnimation {
    private static final float STARTUP_TICKS = 3.0F;
    private static final float STARTUP_ROTATION_X = -60.0F;
    private static final float FIRST_PERSON_ROTATION_X = 10.0F;
    private static final float FIRST_PERSON_ROTATION_Z = 10.0F;
    private static final float THIRD_PERSON_ROTATION_Z = 33.0F;
    private static final float WAVE_AMPLITUDE = 10.0F;

    private ClassicWandDrainAnimation() {
    }

    static Transform sample(
            float elapsedUseTicks,
            HumanoidArm arm,
            boolean firstPerson
    ) {
        float elapsed = Math.max(0.0F, elapsedUseTicks);
        float startupTicks = Math.min(elapsed, STARTUP_TICKS);
        float handSign = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;
        return new Transform(
                firstPerson ? FIRST_PERSON_ROTATION_X : 0.0F,
                handSign * (
                        firstPerson
                                ? FIRST_PERSON_ROTATION_Z
                                : THIRD_PERSON_ROTATION_Z
                ),
                STARTUP_ROTATION_X * startupTicks / STARTUP_TICKS,
                Mth.sin(elapsed / 10.0F)
                        * WAVE_AMPLITUDE
                        * handSign,
                Mth.sin(elapsed / 15.0F) * WAVE_AMPLITUDE
        );
    }

    record Transform(
            float contextRotationX,
            float contextRotationZ,
            float startupRotationX,
            float waveRotationZ,
            float waveRotationX
    ) {
    }
}
