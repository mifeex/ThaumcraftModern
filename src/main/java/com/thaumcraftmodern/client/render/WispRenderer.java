package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Faithful modern equivalent of TC4's RenderWisp.
 */
public final class WispRenderer extends EntityRenderer<LegacyThaumcraftMob> {
    private static final int ATLAS_COLUMNS = 4;
    private static final int ATLAS_FRAMES = 16;
    private static final float FRAME_SIZE = 1.0F / ATLAS_COLUMNS;
    private static final float UV_INSET = 0.01F / 256.0F;

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
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
        int color = AspectRegistryRuntime.find(entity.wispAspect())
                .map(definition -> definition.color())
                .orElse(0xFFFFFF);
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        if (entity.hurtTime > 0) {
            red = 1.0F;
            green *= 0.85F;
            blue *= 0.85F;
        }

        int frame = entity.tickCount % ATLAS_FRAMES;
        float u0 = (frame % ATLAS_COLUMNS) * FRAME_SIZE;
        float v0 = (frame / ATLAS_COLUMNS) * FRAME_SIZE;
        float u1 = u0 + FRAME_SIZE - UV_INSET;
        float v1 = v0 + FRAME_SIZE - UV_INSET;

        pose.pushPose();
        pose.translate(0.0D, 0.45D, 0.0D);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose last = pose.last();
        VertexConsumer vertices = buffers.getBuffer(WispRenderType.get());
        vertex(vertices, last.pose(), last.normal(),
                -1.0F, -1.0F, u1, v1, red, green, blue);
        vertex(vertices, last.pose(), last.normal(),
                1.0F, -1.0F, u0, v1, red, green, blue);
        vertex(vertices, last.pose(), last.normal(),
                1.0F, 1.0F, u0, v0, red, green, blue);
        vertex(vertices, last.pose(), last.normal(),
                -1.0F, 1.0F, u1, v0, red, green, blue);
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
            float red,
            float green,
            float blue
    ) {
        consumer.vertex(pose, x, y, 0.0F)
                .color(red, green, blue, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return WispRenderType.TEXTURE;
    }
}
