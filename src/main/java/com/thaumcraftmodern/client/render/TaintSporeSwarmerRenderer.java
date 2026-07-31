package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintSporeSwarmerRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        TaintSporeSwarmerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/taint_spore.png"
    );

    public TaintSporeSwarmerRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new TaintSporeSwarmerModel(
                        context.bakeLayer(TaintSporeSwarmerModel.LAYER)
                ),
                0.25F
        );
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
