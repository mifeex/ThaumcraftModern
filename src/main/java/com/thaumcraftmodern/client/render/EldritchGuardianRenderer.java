package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;

/**
 * Dedicated renderer for the classic multi-part guardian model.
 */
public final class EldritchGuardianRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        EldritchGuardianModel> {
    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new EldritchGuardianModel(
                        context.bakeLayer(EldritchGuardianModel.LAYER)
                ),
                0.6F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return entity.kind().texture();
    }

    @Override
    public void render(
            LegacyThaumcraftMob entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        model.setRenderAlpha(classicFadeAlpha(entity));
        try {
            super.render(
                    entity,
                    entityYaw,
                    partialTick,
                    poseStack,
                    buffer,
                    packedLight
            );
        } finally {
            model.setRenderAlpha(1.0F);
        }
    }

    /**
     * TC4 guardians are semi-transparent in normal dimensions and fade away
     * between 16 and 24/32 blocks (Hard/other difficulties).
     */
    private static float classicFadeAlpha(LegacyThaumcraftMob entity) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null) {
            return 1.0F;
        }
        float fullAlphaDistanceSquared = 256.0F;
        float maxDistanceSquared =
                viewer.level().getDifficulty() == Difficulty.HARD
                        ? 576.0F
                        : 1024.0F;
        double distanceSquared = entity.distanceToSqr(viewer);
        if (distanceSquared < fullAlphaDistanceSquared) {
            return 0.6F;
        }
        double fade = 1.0D - Math.min(
                maxDistanceSquared - fullAlphaDistanceSquared,
                distanceSquared - fullAlphaDistanceSquared
        ) / (maxDistanceSquared - fullAlphaDistanceSquared);
        return (float) fade * 0.6F;
    }
}
