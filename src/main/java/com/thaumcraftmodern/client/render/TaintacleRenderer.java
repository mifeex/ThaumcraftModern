package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.geom.ModelLayerLocation;
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
 * Dedicated TC4 renderer for normal, small and giant taintacles.
 */
public final class TaintacleRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        TaintacleModel> {
    public TaintacleRenderer(
            EntityRendererProvider.Context context,
            LegacyMobKind kind
    ) {
        super(
                context,
                new TaintacleModel(
                        context.bakeLayer(layer(kind)),
                        length(kind)
                ),
                shadowRadius(kind)
        );
        addLayer(new GlowingTipLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return entity.kind().texture();
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        float classicScale = entity.getBbHeight() / 3.0F;
        if (entity.kind() == LegacyMobKind.GIANT_TAINTACLE) {
            classicScale *= 1.33F;
        }
        float growTicks = entity.getBbHeight() * 10.0F;
        float growth = Math.min(
                1.0F,
                (entity.tickCount + partialTick) / growTicks
        );
        pose.scale(classicScale, classicScale * growth, classicScale);
    }

    private static int length(LegacyMobKind kind) {
        return switch (kind) {
            case TAINT_TENDRIL -> TaintacleModel.TENDRIL_LENGTH;
            case GIANT_TAINTACLE -> TaintacleModel.GIANT_LENGTH;
            default -> TaintacleModel.NORMAL_LENGTH;
        };
    }

    private static float shadowRadius(LegacyMobKind kind) {
        return switch (kind) {
            case TAINT_TENDRIL -> 0.2F;
            case GIANT_TAINTACLE -> 1.0F;
            default -> 0.6F;
        };
    }

    private static ModelLayerLocation layer(LegacyMobKind kind) {
        return switch (kind) {
            case TAINT_TENDRIL -> TaintacleModel.TENDRIL_LAYER;
            case GIANT_TAINTACLE -> TaintacleModel.GIANT_LAYER;
            default -> TaintacleModel.NORMAL_LAYER;
        };
    }

    private static final class GlowingTipLayer extends RenderLayer<
            LegacyThaumcraftMob,
            TaintacleModel> {
        private GlowingTipLayer(RenderLayerParent<
                LegacyThaumcraftMob,
                TaintacleModel> parent) {
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
                    RenderType.eyes(entity.kind().texture())
            );
            getParentModel().renderGlowingTip(
                    pose,
                    vertices,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
        }
    }
}
