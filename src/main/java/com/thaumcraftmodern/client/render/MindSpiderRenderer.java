package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
 * TC4 Mind Spider: 30% vanilla spider, translucent taint skin, bright eyes.
 */
public final class MindSpiderRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        MindSpiderModel> {
    private static final ResourceLocation TEXTURE = texture("taint_spider.png");
    private static final ResourceLocation EYES =
            texture("taint_spider_eyes.png");

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new MindSpiderModel(context.bakeLayer(ModelLayers.SPIDER)),
                0.0F
        );
        addLayer(new Eyes(this));
    }

    @Override
    public void render(
            LegacyThaumcraftMob entity,
            float entityYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer != null && entity.isInvisibleTo(viewer)) {
            return;
        }
        model.setRenderAlpha(Math.min(
                0.1F,
                (entity.tickCount + partialTick) / 100.0F
        ));
        try {
            super.render(
                    entity,
                    entityYaw,
                    partialTick,
                    pose,
                    buffers,
                    packedLight
            );
        } finally {
            model.setRenderAlpha(0.1F);
        }
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(0.3F, 0.3F, 0.3F);
    }

    @Override
    protected RenderType getRenderType(
            LegacyThaumcraftMob entity,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing
    ) {
        return RenderType.entityTranslucent(TEXTURE);
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
            MindSpiderModel> {
        private Eyes(RenderLayerParent<
                LegacyThaumcraftMob,
                MindSpiderModel> parent) {
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
            VertexConsumer vertices = buffers.getBuffer(RenderType.eyes(EYES));
            getParentModel().renderEyes(
                    pose,
                    vertices,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
        }
    }
}
