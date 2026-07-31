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

public final class TaintSporeModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "taint_spore"
            ),
            "main"
    );

    private final ModelPart cube;

    public TaintSporeModel(ModelPart root) {
        cube = root.getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F)
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F)
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
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        cube.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        cube.zRot = intensity * Mth.sin(ageInTicks * 0.1F);
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
        cube.render(
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
