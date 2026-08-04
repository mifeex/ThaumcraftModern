package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.FacelessWitnessEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/** A dedicated 48-pixel-tall, box-UV model; no HumanoidModel parts are reused. */
public final class FacelessWitnessModel
        extends EntityModel<FacelessWitnessEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "faceless_witness"),
            "main"
    );

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart hood;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightBoot;
    private final ModelPart leftBoot;
    private final ModelPart mantleCenter;
    private final ModelPart mantleLeft;
    private final ModelPart mantleRight;
    private final ModelPart mantleBack;
    private final ModelPart mantleSideLeft;
    private final ModelPart mantleSideRight;
    private final Limb upperLeft;
    private final Limb upperRight;
    private final Limb lower;

    public FacelessWitnessModel(ModelPart root) {
        this.root = root;
        torso = root.getChild("torso");
        hood = root.getChild("hood");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        rightBoot = rightLeg.getChild("boot");
        leftBoot = leftLeg.getChild("boot");
        mantleCenter = root.getChild("mantle_center");
        mantleLeft = root.getChild("mantle_left");
        mantleRight = root.getChild("mantle_right");
        mantleBack = root.getChild("mantle_back");
        mantleSideLeft = root.getChild("mantle_side_left");
        mantleSideRight = root.getChild("mantle_side_right");
        upperLeft = limb(root, "upper_left");
        upperRight = limb(root, "upper_right");
        lower = limb(root, "lower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "torso",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-4.5F, -13.0F, -2.75F, 9.0F, 17.0F, 5.5F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "chest_plate",
                CubeListBuilder.create().texOffs(72, 0)
                        .addBox(-5.0F, -12.0F, -3.55F, 10.0F, 11.0F, 1.25F),
                PartPose.ZERO
        );
        PartDefinition hood = root.addOrReplaceChild(
                "hood",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.5F, -11.5F, -5.0F, 11.0F, 12.0F, 10.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F)
        );
        hood.addOrReplaceChild(
                "hood_crown",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-3.5F, -3.0F, -3.0F, 7.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -11.5F, 1.0F)
        );
        hood.addOrReplaceChild(
                "void",
                CubeListBuilder.create().texOffs(32, 30)
                        .addBox(-4.1F, -9.8F, -5.35F, 8.2F, 9.2F, 0.5F),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "right_shoulder",
                CubeListBuilder.create().texOffs(72, 16)
                        .addBox(-7.5F, -13.0F, -3.75F, 3.5F, 6.0F, 7.5F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "left_shoulder",
                CubeListBuilder.create().texOffs(92, 16)
                        .addBox(4.0F, -13.0F, -3.75F, 3.5F, 6.0F, 7.5F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(0, 42)
                        .addBox(-2.5F, 0.0F, -1.5F, 3.0F, 38.0F, 3.0F),
                PartPose.offsetAndRotation(-6.0F, -10.5F, 0.0F, -0.08F, 0.0F, 0.13F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(16, 42)
                        .addBox(-0.5F, 0.0F, -1.5F, 3.0F, 38.0F, 3.0F),
                PartPose.offsetAndRotation(6.0F, -10.5F, 0.0F, -0.08F, 0.0F, -0.13F)
        );
        PartDefinition rightLeg = root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(32, 42)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-1.75F, 12.0F, 0.0F)
        );
        rightLeg.addOrReplaceChild(
                "boot",
                CubeListBuilder.create().texOffs(32, 64)
                        .addBox(-2.0F, 0.0F, -2.25F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F)
        );
        PartDefinition leftLeg = root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(48, 42)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(1.75F, 12.0F, 0.0F)
        );
        leftLeg.addOrReplaceChild(
                "boot",
                CubeListBuilder.create().texOffs(48, 64)
                        .addBox(-2.0F, 0.0F, -2.25F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F)
        );

        mantle(root, "mantle_center", 64, 34, -1.0F, 3.0F, -3.8F, 2, 17, 2);
        mantle(root, "mantle_left", 80, 34, 1.4F, 3.0F, -3.55F, 3, 14, 2);
        mantle(root, "mantle_right", 90, 34, -4.4F, 3.0F, -3.55F, 3, 18, 2);
        mantle(root, "mantle_back", 100, 34, -4.0F, 3.0F, 2.35F, 8, 17, 2);
        mantle(root, "mantle_side_left", 0, 84, 3.6F, 3.0F, -2.2F, 2, 16, 5);
        mantle(root, "mantle_side_right", 16, 84, -5.6F, 3.0F, -2.2F, 2, 19, 5);

        addLimb(root, "upper_left", 76, 76, -4.2F, -8.0F, 3.0F, true);
        addLimb(root, "upper_right", 92, 76, 4.2F, -8.0F, 3.0F, true);
        addLimb(root, "lower", 108, 76, 1.8F, 0.0F, 3.0F, false);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void mantle(
            PartDefinition root,
            String name,
            int u,
            int v,
            float x,
            float y,
            float z,
            int width,
            int height,
            int depth
    ) {
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(u, v)
                        .addBox(x, y, z, width, height, depth),
                PartPose.ZERO
        );
    }

    private static void addLimb(
            PartDefinition root,
            String name,
            int u,
            int v,
            float x,
            float y,
            float z,
            boolean eye
    ) {
        PartDefinition socket = root.addOrReplaceChild(
                name + "_socket",
                CubeListBuilder.create().texOffs(112, 0)
                        .addBox(-1.5F, -1.5F, -0.75F, 3.0F, 3.0F, 1.5F),
                PartPose.offset(x, y, z)
        );
        PartDefinition first = socket.addOrReplaceChild(
                "segment_1",
                CubeListBuilder.create().texOffs(u, v)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                PartPose.ZERO
        );
        PartDefinition second = first.addOrReplaceChild(
                "segment_2",
                CubeListBuilder.create().texOffs(u, v + 12)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F)
        );
        PartDefinition third = second.addOrReplaceChild(
                "segment_3",
                CubeListBuilder.create().texOffs(u, v + 24)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F)
        );
        third.addOrReplaceChild(
                "tip",
                CubeListBuilder.create().texOffs(eye ? 96 : 112, 108)
                        .addBox(-2.5F, 0.0F, -1.25F, 5.0F, eye ? 6.0F : 4.0F, 2.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F)
        );
        if (eye) {
            third.addOrReplaceChild(
                    "eye_plate",
                    CubeListBuilder.create().texOffs(96, 120)
                            .addBox(-1.75F, 1.0F, -1.55F, 3.5F, 4.0F, 0.5F),
                    PartPose.offset(0.0F, 8.0F, 0.0F)
            );
        }
    }

    private static Limb limb(ModelPart root, String name) {
        ModelPart socket = root.getChild(name + "_socket");
        ModelPart first = socket.getChild("segment_1");
        ModelPart second = first.getChild("segment_2");
        ModelPart third = second.getChild("segment_3");
        return new Limb(socket, first, second, third, third.getChild("tip"));
    }

    @Override
    public void setupAnim(
            FacelessWitnessEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float walk = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float slow = ageInTicks * 0.045F;
        float drift = Mth.sin(slow);
        float secondary = Mth.sin(slow * 1.37F + 1.2F);
        float breathe = Mth.sin(ageInTicks * 0.09F);

        root.y = breathe * 0.18F;
        hood.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.28F;
        hood.xRot = headPitch * Mth.DEG_TO_RAD * 0.18F;
        torso.zRot = drift * 0.012F;
        float rightStep = Mth.cos(limbSwing * 0.6662F) * walk;
        float leftStep = Mth.cos(limbSwing * 0.6662F + Mth.PI) * walk;
        rightLeg.xRot = rightStep * 0.42F;
        leftLeg.xRot = leftStep * 0.42F;
        rightBoot.xRot = Math.max(0.0F, -rightStep) * 0.48F;
        leftBoot.xRot = Math.max(0.0F, -leftStep) * 0.48F;
        rightBoot.zRot = drift * 0.018F;
        leftBoot.zRot = -drift * 0.018F;

        rightArm.xRot = -0.08F + secondary * 0.16F
                + Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.42F * walk;
        leftArm.xRot = -0.08F - secondary * 0.16F
                + Mth.cos(limbSwing * 0.6662F) * 0.42F * walk;
        rightArm.zRot = 0.13F + drift * 0.075F;
        leftArm.zRot = -0.13F - drift * 0.075F;
        mantleCenter.xRot = 0.025F + breathe * 0.025F;
        mantleLeft.xRot = 0.035F + secondary * 0.04F;
        mantleRight.xRot = 0.035F - secondary * 0.04F;
        mantleBack.xRot = -0.025F - breathe * 0.03F;
        mantleSideLeft.zRot = -0.025F + drift * 0.018F;
        mantleSideRight.zRot = 0.025F - drift * 0.018F;

        idleLimb(upperLeft, 2.18F, 0.18F, -0.10F, drift, secondary);
        idleLimb(upperRight, -2.18F, -0.18F, 0.10F, secondary, drift);
        idleLimb(lower, -0.68F, 0.24F, 0.16F, -secondary, drift);
        applyIdleGestures(ageInTicks, walk);

        float alert = pulse(entity.alertAnimation(ageInTicks - entity.tickCount));
        if (alert > 0.0F) {
            rightArm.zRot += 0.28F * alert;
            leftArm.zRot -= 0.28F * alert;
            upperLeft.socket.zRot -= 0.35F * alert;
            upperRight.socket.zRot += 0.35F * alert;
            lower.socket.xRot -= 0.5F * alert;
            hood.xRot -= 0.16F * alert;
        }

        float attackProgress = entity.attackAnimation(ageInTicks - entity.tickCount);
        applyAttack(entity.attackVariant(), attackProgress);

        if (entity.deathTime > 0) {
            float collapse = Mth.clamp(entity.deathTime / 20.0F, 0.0F, 1.0F);
            upperLeft.socket.xRot += 1.3F * collapse;
            upperRight.socket.xRot += 1.3F * collapse;
            lower.socket.xRot += 1.5F * collapse;
            rightArm.xRot += 0.8F * collapse;
            leftArm.xRot += 0.8F * collapse;
        }
    }

    private void applyIdleGestures(float ageInTicks, float walk) {
        if (walk > 0.12F) {
            return;
        }
        float phase = ageInTicks % 240.0F;
        float listen = window(phase, 45.0F, 88.0F);
        float reach = window(phase, 112.0F, 160.0F);
        float unfold = window(phase, 184.0F, 232.0F);

        // A slow one-sided listening pose, visible even when no target exists.
        hood.zRot += 0.09F * listen;
        leftArm.xRot -= 0.28F * listen;
        leftArm.zRot -= 0.08F * listen;
        upperRight.socket.xRot -= 0.26F * listen;
        upperRight.third.zRot -= 0.18F * listen;

        // The lower witness limb reaches around the body while the real arm recoils.
        lower.socket.xRot -= 0.46F * reach;
        lower.second.xRot += 0.34F * reach;
        lower.third.zRot += 0.26F * reach;
        rightArm.xRot += 0.16F * reach;
        torso.zRot -= 0.025F * reach;

        // Brief silhouette opening: both physical arms and upper appendages spread.
        rightArm.zRot += 0.12F * unfold;
        leftArm.zRot -= 0.12F * unfold;
        rightArm.xRot -= 0.16F * unfold;
        leftArm.xRot -= 0.16F * unfold;
        upperLeft.socket.zRot += 0.15F * unfold;
        upperRight.socket.zRot -= 0.15F * unfold;
        mantleLeft.zRot -= 0.035F * unfold;
        mantleRight.zRot += 0.035F * unfold;
        mantleSideLeft.zRot -= 0.025F * unfold;
        mantleSideRight.zRot += 0.025F * unfold;
    }

    private static float window(float phase, float start, float end) {
        if (phase <= start || phase >= end) {
            return 0.0F;
        }
        return Mth.sin((phase - start) / (end - start) * Mth.PI);
    }

    private void idleLimb(
            Limb limb,
            float baseZ,
            float baseX,
            float baseY,
            float waveA,
            float waveB
    ) {
        limb.socket.zRot = baseZ + waveA * 0.08F;
        limb.socket.xRot = baseX + waveB * 0.055F;
        limb.socket.yRot = baseY + waveA * 0.035F;
        limb.first.zRot = waveB * 0.07F;
        limb.first.yRot = -baseY * 0.35F;
        limb.second.zRot = (baseZ > 1.0F ? 0.26F : -0.26F)
                + waveA * 0.09F;
        limb.second.yRot = baseY * 0.42F;
        limb.third.zRot = (baseZ < -1.0F ? -0.18F : 0.18F)
                + waveB * 0.11F;
        limb.third.yRot = -baseY * 0.30F;
        limb.tip.zRot = waveA * 0.08F;
    }

    private void applyAttack(int variant, float progress) {
        if (progress <= 0.0F) {
            return;
        }
        float strike = Mth.sin(progress * Mth.PI);
        float snap = Mth.sin(Mth.clamp(progress * 1.35F, 0.0F, 1.0F) * Mth.PI);
        switch (variant) {
            case 0 -> {
                rightArm.xRot -= 2.05F * strike;
                leftArm.xRot -= 1.35F * strike;
                rightArm.zRot -= 0.55F * snap;
                leftArm.zRot += 0.35F * snap;
            }
            case 1 -> {
                upperLeft.socket.xRot -= 1.9F * strike;
                upperLeft.second.xRot -= 0.7F * snap;
                upperRight.socket.xRot -= 0.9F * strike;
                rightArm.xRot += 0.45F * strike;
            }
            case 2 -> {
                lower.socket.xRot -= 2.3F * strike;
                lower.first.xRot -= 0.5F * snap;
                lower.second.xRot += 0.85F * snap;
                torso.xRot -= 0.16F * strike;
            }
            case 3 -> {
                rightArm.xRot -= 1.25F * strike;
                leftArm.xRot -= 1.25F * strike;
                upperLeft.socket.xRot -= 1.35F * strike;
                upperRight.socket.xRot -= 1.35F * strike;
                lower.socket.xRot -= 1.75F * strike;
                upperLeft.third.zRot += 0.75F * snap;
                upperRight.third.zRot -= 0.75F * snap;
            }
            default -> {
                // Synced variants are constrained to 0-3 by the entity.
            }
        }
    }

    private static float pulse(float progress) {
        return progress <= 0.0F ? 0.0F : Mth.sin(progress * Mth.PI);
    }

    @Override
    public void renderToBuffer(
            PoseStack pose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        root.render(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private record Limb(
            ModelPart socket,
            ModelPart first,
            ModelPart second,
            ModelPart third,
            ModelPart tip
    ) {}
}
