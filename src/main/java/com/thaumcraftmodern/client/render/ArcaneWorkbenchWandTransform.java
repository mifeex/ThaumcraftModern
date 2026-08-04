package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.wand.WandForm;

/** TC4 casting-tool transform, centred on the modern workbench grid. */
public final class ArcaneWorkbenchWandTransform {
    public static final double X = 0.7032931995911324D;
    public static final double Y = 1.0625D;
    public static final double Z = 0.2967068004088675D;
    public static final float X_ROTATION = 90.0F;
    public static final float Z_ROTATION = 45.0F;
    public static final double TOOL_Y_OFFSET = 0.6D;
    public static final double STAFF_Y_OFFSET = 1.0D;
    public static final float TOOL_SCALE = 0.5F;
    public static final float STAFF_SCALE = 0.45F;
    static final double TOOL_CENTER_FROM_PIVOT = 0.2875D;

    private ArcaneWorkbenchWandTransform() {
    }

    public static void apply(PoseStack poseStack, WandForm form) {
        boolean staff = form == WandForm.STAFF;
        poseStack.translate(X, Y, Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(X_ROTATION));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Z_ROTATION));
        poseStack.translate(
                0.0D,
                staff ? STAFF_Y_OFFSET : TOOL_Y_OFFSET,
                0.0D
        );
        float scale = staff ? STAFF_SCALE : TOOL_SCALE;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }
}
