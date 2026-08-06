package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class ConvertedVillagerRenderer extends MobRenderer<
        LegacyThaumcraftMob, VillagerModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation BASE = new ResourceLocation(
            "minecraft", "textures/entity/villager/villager.png");
    private static final ResourceLocation GARMENTS = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/converted_villager_garments.png");
    private static final ResourceLocation PLAINS_CLOTHES = new ResourceLocation(
            "minecraft", "textures/entity/villager/type/plains.png");
    private static final ResourceLocation CONVERTED_PROFESSION = new ResourceLocation(
            "minecraft", "textures/entity/villager/profession/leatherworker.png");
    private final ConvertedVillagerModel garments;

    public ConvertedVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        garments = new ConvertedVillagerModel(
                context.bakeLayer(ConvertedVillagerModel.LAYER));
        addLayer(villagerClothesLayer(PLAINS_CLOTHES, 1.0F, 1.0F, 1.0F));
        addLayer(villagerClothesLayer(CONVERTED_PROFESSION, 0.82F, 0.22F, 0.25F));
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(com.mojang.blaze3d.vertex.PoseStack pose,
                               MultiBufferSource buffers, int light,
                               LegacyThaumcraftMob entity, float limbSwing,
                               float limbSwingAmount, float partialTick,
                               float ageInTicks, float netHeadYaw,
                               float headPitch) {
                garments.renderToBuffer(
                        pose,
                        buffers.getBuffer(RenderType.entityCutoutNoCull(GARMENTS)),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F
                );
            }
        });
    }

    private RenderLayer<LegacyThaumcraftMob, VillagerModel<LegacyThaumcraftMob>>
            villagerClothesLayer(ResourceLocation texture,
                                 float red, float green, float blue) {
        return new RenderLayer<>(this) {
            @Override
            public void render(com.mojang.blaze3d.vertex.PoseStack pose,
                               MultiBufferSource buffers, int light,
                               LegacyThaumcraftMob entity, float limbSwing,
                               float limbSwingAmount, float partialTick,
                               float ageInTicks, float netHeadYaw,
                               float headPitch) {
                getParentModel().renderToBuffer(
                        pose,
                        buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        red, green, blue, 1.0F
                );
            }
        };
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return BASE;
    }

}
