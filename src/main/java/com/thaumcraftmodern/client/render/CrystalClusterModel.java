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

/** Exact geometry port of TC4 ModelCrystal. */
public final class CrystalClusterModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "crystal_cluster"),
            "main"
    );
    private final ModelPart crystal;

    public CrystalClusterModel(ModelPart root) {
        crystal = root.getChild("crystal");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
                "crystal",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(
                                -16.0F,
                                -16.0F,
                                0.0F,
                                16.0F,
                                16.0F,
                                16.0F
                        ),
                PartPose.offsetAndRotation(
                        0.0F,
                        32.0F,
                        0.0F,
                        0.7071F,
                        0.0F,
                        0.7071F
                )
        );
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void render(
            PoseStack poses,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue
    ) {
        crystal.render(
                poses,
                vertices,
                light,
                overlay,
                red,
                green,
                blue,
                1.0F
        );
    }
}
