package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public final class PechModel extends EntityModel<LegacyThaumcraftMob>
        implements ArmedModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "pech"),
            "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jowls;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart lowerPack;

    public PechModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        head = root.getChild("head");
        jowls = root.getChild("jowls");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        lowerPack = root.getChild("lower_pack");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "body", 34, 12, -3, 0, 0, 6, 10, 6,
                0, 9, -3, 0.3129957F, 0, 0, false);
        part(root, "right_leg", 35, 1, -2.9F, 0, 0, 3, 6, 3,
                0, 18, 0, 0, 0, 0, true);
        part(root, "left_leg", 35, 1, -0.1F, 0, 0, 3, 6, 3,
                0, 18, 0, 0, 0, 0, false);
        part(root, "head", 2, 11, -3.5F, -5, -5, 7, 5, 5,
                0, 8, 0, 0, 0, 0, false);
        part(root, "jowls", 1, 21, -4, -1, -6, 8, 3, 5,
                0, 8, 0, 0, 0, 0, false);
        part(root, "lower_pack", 0, 0, -5, 0, 0, 10, 5, 5,
                0, 10, 3.5F, 0.3013602F, 0, 0, false);
        part(root, "upper_pack", 64, 1, -7.5F, -14, 0, 15, 14, 11,
                0, 10, 3, 0.4537856F, 0, 0, false);
        part(root, "right_arm", 52, 2, -2, 0, -1, 2, 6, 2,
                -3, 10, -1, 0, 0, 0, true);
        part(root, "left_arm", 52, 2, 0, 0, -1, 2, 6, 2,
                3, 10, -1, 0, 0, 0, false);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void part(
            PartDefinition root,
            String name,
            int u,
            int v,
            float x,
            float y,
            float z,
            float width,
            float height,
            float depth,
            float pivotX,
            float pivotY,
            float pivotZ,
            float xRot,
            float yRot,
            float zRot,
            boolean mirror
    ) {
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(u, v).mirror(mirror)
                        .addBox(x, y, z, width, height, depth),
                PartPose.offsetAndRotation(
                        pivotX, pivotY, pivotZ, xRot, yRot, zRot
                )
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
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jowls.yRot = head.yRot;
        jowls.xRot = head.xRot + 0.2617994F
                + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F
                + 0.34906587F * Mth.abs(Mth.sin(ageInTicks * 0.08F));
        rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI)
                * limbSwingAmount;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI)
                * 1.4F * limbSwingAmount;
        lowerPack.yRot = Mth.cos(limbSwing * 0.6662F)
                * limbSwingAmount * 0.25F;
        lowerPack.zRot = lowerPack.yRot;
        rightArm.zRot = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot = -rightArm.zRot;
        rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
        body.yRot = 0.0F;
    }

    @Override
    public void renderToBuffer(
            PoseStack pose,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        root.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        root.translateAndRotate(pose);
        ModelPart modelArm = arm == HumanoidArm.RIGHT ? rightArm : leftArm;
        modelArm.translateAndRotate(pose);
        pose.translate(
                arm == HumanoidArm.RIGHT ? -0.0625D : 0.0625D,
                0.3375D,
                0.0625D
        );
    }
}
