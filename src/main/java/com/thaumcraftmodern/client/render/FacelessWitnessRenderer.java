package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.FacelessWitnessEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class FacelessWitnessRenderer extends MobRenderer<
        FacelessWitnessEntity,
        FacelessWitnessModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/faceless_witness.png"
    );

    public FacelessWitnessRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FacelessWitnessModel(
                        context.bakeLayer(FacelessWitnessModel.LAYER)
                ),
                0.55F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(FacelessWitnessEntity entity) {
        return TEXTURE;
    }
}
