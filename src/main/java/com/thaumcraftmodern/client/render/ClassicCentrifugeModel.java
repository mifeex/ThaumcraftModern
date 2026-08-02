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

public final class ClassicCentrifugeModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "essentia_centrifuge"), "main");
    private final ModelPart boxes;
    private final ModelPart spin;
    public ClassicCentrifugeModel(ModelPart root) { boxes = root.getChild("boxes"); spin = root.getChild("spin"); }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("boxes", CubeListBuilder.create().texOffs(20, 16)
                .addBox(-4, -8, -4, 8, 4, 8)
                .addBox(-4, 4, -4, 8, 4, 8), PartPose.ZERO);
        root.addOrReplaceChild("spin", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-4, -1, -1, 8, 2, 2)
                .texOffs(0, 16).addBox(4, -3, -2, 4, 6, 4)
                .texOffs(0, 16).addBox(-8, -3, -2, 4, 6, 4)
                .texOffs(0, 0).addBox(-1.5F, -4, -1.5F, 3, 8, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
    public void renderBoxes(PoseStack pose, VertexConsumer out, int light, int overlay) {
        boxes.render(pose, out, light, overlay);
    }
    public void renderSpin(PoseStack pose, VertexConsumer out, int light, int overlay) {
        spin.render(pose, out, light, overlay);
    }
}
