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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TaintSporeSwarmerModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "taint_spore_swarmer"
            ),
            "main"
    );
    private static final float DISPLAY_SIZE = 10.0F;

    private final ModelPart innerCube;
    private final ModelPart outerCube;
    private float ageInTicks;

    public TaintSporeSwarmerModel(ModelPart root) {
        innerCube = root.getChild("inner_cube");
        outerCube = root.getChild("outer_cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "inner_cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "outer_cube",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F)
        );
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
        this.ageInTicks = ageInTicks;
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        innerCube.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        innerCube.zRot = intensity * Mth.sin(ageInTicks * 0.1F);
    }

    @Override
    public void renderToBuffer(
            PoseStack pose,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float pulse = 0.025F * Mth.sin(ageInTicks * 0.075F);
        float scaleBase = -0.07F;
        float scaleXz = scaleBase * DISPLAY_SIZE - pulse;
        float scaleY = scaleBase * DISPLAY_SIZE + pulse;

        pose.pushPose();
        pose.translate(0.0F, 1.6F, 0.0F);
        pose.scale(scaleXz, scaleY, scaleXz);
        pose.translate(0.0F, -scaleY / 2.0F, 0.0F);
        innerCube.render(
                pose,
                vertices,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
        pose.popPose();

        outerCube.render(
                pose,
                vertices,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}
