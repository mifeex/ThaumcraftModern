package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class EldritchCrabModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "eldritch_crab"),
            "main"
    );

    private final ModelPart root;
    private final ModelPart tailHelm;
    private final ModelPart tailBare;
    private final ModelPart rightClawMid;
    private final ModelPart leftClawMid;
    private final ModelPart rightClawEnd;
    private final ModelPart leftClawEnd;
    private final ModelPart rightRearLegTop;
    private final ModelPart rightFrontLegTop;
    private final ModelPart leftRearLegTop;
    private final ModelPart leftFrontLegTop;
    private final ModelPart rightRearLegBase;
    private final ModelPart rightFrontLegBase;
    private final ModelPart leftRearLegBase;
    private final ModelPart leftFrontLegBase;

    public EldritchCrabModel(ModelPart root) {
        this.root = root;
        tailHelm = child("tail_helm");
        tailBare = child("tail_bare");
        rightClawMid = child("right_claw_mid");
        leftClawMid = child("left_claw_mid");
        rightClawEnd = child("right_claw_end");
        leftClawEnd = child("left_claw_end");
        rightRearLegTop = child("right_rear_leg_top");
        rightFrontLegTop = child("right_front_leg_top");
        leftRearLegTop = child("left_rear_leg_top");
        leftFrontLegTop = child("left_front_leg_top");
        rightRearLegBase = child("right_rear_leg_base");
        rightFrontLegBase = child("right_front_leg_base");
        leftRearLegBase = child("left_rear_leg_base");
        leftFrontLegBase = child("left_front_leg_base");
    }

    private ModelPart child(String name) {
        return root.getChild(name);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "tail_helm", 0, 0, -4.5F, -4.5F, -0.4F, 9, 9, 9,
                0, 18, 0, 0.1047198F, 0, 0, false);
        part(root, "tail_bare", 64, 0, -4, -4, -0.4F, 8, 8, 8,
                0, 18, 0, 0.1047198F, 0, 0, false);
        part(root, "right_claw_mid", 0, 47, -2, -1, -5.066667F,
                4, 3, 5, -6, 15.5F, -10, 0, 0, 0, false);
        part(root, "head_tip", 0, 38, -2, -1.5F, -9.066667F,
                4, 4, 1, 0, 18, 0, 0, 0, 0, false);
        part(root, "right_claw_base", 0, 55, -2, -2.5F, -3.066667F,
                4, 5, 3, -6, 17, -7, 0, 0, 0, false);
        part(root, "right_claw_end", 14, 54, -1.5F, -1, -4.066667F,
                3, 2, 5, -6, 18.5F, -10, 0.3141593F, 0, 0, false);
        part(root, "right_arm", 44, 4, -1, -1, -5.066667F,
                2, 2, 6, -3, 17, -4, 0, 0.7504916F, 0, false);
        part(root, "left_claw_end", 14, 54, -1.5F, -1, -4.066667F,
                3, 2, 5, 6, 18.5F, -10, 0.3141593F, 0, 0, false);
        part(root, "left_claw_mid", 0, 47, -2, -1, -5.066667F,
                4, 3, 5, 6, 15.5F, -10, 0, 0, 0, true);
        part(root, "left_claw_base", 0, 55, -2, -2.5F, -3.066667F,
                4, 5, 3, 6, 17, -7, 0, 0, 0, true);
        part(root, "left_arm", 44, 4, -1, -1, -4.066667F,
                2, 2, 6, 4, 17, -5, 0, -0.7504916F, 0, false);
        part(root, "torso", 0, 18, -3.5F, -3.5F, -6.066667F,
                7, 7, 6, 0, 18, 0, 0.0523599F, 0, 0, false);
        part(root, "head_base", 0, 31, -2.5F, -2, -8.066667F,
                5, 5, 2, 0, 18, 0, 0, 0, 0, false);
        leg(root, "right_rear_leg_top", -4.5F, 1, -0.9F,
                -4, 20, -1.5F);
        leg(root, "right_front_leg_top", -5, 1, -1.066667F,
                -4, 20, -3.5F);
        leg(root, "left_rear_leg_top", 2.5F, 1, -0.9F,
                4, 20, -1.5F);
        leg(root, "left_front_leg_top", 3, 1, -1.066667F,
                4, 20, -3.5F);
        base(root, "right_rear_leg_base", -4.5F, -0.9F,
                -4, 20, -1.5F);
        base(root, "right_front_leg_base", -5, -1.066667F,
                -4, 20, -3.5F);
        base(root, "left_front_leg_base", -1, -1.066667F,
                4, 20, -3.5F);
        base(root, "left_rear_leg_base", -1.5F, -0.9F,
                4, 20, -1.5F);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void leg(
            PartDefinition root, String name, float x, float y, float z,
            float px, float py, float pz
    ) {
        part(root, name, 36, 4, x, y, z, 2, 5, 2,
                px, py, pz, 0, 0, 0, false);
    }

    private static void base(
            PartDefinition root, String name, float x, float z,
            float px, float py, float pz
    ) {
        part(root, name, 36, 0, x, -1, z, 6, 2, 2,
                px, py, pz, 0, 0, 0, false);
    }

    private static void part(
            PartDefinition root, String name, int u, int v,
            float x, float y, float z, float w, float h, float d,
            float px, float py, float pz,
            float xr, float yr, float zr, boolean mirror
    ) {
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(u, v).mirror(mirror)
                        .addBox(x, y, z, w, h, d),
                PartPose.offsetAndRotation(px, py, pz, xr, yr, zr)
        );
    }

    @Override
    public void setupAnim(
            LegacyThaumcraftMob entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        tailHelm.visible = entity.hasCrabHelm();
        tailBare.visible = !tailHelm.visible;
        resetLeg(rightRearLegTop, 0.2094395F, 0.4363323F);
        resetLeg(rightFrontLegTop, -0.2094395F, 0.4363323F);
        resetLeg(leftRearLegTop, -0.2094395F, -0.4363323F);
        resetLeg(leftFrontLegTop, 0.2094395F, -0.4363323F);
        resetLeg(rightRearLegBase, 0.2094395F, 0.4363323F);
        resetLeg(rightFrontLegBase, -0.2094395F, 0.4363323F);
        resetLeg(leftFrontLegBase, 0.2094395F, -0.4363323F);
        resetLeg(leftRearLegBase, -0.2094395F, -0.4363323F);
        float rear = -Mth.cos(limbSwing * 1.3324F)
                * 0.4F * limbSwingAmount;
        float front = -Mth.cos(limbSwing * 1.3324F + Mth.PI)
                * 0.4F * limbSwingAmount;
        swing(rightRearLegTop, rear);
        swing(rightRearLegBase, rear);
        swing(leftRearLegTop, -rear);
        swing(leftRearLegBase, -rear);
        swing(rightFrontLegTop, front);
        swing(rightFrontLegBase, front);
        swing(leftFrontLegTop, -front);
        swing(leftFrontLegBase, -front);
        float tailYaw = Mth.cos(limbSwing * 0.6662F)
                * limbSwingAmount * 0.25F;
        float tailRoll = Mth.cos(limbSwing * 0.6662F)
                * limbSwingAmount * 0.125F;
        tailBare.yRot = tailHelm.yRot = tailYaw;
        tailBare.zRot = tailHelm.zRot = tailRoll;
        rightClawEnd.xRot = 0.3141593F - Mth.sin(ageInTicks / 4.0F) * 0.25F;
        leftClawEnd.xRot = 0.3141593F + Mth.sin(ageInTicks / 4.1F) * 0.25F;
        rightClawMid.xRot = Mth.sin(ageInTicks / 4.0F) * 0.125F;
        leftClawMid.xRot = -Mth.sin(ageInTicks / 4.1F) * 0.125F;
    }

    private static void resetLeg(ModelPart part, float yaw, float roll) {
        part.xRot = 0;
        part.yRot = yaw;
        part.zRot = roll;
    }

    private static void swing(ModelPart part, float amount) {
        part.yRot += amount;
        part.zRot += amount;
    }

    @Override
    public void renderToBuffer(
            PoseStack pose, VertexConsumer vertices, int light, int overlay,
            float red, float green, float blue, float alpha
    ) {
        root.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
