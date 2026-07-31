package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Full-texture crossed billboards preserve the original wisp/firebat art
 * without forcing those non-humanoid textures onto a vanilla biped model.
 */
public final class LegacyFlyingMobRenderer
        extends EntityRenderer<LegacyThaumcraftMob> {
    public LegacyFlyingMobRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.15F;
    }

    @Override
    public void render(
            LegacyThaumcraftMob entity,
            float yaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        pose.pushPose();
        pose.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        float pulse = entity.kind() == LegacyMobKind.WISP
                ? 0.75F + (float) Math.sin((entity.tickCount + partialTick) * 0.2F)
                        * 0.08F
                : 0.55F;
        pose.scale(pulse, pulse, pulse);
        PoseStack.Pose last = pose.last();
        VertexConsumer consumer = buffers.getBuffer(
                RenderType.entityTranslucent(getTextureLocation(entity))
        );
        int light = entity.kind() == LegacyMobKind.WISP
                ? LightTexture.FULL_BRIGHT
                : packedLight;
        vertex(consumer, last.pose(), last.normal(), -0.5F, -0.5F, 0.0F, 1.0F, light);
        vertex(consumer, last.pose(), last.normal(), 0.5F, -0.5F, 1.0F, 1.0F, light);
        vertex(consumer, last.pose(), last.normal(), 0.5F, 0.5F, 1.0F, 0.0F, light);
        vertex(consumer, last.pose(), last.normal(), -0.5F, 0.5F, 0.0F, 0.0F, light);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f pose,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v,
            int light
    ) {
        consumer.vertex(pose, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return entity.kind().texture();
    }
}
