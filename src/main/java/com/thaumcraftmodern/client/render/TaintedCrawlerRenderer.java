package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 Taint Spider: vanilla spider geometry at 40% width and 50% height,
 * with the original opaque skin and additive full-bright eyes.
 */
public final class TaintedCrawlerRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        SpiderModel<LegacyThaumcraftMob>> {
    private static final ResourceLocation TEXTURE =
            texture("taint_spider.png");
    private static final ResourceLocation EYES =
            texture("taint_spider_eyes.png");

    public TaintedCrawlerRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)),
                0.5F
        );
        addLayer(new Eyes(this));
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(0.4F, 0.5F, 0.4F);
    }

    @Override
    protected float getFlipDegrees(LegacyThaumcraftMob entity) {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + name
        );
    }

    private static final class Eyes extends RenderLayer<
            LegacyThaumcraftMob,
            SpiderModel<LegacyThaumcraftMob>> {
        private Eyes(RenderLayerParent<
                LegacyThaumcraftMob,
                SpiderModel<LegacyThaumcraftMob>> parent) {
            super(parent);
        }

        @Override
        public void render(
                PoseStack pose,
                MultiBufferSource buffers,
                int packedLight,
                LegacyThaumcraftMob entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            VertexConsumer vertices = buffers.getBuffer(
                    RenderType.eyes(EYES)
            );
            getParentModel().renderToBuffer(
                    pose,
                    vertices,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
    }
}
