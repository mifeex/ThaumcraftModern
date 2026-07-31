package com.thaumcraftmodern.client;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Shared first-person swing transform for the Thaumometer overlay and hands.
 *
 * <p>Both render paths sample the same curve so the physical hands and the
 * two-dimensional instrument face cannot drift out of phase during an attack.</p>
 */
public final class ThaumometerSwingAnimation {
    private static final float GUI_HORIZONTAL_DISTANCE = 18.0F;
    private static final float GUI_REBOUND_DISTANCE = 5.0F;
    private static final float GUI_DROP_DISTANCE = 10.0F;
    private static final float GUI_EQUIP_DROP_DISTANCE = 120.0F;
    private static final float GUI_ROTATION_DEGREES = 8.0F;

    private static final float HAND_HORIZONTAL_DISTANCE = 0.18F;
    private static final float HAND_REBOUND_DISTANCE = 0.05F;
    private static final float HAND_DROP_DISTANCE = 0.10F;
    private static final float HAND_EQUIP_DROP_DISTANCE = 1.2F;
    private static final float HAND_ROTATION_DEGREES = 8.0F;

    private ThaumometerSwingAnimation() {
    }

    public static Transform sample(float swingProgress, float handSide) {
        return sample(swingProgress, handSide, 0.0F);
    }

    public static Transform sample(
            float swingProgress,
            float handSide,
            float equipProgress
    ) {
        float progress = Mth.clamp(swingProgress, 0.0F, 1.0F);
        float equip = Mth.clamp(equipProgress, 0.0F, 1.0F);
        float side = handSide < 0.0F ? -1.0F : 1.0F;
        float root = Mth.sqrt(progress);
        float sweep = Mth.sin(root * (float) Math.PI);
        float rebound = Mth.sin(root * (float) Math.PI * 2.0F);
        float drop = Mth.sin(progress * (float) Math.PI);

        return new Transform(
                -side * sweep * GUI_HORIZONTAL_DISTANCE,
                rebound * GUI_REBOUND_DISTANCE
                        + drop * GUI_DROP_DISTANCE
                        + equip * GUI_EQUIP_DROP_DISTANCE,
                -side * sweep * GUI_ROTATION_DEGREES,
                -side * sweep * HAND_HORIZONTAL_DISTANCE,
                -(rebound * HAND_REBOUND_DISTANCE
                        + drop * HAND_DROP_DISTANCE
                        + equip * HAND_EQUIP_DROP_DISTANCE),
                side * sweep * HAND_ROTATION_DEGREES
        );
    }

    public static float sideFor(HumanoidArm mainArm, InteractionHand hand) {
        HumanoidArm ownerArm = hand == InteractionHand.MAIN_HAND
                ? mainArm
                : mainArm.getOpposite();
        return ownerArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    }

    public record Transform(
            float guiOffsetX,
            float guiOffsetY,
            float guiRotationDegrees,
            float handOffsetX,
            float handOffsetY,
            float handRotationDegrees
    ) {
    }
}
