package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class EldritchCrabRenderer
        extends MobRenderer<LegacyThaumcraftMob, EldritchCrabModel> {
    private static final ResourceLocation TEXTURE = texture("crab.png");
    private static final ResourceLocation OVERLAY = texture("craboverlay.png");

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new EldritchCrabModel(
                        context.bakeLayer(EldritchCrabModel.LAYER)
                ),
                1.0F
        );
        addLayer(new GlowLayer(this));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + name
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }

    private static final class GlowLayer
            extends RenderLayer<LegacyThaumcraftMob, EldritchCrabModel> {
        private GlowLayer(EldritchCrabRenderer renderer) {
            super(renderer);
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
            var vertices = buffers.getBuffer(
                    RenderType.entityTranslucentEmissive(OVERLAY)
            );
            getParentModel().renderToBuffer(
                    pose,
                    vertices,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    1, 1, 1, 1
            );
        }
    }
}
