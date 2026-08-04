package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Byte-coordinate port of TC4 ModelArcaneWorkbench used by the table. */
public final class DeconstructionTableModel {
    private static final String TABLE = "table";
    private final ModelPart table;

    public DeconstructionTableModel(ModelPart root) {
        table = root.getChild(TABLE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(0, 0).mirror()
                .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 8.0F, 16.0F)
                .texOffs(0, 32).mirror()
                .addBox(-8.0F, 12.0F, -8.0F, 16.0F, 4.0F, 16.0F)
                .texOffs(72, 0).mirror()
                .addBox(3.0F, 8.0F, -7.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(72, 0).mirror()
                .addBox(-7.0F, 8.0F, 3.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(72, 0).mirror()
                .addBox(3.0F, 8.0F, 3.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(72, 0).mirror()
                .addBox(-7.0F, 8.0F, -7.0F, 4.0F, 4.0F, 4.0F);
        root.addOrReplaceChild(TABLE, cubes, PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    public void render(
            PoseStack poses,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay
    ) {
        table.render(poses, vertices, packedLight, packedOverlay);
    }
}
