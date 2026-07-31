package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EldritchConstructRenderer
        extends MobRenderer<LegacyThaumcraftMob, EldritchConstructModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/eldritch_golem.png"
    );

    public EldritchConstructRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new EldritchConstructModel(
                        context.bakeLayer(EldritchConstructModel.LAYER)
                ),
                0.9F
        );
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(2.15F, 2.15F, 2.15F);
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
