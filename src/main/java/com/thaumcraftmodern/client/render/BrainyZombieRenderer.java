package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Dedicated renderer shared by TC4's Angry and Furious Zombie variants.
 */
public final class BrainyZombieRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        BrainyZombieModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/bzombie.png"
    );

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new BrainyZombieModel(context.bakeLayer(ModelLayers.ZOMBIE)),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        if (entity.kind() == LegacyMobKind.FURIOUS_ZOMBIE) {
            float anger = entity.furiousAnger();
            pose.scale(anger, anger, anger);
        }
    }
}
