package com.thaumcraftmodern.client.render;

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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Faithful modern bake of Thaumcraft 4.2.3.5's ModelEldritchGuardian.
 *
 * <p>The classic texture is physically 256x128, but the original model uses a
 * logical 128x64 UV canvas. Minecraft samples those coordinates normally and
 * therefore retains the original texture's 2x detail.</p>
 */
public final class EldritchGuardianModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "eldritch_guardian"
            ),
            "main"
    );

    private final ModelPart root;
    private final ModelPart hood;
    private final ModelPart hoodEye;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart centerPanel1;
    private final ModelPart centerPanel2;
    private final ModelPart centerPanel3;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart sidePanelLeft2;
    private final ModelPart sidePanelLeft3;
    private final ModelPart sidePanelLeft4;
    private final ModelPart sidePanelRight2;
    private final ModelPart sidePanelRight3;
    private final ModelPart sidePanelRight4;
    private float renderAlpha = 1.0F;

    public EldritchGuardianModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        hood = root.getChild("hood");
        hoodEye = root.getChild("hood_eye");
        hoodEye.visible = false;
        armLeft = root.getChild("arm_left");
        armRight = root.getChild("arm_right");
        centerPanel1 = root.getChild("center_panel_1");
        centerPanel2 = centerPanel1.getChild("center_panel_2");
        centerPanel3 = centerPanel2.getChild("center_panel_3");
        cloak1 = root.getChild("cloak_1");
        cloak2 = cloak1.getChild("cloak_2");
        cloak3 = cloak2.getChild("cloak_3");
        sidePanelLeft2 = root.getChild("side_panel_left_2");
        sidePanelLeft3 = sidePanelLeft2.getChild("side_panel_left_3");
        sidePanelLeft4 = sidePanelLeft3.getChild("side_panel_left_4");
        sidePanelRight2 = root.getChild("side_panel_right_2");
        sidePanelRight3 =
                sidePanelRight2.getChild("side_panel_right_3");
        sidePanelRight4 =
                sidePanelRight3.getChild("side_panel_right_4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        part(root, "belt_right", 76, 44,
                -5.0F, 4.0F, -3.0F, 1, 3, 6,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "middle_belt", 56, 55,
                -4.0F, 8.0F, -3.0F, 8, 4, 1,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "middle_belt_left", 76, 44,
                4.0F, 8.0F, -3.0F, 1, 3, 6,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "middle_belt_right", 76, 44,
                -5.0F, 8.0F, -3.0F, 1, 3, 6,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "belt_left", 76, 44,
                4.0F, 4.0F, -3.0F, 1, 3, 6,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "chestplate", 56, 45,
                -4.0F, 1.0F, -4.0F, 8, 7, 2,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition hood = part(root, "hood", 40, 12,
                -4.0F, -8.0F, -4.0F, 8, 8, 8,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(root, "hood_eye", 0, 0,
                -4.0F, -8.0F, -4.0F, 8, 8, 8,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(hood, "hood_2", 36, 28,
                -3.5F, -8.7F, 2.0F, 7, 7, 3,
                0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F);
        part(hood, "hood_3", 22, 19,
                -3.0F, -9.0F, 2.5F, 6, 6, 3,
                0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F);
        part(hood, "hood_4", 40, 4,
                -2.5F, -9.7F, 3.5F, 5, 5, 3,
                0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F);
        part(root, "backplate", 36, 45,
                -4.0F, 1.0F, 2.0F, 8, 11, 2,
                0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        part(root, "shoulder_plate_right_top", 110, 37,
                -5.5F, -2.5F, -3.5F, 2, 1, 7,
                -5.0F, -4.0F, 0.0F,
                -0.3665191F, 0.3141593F, 0.4363323F);
        part(root, "shoulder_plate_right_1", 110, 45,
                3.5F, -1.5F, -3.5F, 1, 4, 7,
                5.0F, -4.0F, 0.0F,
                -0.3665191F, -0.3141593F, -0.4363323F);
        part(root, "shoulder_plate_right_2", 94, 45,
                -3.5F, 1.5F, -3.5F, 1, 3, 7,
                -5.0F, -4.0F, 0.0F,
                -0.3665191F, 0.3141593F, 0.4363323F);
        part(root, "shoulder_plate_right_3", 94, 45,
                -2.5F, 3.5F, -3.5F, 1, 3, 7,
                -5.0F, -4.0F, 0.0F,
                -0.3665191F, 0.3141593F, 0.4363323F);
        part(root, "shoulder_right", 56, 35,
                -3.5F, -2.5F, -2.5F, 5, 5, 5,
                -5.0F, -4.0F, 0.0F,
                -0.3665191F, 0.122173F, 0.0349066F);

        PartDefinition armLeft = part(root, "arm_left", 72, 8,
                -1.0F, 2.5F, -1.5F, 4, 10, 5,
                5.0F, -4.0F, 0.0F,
                -0.9599311F, -0.1047198F, -0.1919862F);
        part(armLeft, "arm_left_2", 76, 28,
                -1.0F, 9.5F, 3.5F, 4, 3, 3,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(armLeft, "arm_left_3", 76, 23,
                -1.0F, 6.5F, 3.5F, 4, 3, 2,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition armRight = part(root, "arm_right", 72, 8,
                -3.0F, 2.5F, -1.5F, 4, 10, 5,
                -5.0F, -4.0F, 0.0F,
                -0.9599311F, 0.1047198F, 0.1919862F);
        part(armRight, "arm_right_2", 76, 28,
                -3.0F, 9.5F, 3.5F, 4, 3, 3,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(armRight, "arm_right_3", 76, 23,
                -3.0F, 6.5F, 3.5F, 4, 3, 2,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        part(root, "shoulder_left", 56, 35,
                -1.5F, -2.5F, -2.5F, 5, 5, 5,
                5.0F, -4.0F, 0.0F,
                -0.3665191F, -0.122173F, -0.0349066F);
        part(root, "shoulder_plate_left_top", 110, 37,
                3.5F, -2.5F, -3.5F, 2, 1, 7,
                5.0F, -4.0F, 0.0F,
                -0.3665191F, -0.3141593F, -0.4363323F);
        part(root, "shoulder_plate_left_1", 110, 45,
                -4.5F, -1.5F, -3.5F, 1, 4, 7,
                -5.0F, -4.0F, 0.0F,
                -0.3665191F, 0.3141593F, 0.4363323F);
        part(root, "shoulder_plate_left_2", 94, 45,
                2.5F, 1.5F, -3.5F, 1, 3, 7,
                5.0F, -4.0F, 0.0F,
                -0.3665191F, -0.3141593F, -0.4363323F);
        part(root, "shoulder_plate_left_3", 94, 45,
                1.5F, 3.5F, -3.5F, 1, 3, 7,
                5.0F, -4.0F, 0.0F,
                -0.3665191F, -0.3141593F, -0.4363323F);

        part(root, "leg_panel_right_4", 0, 43,
                -3.0F, 0.5F, -3.5F, 2, 3, 1,
                -2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "leg_panel_right_5", 0, 47,
                -3.0F, 2.5F, -2.5F, 2, 3, 1,
                -2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "leg_panel_right_6", 6, 43,
                -3.0F, 4.5F, -1.5F, 2, 3, 1,
                -2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_right_1", 0, 18,
                -3.0F, 0.5F, 2.5F, 5, 3, 1,
                -2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_right_2", 0, 18,
                -3.0F, 2.5F, 1.5F, 5, 3, 1,
                -2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_right_3", 0, 18,
                -3.0F, 4.5F, 0.5F, 5, 3, 1,
                -2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_left_3", 0, 18,
                -2.0F, 4.5F, 0.5F, 5, 3, 1,
                2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "leg_panel_left_4", 0, 43,
                1.0F, 0.5F, -3.5F, 2, 3, 1,
                2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "leg_panel_left_5", 0, 47,
                1.0F, 2.5F, -2.5F, 2, 3, 1,
                2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "leg_panel_left_6", 6, 43,
                1.0F, 4.5F, -1.5F, 2, 3, 1,
                2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_left_1", 0, 18,
                -2.0F, 0.5F, 2.5F, 5, 3, 1,
                2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "back_panel_left_2", 0, 18,
                -2.0F, 2.5F, 1.5F, 5, 3, 1,
                2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F);
        part(root, "side_panel_left_1", 0, 22,
                1.5F, 0.5F, -2.5F, 1, 4, 5,
                2.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.4363323F);
        part(root, "side_panel_right_1", 0, 22,
                -2.5F, 0.5F, -2.5F, 1, 4, 5,
                -2.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.4363323F);

        PartDefinition sideRight2 = part(root, "side_panel_right_2", 0, 54,
                0.0F, 0.0F, -0.5F, 1, 5, 5,
                -4.5F, 9.5F, -2.0F, 0.0F, 0.0F, 0.122173F);
        PartDefinition sideRight3 = part(
                sideRight2, "side_panel_right_3", 0, 35,
                0.0F, 0.0F, -0.5F, 1, 3, 5,
                0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.296706F);
        part(sideRight3, "side_panel_right_4", 24, 35,
                0.0F, 0.0F, -0.5F, 1, 3, 5,
                0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.5235988F);

        PartDefinition sideLeft2 = part(root, "side_panel_left_2", 0, 54,
                0.0F, 0.0F, -0.5F, 1, 5, 5,
                4.5F, 9.5F, -2.0F, 0.0F, 0.0F, -0.122173F);
        PartDefinition sideLeft3 = part(
                sideLeft2, "side_panel_left_3", 0, 35,
                0.0F, 0.0F, -0.5F, 1, 3, 5,
                0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.296706F);
        part(sideLeft3, "side_panel_left_4", 24, 35,
                0.0F, 0.0F, -0.5F, 1, 3, 5,
                0.0F, 3.0F, 0.0F, 0.0F, 0.0F, -0.5235988F);

        PartDefinition center1 = part(root, "center_panel_1", 16, 45,
                -3.0F, 0.0F, -0.5F, 6, 8, 1,
                0.0F, 5.5F, -3.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition center2 = part(center1, "center_panel_2", 16, 54,
                -3.0F, 0.0F, -0.5F, 6, 4, 1,
                0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(center2, "center_panel_3", 32, 59,
                -3.0F, 0.0F, -0.5F, 6, 4, 1,
                0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        PartDefinition cloak1 = part(root, "cloak_1", 106, 0,
                0.0F, 0.0F, -0.5F, 10, 18, 1,
                -5.0F, -6.0F, 4.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition cloak2 = part(cloak1, "cloak_2", 106, 19,
                0.0F, 0.0F, -0.5F, 10, 4, 1,
                0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        part(cloak2, "cloak_3", 106, 24,
                0.0F, 0.0F, -0.5F, 10, 4, 1,
                0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        return LayerDefinition.create(mesh, 128, 64);
    }

    private static PartDefinition part(
            PartDefinition parent,
            String name,
            int textureX,
            int textureY,
            float cubeX,
            float cubeY,
            float cubeZ,
            float width,
            float height,
            float depth,
            float pivotX,
            float pivotY,
            float pivotZ,
            float rotationX,
            float rotationY,
            float rotationZ
    ) {
        return parent.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(textureX, textureY)
                        .mirror()
                        .addBox(
                                cubeX,
                                cubeY,
                                cubeZ,
                                width,
                                height,
                                depth
                        ),
                PartPose.offsetAndRotation(
                        pivotX,
                        pivotY,
                        pivotZ,
                        rotationX,
                        rotationY,
                        rotationZ
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
        hood.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        hood.xRot = headPitch * Mth.DEG_TO_RAD;
        hoodEye.yRot = hood.yRot;
        hoodEye.xRot = hood.xRot;

        // The original guardian does not walk-swing its arms. They hover
        // forward and move out of phase while the robe panels ripple.
        armLeft.xRot = -1.0F
                + Mth.sin((ageInTicks + 20.0F) / 10.0F) * 0.08F;
        armRight.xRot = -1.0F
                + Mth.sin(ageInTicks / 10.0F) * 0.08F;

        centerPanel1.xRot = -0.15F
                + Mth.sin(ageInTicks / 8.0F) * 0.12F;
        centerPanel2.xRot =
                Mth.sin((ageInTicks - 5.0F) / 8.0F) * 0.13F;
        centerPanel3.xRot =
                Mth.sin((ageInTicks - 10.0F) / 8.0F) * 0.14F;

        cloak1.xRot = 0.2F + Mth.sin(ageInTicks / 7.0F) * 0.08F;
        cloak2.xRot = Mth.sin((ageInTicks - 5.0F) / 7.0F) * 0.1F;
        cloak3.xRot = Mth.sin((ageInTicks - 10.0F) / 7.0F) * 0.12F;

        sidePanelLeft2.zRot = -0.2F
                + Mth.sin((ageInTicks + 10.0F) / 8.0F) * 0.12F;
        sidePanelLeft3.zRot =
                Mth.sin((ageInTicks + 5.0F) / 8.0F) * 0.13F;
        sidePanelLeft4.zRot = Mth.sin(ageInTicks / 8.0F) * 0.14F;
        sidePanelRight2.zRot = 0.2F
                + Mth.sin((ageInTicks - 5.0F) / 8.0F) * 0.12F;
        sidePanelRight3.zRot =
                Mth.sin((ageInTicks - 10.0F) / 8.0F) * 0.13F;
        sidePanelRight4.zRot =
                Mth.sin((ageInTicks - 15.0F) / 8.0F) * 0.14F;
    }

    @Override
    public void renderToBuffer(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        root.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha * renderAlpha
        );
    }

    void setRenderAlpha(float renderAlpha) {
        this.renderAlpha = renderAlpha;
    }

    void renderWardenEyes(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay
    ) {
        hoodEye.visible = true;
        try {
            hoodEye.render(
                    poseStack,
                    vertexConsumer,
                    packedLight,
                    packedOverlay
            );
        } finally {
            hoodEye.visible = false;
        }
    }
}
