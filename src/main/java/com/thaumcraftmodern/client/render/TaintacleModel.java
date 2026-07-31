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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern bake of TC4 4.2.3.5's ModelTaintacle.
 *
 * <p>The classic model is a recursive chain of cubes. Every child is scaled
 * by 0.88, producing the characteristic taper and curved silhouette instead
 * of a humanoid body.</p>
 */
public final class TaintacleModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final int NORMAL_LENGTH = 10;
    public static final int TENDRIL_LENGTH = 6;
    public static final int GIANT_LENGTH = 14;
    private static final float CHILD_SCALE = 0.88F;
    private static final float CHILD_OFFSET = -8.0F * CHILD_SCALE;

    public static final ModelLayerLocation NORMAL_LAYER = layer("taintacle");
    public static final ModelLayerLocation TENDRIL_LAYER =
            layer("taint_tendril");
    public static final ModelLayerLocation GIANT_LAYER =
            layer("giant_taintacle");

    private final ModelPart base;
    private final List<ModelPart> animatedSegments;
    private final List<ModelPart> nonGlowingBody;

    public TaintacleModel(ModelPart bakedRoot, int length) {
        super(RenderType::entityTranslucent);
        base = bakedRoot.getChild("base");
        animatedSegments = new ArrayList<>(length - 1);
        nonGlowingBody = new ArrayList<>(length);
        nonGlowingBody.add(base);

        ModelPart current = base;
        for (int index = 0; index < length - 1; index++) {
            current = current.getChild(segmentName(index));
            applyClassicChildScale(current);
            animatedSegments.add(current);
            nonGlowingBody.add(current);
        }
        applyClassicChildScale(current.getChild("orb"));
        applyClassicChildScale(current.getChild("terminal_bulb"));
    }

    public static LayerDefinition createNormalLayer() {
        return createBodyLayer(NORMAL_LENGTH);
    }

    public static LayerDefinition createTendrilLayer() {
        return createBodyLayer(TENDRIL_LENGTH);
    }

    public static LayerDefinition createGiantLayer() {
        return createBodyLayer(GIANT_LENGTH);
    }

    private static LayerDefinition createBodyLayer(int length) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition current = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        for (int index = 0; index < length - 1; index++) {
            current = current.addOrReplaceChild(
                    segmentName(index),
                    CubeListBuilder.create()
                            .texOffs(0, 16)
                            .addBox(
                                    -4.0F,
                                    -4.0F,
                                    -4.0F,
                                    8.0F,
                                    8.0F,
                                    8.0F
                            ),
                    PartPose.offset(0.0F, CHILD_OFFSET, 0.0F)
            );
        }
        current.addOrReplaceChild(
                "orb",
                CubeListBuilder.create()
                        .texOffs(0, 56)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, CHILD_OFFSET, 0.0F)
        );
        current.addOrReplaceChild(
                "terminal_bulb",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(
                                -6.0F,
                                -6.0F,
                                -6.0F,
                                12.0F,
                                12.0F,
                                12.0F
                        ),
                PartPose.offset(0.0F, CHILD_OFFSET, 0.0F)
        );
        // TC4 uses a logical 64x64 canvas for the exact 128x128 texture.
        return LayerDefinition.create(mesh, 64, 64);
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
        boolean agitated = entity.hurtTime > 0
                || entity.getAttackAnim(0.0F) > 0.0F
                || entity.getTarget() != null;
        float flailIntensity = agitated ? 3.0F : 1.0F;
        float flailSpeed = agitated ? 3.0F : 1.0F;
        for (int index = 0; index < animatedSegments.size(); index++) {
            ModelPart segment = animatedSegments.get(index);
            segment.xRot = 0.15F
                    * flailIntensity
                    * Mth.sin(ageInTicks * 0.1F * flailSpeed
                    - index / 2.0F);
            segment.zRot = 0.1F
                    / flailIntensity
                    * Mth.sin(ageInTicks * 0.15F - index / 2.0F);
        }
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        base.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }

    /**
     * TC4 forces the two leaf cubes to full brightness. Parent cubes still
     * traverse their children but skip their own draw for this emissive pass.
     */
    void renderGlowingTip(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay
    ) {
        nonGlowingBody.forEach(part -> part.skipDraw = true);
        try {
            base.render(
                    poseStack,
                    vertexConsumer,
                    packedLight,
                    packedOverlay
            );
        } finally {
            nonGlowingBody.forEach(part -> part.skipDraw = false);
        }
    }

    private static void applyClassicChildScale(ModelPart part) {
        part.xScale = CHILD_SCALE;
        part.yScale = CHILD_SCALE;
        part.zScale = CHILD_SCALE;
    }

    private static String segmentName(int index) {
        return "segment_" + index;
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(
                new ResourceLocation(ThaumcraftModern.MOD_ID, path),
                "main"
        );
    }
}
