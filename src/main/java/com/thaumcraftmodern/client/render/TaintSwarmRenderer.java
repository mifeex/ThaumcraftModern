package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * TC4's swarm entity was invisible. Its body was 10-25 independent FXSwarm
 * sprites using frames 7-14 on the fourth row of particles.png.
 */
public final class TaintSwarmRenderer
        extends EntityRenderer<LegacyThaumcraftMob> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/misc/particles.png"
    );
    private static final int PARTICLES = 25;
    private static final float CELL = 1.0F / 16.0F;
    private static final float UV_SIZE = 0.0624375F;
    private static final float TAINT_RED = 0x6D / 255.0F;
    private static final float TAINT_GREEN = 0x41 / 255.0F;
    private static final float TAINT_BLUE = 0x89 / 255.0F;

    public TaintSwarmRenderer(EntityRendererProvider.Context context) {
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
        float time = entity.tickCount + partialTick;
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(TEXTURE)
        );
        for (int index = 0; index < PARTICLES; index++) {
            float phase = index * 2.3999632F
                    + (entity.getId() & 31) * 0.17F;
            float radius = 0.18F + hash(index, 3) * 0.72F;
            float x = Mth.sin(time * (0.08F + hash(index, 7) * 0.05F)
                    + phase) * radius;
            float z = Mth.cos(time * (0.075F + hash(index, 11) * 0.05F)
                    + phase) * radius;
            float y = 0.25F + hash(index, 17) * 1.5F
                    + Mth.sin(time * 0.22F + phase) * 0.2F;
            float bob = Mth.sin((time + index * 3.0F) / 3.0F)
                    * 0.25F + 1.0F;
            float size = (0.10F + hash(index, 23) * 0.05F) * bob;
            int frame = 7 + ((entity.tickCount + index) & 7);
            float u0 = frame * CELL;
            float u1 = u0 + UV_SIZE;
            float v0 = 0.25F;
            float v1 = v0 + UV_SIZE;
            float hurtTint = entity.hurtTime > 0 ? 0.65F : 1.0F;

            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            PoseStack.Pose last = pose.last();
            vertex(vertices, last.pose(), last.normal(),
                    -size, -size, u1, v1,
                    TAINT_RED, TAINT_GREEN * hurtTint, TAINT_BLUE * hurtTint);
            vertex(vertices, last.pose(), last.normal(),
                    size, -size, u0, v1,
                    TAINT_RED, TAINT_GREEN * hurtTint, TAINT_BLUE * hurtTint);
            vertex(vertices, last.pose(), last.normal(),
                    size, size, u0, v0,
                    TAINT_RED, TAINT_GREEN * hurtTint, TAINT_BLUE * hurtTint);
            vertex(vertices, last.pose(), last.normal(),
                    -size, size, u1, v0,
                    TAINT_RED, TAINT_GREEN * hurtTint, TAINT_BLUE * hurtTint);
            pose.popPose();
        }
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private static float hash(int index, int salt) {
        int value = index * 1103515245 + salt * 12345;
        value ^= value >>> 16;
        return (value & 0xFFFF) / 65535.0F;
    }

    private static void vertex(
            VertexConsumer vertices,
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
        vertices.vertex(pose, x, y, 0)
                .color(red, green, blue, 1)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
