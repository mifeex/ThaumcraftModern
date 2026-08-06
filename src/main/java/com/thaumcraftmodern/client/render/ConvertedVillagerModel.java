package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Extra cult garments placed over the vanilla villager silhouette. */
public final class ConvertedVillagerModel
        extends EntityModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "converted_villager"),
            "main"
    );

    private final ModelPart root;

    public ConvertedVillagerModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_pauldron",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.75F, -1.5F, -2.75F, 5.5F, 4.0F, 5.5F),
                PartPose.offset(-5.5F, 3.4F, 0.0F));
        root.addOrReplaceChild("left_pauldron",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-2.75F, -1.5F, -2.75F, 5.5F, 4.0F, 5.5F),
                PartPose.offset(5.5F, 3.4F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LegacyThaumcraftMob entity, float limbSwing,
                          float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer,
                               int light, int overlay, float red, float green,
                               float blue, float alpha) {
        root.render(pose, consumer, light, overlay, red, green, blue, alpha);
    }
}
