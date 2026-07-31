package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TaintSporeRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        TaintSporeModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/taint_spore.png"
    );
    private static final float INITIAL_SPORE_SIZE = 2.0F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new TaintSporeModel(context.bakeLayer(TaintSporeModel.LAYER)),
                0.25F
        );
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        float flutter = 0.025F * Mth.sin(
                (entity.tickCount + partialTick) * 0.075F
        );
        float base = -0.12F * INITIAL_SPORE_SIZE;
        pose.scale(base - flutter, base + flutter, base - flutter);
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
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
