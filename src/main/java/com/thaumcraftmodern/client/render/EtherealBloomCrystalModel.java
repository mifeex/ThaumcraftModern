package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Exact modern geometry equivalent of TC4's no-argument ModelCube.
 */
public final class EtherealBloomCrystalModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "ethereal_bloom_crystal"
            ),
            "main"
    );

    private final ModelPart cube;

    public EtherealBloomCrystalModel(ModelPart root) {
        cube = root.getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(
                                -8.0F,
                                -8.0F,
                                -8.0F,
                                16.0F,
                                16.0F,
                                16.0F
                        ),
                PartPose.offset(8.0F, 8.0F, 8.0F)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void render(
            PoseStack pose,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay
    ) {
        cube.render(
                pose,
                vertices,
                packedLight,
                packedOverlay
        );
    }
}
