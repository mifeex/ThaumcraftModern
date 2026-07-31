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

/**
 * Direct geometry port of TC4 ModelEldritchGolem (128x64 atlas).
 */
public final class EldritchConstructModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "eldritch_construct"
            ),
            "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart core;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart frontCloth1;
    private final ModelPart frontCloth2;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public EldritchConstructModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        core = root.getChild("core");
        armLeft = root.getChild("arm_left");
        armRight = root.getChild("arm_right");
        legLeft = root.getChild("leg_left");
        legRight = root.getChild("leg_right");
        frontCloth1 = root.getChild("front_cloth_1");
        frontCloth2 = root.getChild("front_cloth_2");
        cloak1 = root.getChild("cloak_1");
        cloak2 = root.getChild("cloak_2");
        cloak3 = root.getChild("cloak_3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "cloak_1", 0, 47, -5, 1.5F, 4, 10, 12, 1,
                0, 0, -2.5F, 0.1396263F, 0, 0, false);
        part(root, "cloak_2", 0, 59, -5, 13.5F, 1.7F, 10, 4, 1,
                0, 0, -2.5F, 0.3069452F, 0, 0, false);
        part(root, "cloak_3", 0, 37, -5, 17.5F, -0.8F, 10, 4, 1,
                0, 0, -2.5F, 0.4465716F, 0, 0, false);
        part(root, "cloak_collar_left", 0, 43, 3, 0.5F, 2, 2, 1, 3,
                0, 0, -2.5F, 0.1396263F, 0, 0, false);
        part(root, "cloak_collar_right", 0, 43, -5, 0.5F, 2, 2, 1, 3,
                0, 0, -2.5F, 0.1396263F, 0, 0, false);
        part(root, "head", 47, 12, -3.5F, -6, -2.5F, 7, 7, 5,
                0, 4.5F, -3.8F, -0.1047198F, 0, 0, false);
        part(root, "core", 26, 16, -2, -2, -2, 4, 4, 4,
                0, 0, -5, -0.1047198F, 0, 0, false);
        part(root, "collar_left", 75, 50, 3.5F, -0.5F, -7, 1, 4, 10,
                0, 0, -2.5F, 0.837758F, 0, 0, false);
        part(root, "collar_right", 67, 50, -4.5F, -0.5F, -7, 1, 4, 10,
                0, 0, -2.5F, 0.837758F, 0, 0, false);
        part(root, "collar_back", 77, 59, -3.5F, -0.5F, 2, 7, 4, 1,
                0, 0, -2.5F, 0.837758F, 0, 0, false);
        part(root, "collar_front", 77, 59, -3.5F, -0.5F, -7, 7, 4, 1,
                0, 0, -2.5F, 0.837758F, 0, 0, false);
        part(root, "collar_black", 22, 0, -3.5F, 0, -6, 7, 1, 8,
                0, 0, -2.5F, 0.837758F, 0, 0, false);
        part(root, "front_cloth_0", 114, 52, -3, 3.2F, -3.5F,
                6, 10, 1, 0, 0, -2.5F, 0.1745329F, 0, 0, false);
        part(root, "front_cloth_1", 114, 39, -1, 1.5F, -3.5F,
                6, 6, 1, -2, 12, 0, -0.1047198F, 0, 0, false);
        part(root, "front_cloth_2", 114, 47, -1, 8.5F, -1.5F,
                6, 3, 1, -2, 11, 0, -0.3316126F, 0, 0, false);
        part(root, "torso", 34, 45, -5, 2.5F, -3, 10, 10, 6,
                0, 0, -2.5F, 0.1745329F, 0, 0, true);

        PartDefinition rightArm = part(root, "arm_right", 78, 32,
                -3.5F, 1.5F, -2, 4, 13, 5,
                -5, 3, -2, 0, 0, 0.1047198F, false);
        shoulder(rightArm, "shoulder_right_main", 0, 0,
                -4.3F, -1, -3, 4, 5, 7, 1.186824F, false);
        shoulder(rightArm, "shoulder_right_inner", 56, 31,
                -4.5F, -1.5F, -2.5F, 5, 6, 6, 0, false);
        shoulder(rightArm, "shoulder_right_ridge", 0, 23,
                -3.3F, 4, -2.5F, 1, 2, 6, 1.186824F, false);
        shoulder(rightArm, "shoulder_right_tip", 0, 12,
                -2.3F, 4, -3, 2, 3, 7, 1.186824F, false);

        PartDefinition leftArm = part(root, "arm_left", 78, 32,
                -0.5F, 1.5F, -2, 4, 13, 5,
                5, 3, -2, 0, 0, -0.1047198F, true);
        shoulder(leftArm, "shoulder_left_main", 0, 0,
                0.3F, -1, -3, 4, 5, 7, -1.186824F, true);
        shoulder(leftArm, "shoulder_left_inner", 56, 31,
                -0.5F, -1.5F, -2.5F, 5, 6, 6, 0, true);
        shoulder(leftArm, "shoulder_left_ridge", 0, 23,
                2.3F, 4, -2.5F, 1, 2, 6, -1.186824F, true);
        shoulder(leftArm, "shoulder_left_tip", 0, 12,
                0.3F, 4, -3, 2, 3, 7, -1.186824F, true);

        part(root, "back_panel_right", 96, 7, 0, 2.5F, -2.5F,
                2, 2, 5, -2, 12, 0, 0, 0, 0.1396263F, false);
        part(root, "waist_right_1", 96, 14, -3, -0.5F, -2.5F,
                5, 3, 5, -2, 12, 0, 0, 0, 0.1396263F, false);
        part(root, "waist_right_2", 116, 13, -3, 2.5F, -2.5F,
                1, 4, 5, -2, 12, 0, 0, 0, 0.1396263F, false);
        part(root, "waist_right_3", 114, 5, -2, 2.5F, -2.5F,
                2, 3, 5, -2, 12, 0, 0, 0, 0.1396263F, true);
        part(root, "leg_right", 79, 19, -2.5F, 2.5F, -2,
                4, 9, 4, -2, 12.5F, 0, 0, 0, 0, false);
        part(root, "back_panel_left", 96, 7, -2, 2.5F, -2.5F,
                2, 2, 5, 2, 12, 0, 0, 0, -0.1396263F, true);
        part(root, "waist_left_1", 96, 14, -2, -0.5F, -2.5F,
                5, 3, 5, 2, 12, 0, 0, 0, -0.1396263F, true);
        part(root, "waist_left_2", 116, 13, 2, 2.5F, -2.5F,
                1, 4, 5, 2, 12, 0, 0, 0, -0.1396263F, true);
        part(root, "waist_left_3", 114, 5, 0, 2.5F, -2.5F,
                2, 3, 5, 2, 12, 0, 0, 0, -0.1396263F, true);
        part(root, "leg_left", 79, 19, -1.5F, 2.5F, -2,
                4, 9, 4, 2, 12.5F, 0, 0, 0, 0, true);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void shoulder(
            PartDefinition parent, String name, int u, int v,
            float x, float y, float z, float w, float h, float d,
            float zRot, boolean mirror
    ) {
        part(parent, name, u, v, x, y, z, w, h, d,
                0, 0, 0, 0, 0, zRot, mirror);
    }

    private static PartDefinition part(
            PartDefinition parent, String name, int u, int v,
            float x, float y, float z, float w, float h, float d,
            float px, float py, float pz,
            float xr, float yr, float zr, boolean mirror
    ) {
        return parent.addOrReplaceChild(
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
        boolean headless = entity.isConstructHeadless();
        head.visible = !headless;
        core.visible = headless;
        if (entity.constructRecoveryTimer() > 0) {
            head.yRot = 0.0F;
            head.xRot = (entity.constructRecoveryTimer() / 2)
                    / 57.295776F;
        } else {
            head.yRot = netHeadYaw * 0.25F * Mth.DEG_TO_RAD;
            head.xRot = headPitch * 0.5F * Mth.DEG_TO_RAD;
            core.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            core.xRot = headPitch * Mth.DEG_TO_RAD;
        }
        legRight.xRot = Mth.cos(limbSwing * 0.4662F)
                * 1.4F * limbSwingAmount;
        legLeft.xRot = Mth.cos(limbSwing * 0.4662F + Mth.PI)
                * 1.4F * limbSwingAmount;
        int attackTimer = entity.constructAttackTimer();
        if (attackTimer > 0) {
            float partialTick = ageInTicks - entity.tickCount;
            float attackCurve = triangleWave(
                    attackTimer - partialTick,
                    10.0F
            );
            armRight.xRot = -2.0F + 1.5F * attackCurve;
            armLeft.xRot = -2.0F + 1.5F * attackCurve;
        } else {
            armRight.xRot = Mth.cos(limbSwing * 0.4F + Mth.PI)
                    * limbSwingAmount;
            armLeft.xRot = Mth.cos(limbSwing * 0.4F)
                    * limbSwingAmount;
        }
        float a = Mth.cos(limbSwing * 0.44F) * 1.4F * limbSwingAmount;
        float b = Mth.cos(limbSwing * 0.44F + Mth.PI)
                * 1.4F * limbSwingAmount;
        float cloth = Math.min(a, b);
        frontCloth1.xRot = cloth - 0.1047198F;
        frontCloth2.xRot = cloth - 0.3316126F;
        cloak1.xRot = -cloth / 3.0F + 0.1396263F;
        cloak2.xRot = -cloth / 3.0F + 0.3069452F;
        cloak3.xRot = -cloth / 3.0F + 0.4465716F;
    }

    private static float triangleWave(float value, float period) {
        return (Math.abs(value % period - period * 0.5F)
                - period * 0.25F) / (period * 0.25F);
    }

    @Override
    public void renderToBuffer(
            PoseStack pose, VertexConsumer vertices, int light, int overlay,
            float red, float green, float blue, float alpha
    ) {
        root.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
