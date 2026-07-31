package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public final class PechRenderer
        extends MobRenderer<LegacyThaumcraftMob, PechModel> {
    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(PechModel.LAYER)), 0.5F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        String texture = switch (entity.pechType()) {
            case 1 -> "pech_thaum.png";
            case 2 -> "pech_stalker.png";
            default -> "pech_forage.png";
        };
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + texture
        );
    }
}
