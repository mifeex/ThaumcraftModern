package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Classic 64x32 biped used underneath the TC4 Crimson Cult armor. */
public final class CrimsonCultistModel
        extends HumanoidModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "crimson_cultist"),
            "main"
    );

    public CrimsonCultistModel(net.minecraft.client.model.geom.ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }
}
