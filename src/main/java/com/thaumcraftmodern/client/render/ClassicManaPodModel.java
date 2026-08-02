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

/** Exact box dimensions from TC4 ModelManaPod. */
public final class ClassicManaPodModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "mana_pod"),
            "main"
    );

    private final ModelPart pod0;
    private final ModelPart pod2;

    public ClassicManaPodModel(ModelPart root) {
        pod0 = root.getChild("pod0");
        pod2 = root.getChild("pod2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
                "pod0",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.ZERO
        );
        mesh.getRoot().addOrReplaceChild(
                "pod2",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void renderInner(
            PoseStack pose,
            VertexConsumer output,
            int light,
            int overlay
    ) {
        pod0.render(pose, output, light, overlay);
    }

    public void renderOuter(
            PoseStack pose,
            VertexConsumer output,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        pod2.render(
                pose,
                output,
                light,
                overlay,
                red,
                green,
                blue,
                alpha
        );
    }
}
