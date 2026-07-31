package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Dedicated TC4 Firebat renderer; the classic normal variant scales to 35%.
 */
public final class FireBatRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        FireBatModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/firebat.png"
    );

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FireBatModel(context.bakeLayer(FireBatModel.LAYER)),
                0.25F
        );
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        pose.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float ageInTicks,
            float rotationYaw,
            float partialTick
    ) {
        pose.translate(0.0F, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0F);
        super.setupRotations(
                entity,
                pose,
                ageInTicks,
                rotationYaw,
                partialTick
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
