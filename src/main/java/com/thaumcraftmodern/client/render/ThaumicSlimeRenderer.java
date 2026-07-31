package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Classic TC4 thaumic-slime presentation: an opaque inner cube inside a
 * translucent, slightly larger slime shell.
 */
public final class ThaumicSlimeRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        SlimeModel<LegacyThaumcraftMob>> {
    private static final float CLASSIC_SHADOW_RADIUS = 0.25F;

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)),
                CLASSIC_SHADOW_RADIUS
        );
        addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
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
        // TC4 scales the visible cube by sqrt(size), with a small constant
        // bulge. The dynamic collision box is handled by the entity itself.
        float classicScale =
                (float) Math.sqrt(entity.thaumicSlimeSize()) + 0.1F;
        pose.scale(
                classicScale * 0.999F,
                classicScale * 0.999F,
                classicScale * 0.999F
        );
        pose.translate(0.0F, 0.001F, 0.0F);
    }
}
