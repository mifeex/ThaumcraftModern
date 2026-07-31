package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.EtherealBloomBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Modern pose-stack port of TC4's TileEtherealBloomRenderer.
 */
public final class EtherealBloomBlockEntityRenderer
        implements BlockEntityRenderer<EtherealBloomBlockEntity> {
    private static final ResourceLocation NODES = texture("misc/nodes.png");
    private static final ResourceLocation LEAVES =
            texture("block/purifier_leaves.png");
    private static final ResourceLocation STALK =
            texture("block/purifier_stalk.png");
    private final EtherealBloomCrystalModel crystalModel;

    public EtherealBloomBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        crystalModel = new EtherealBloomCrystalModel(
                context.bakeLayer(EtherealBloomCrystalModel.LAYER)
        );
    }

    @Override
    public void render(
            EtherealBloomBlockEntity bloom,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        float elapsed = bloom.growthCounter() + partialTick;
        float rc1 = Math.min(elapsed, 100.0F);
        float rc2 = Math.min(elapsed, 50.0F);
        float rc3 = Math.min(Math.max(elapsed - 33.0F, 0.0F), 33.0F);
        float rc4 = Math.min(Math.max(elapsed - 66.0F, 0.0F), 33.0F);
        float scale1 = rc1 / 100.0F;
        float scale2 = rc2 / 60.0F + 1.0F / 6.0F;
        float scale3 = rc3 / 33.0F;
        float scale4 = rc4 / 33.0F * 0.7F;

        renderNodePulse(
                bloom,
                partialTick,
                scale1,
                poseStack,
                buffers
        );
        renderCrossLayer(
                STALK,
                0.0F,
                scale1,
                scale2,
                0.0F,
                poseStack,
                buffers,
                packedLight
        );
        renderCrossLayer(
                LEAVES,
                0.25F,
                scale1 * 0.5F,
                scale3,
                0.0F,
                poseStack,
                buffers,
                packedLight
        );
        renderCrossLayer(
                LEAVES,
                0.6F,
                scale1 * 0.35F,
                scale4,
                45.0F,
                poseStack,
                buffers,
                packedLight
        );
        if (scale4 > 0.0F) {
            renderCrystal(
                    scale1,
                    scale4,
                    poseStack,
                    buffers,
                    packedLight
            );
        }
    }

    private static void renderNodePulse(
            EtherealBloomBlockEntity bloom,
            float partialTick,
            float scale,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        if (scale <= 0.0F) {
            return;
        }
        int frame = Math.floorMod(
                (int) (bloom.animationTicks() + partialTick),
                32
        );
        float u0 = frame / 32.0F;
        float u1 = u0 + 1.0F / 32.0F;
        float v0 = 6.0F / 32.0F;
        float v1 = 7.0F / 32.0F;
        poseStack.pushPose();
        poseStack.translate(0.5D, scale, 0.5D);
        poseStack.mulPose(Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .cameraOrientation());
        poseStack.scale(scale, scale, scale);
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(NODES)
        );
        quad(
                poseStack,
                vertices,
                -0.5F,
                -0.5F,
                0.5F,
                0.5F,
                u0,
                v0,
                u1,
                v1,
                0xAA,
                0xDD,
                0xFF,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
    }

    private static void renderCrossLayer(
            ResourceLocation texture,
            float y,
            float height,
            float width,
            float yawOffset,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        if (height <= 0.0F || width <= 0.0F) {
            return;
        }
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityCutoutNoCull(texture)
        );
        for (int layer = 0; layer < 4; layer++) {
            poseStack.pushPose();
            poseStack.translate(0.5D, y, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    yawOffset + layer * 90.0F
            ));
            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            float half = width * 0.5F;
            vertex(vertices, matrix, normal, -half, 0.0F, 0.0F,
                    0.0F, 1.0F, packedLight);
            vertex(vertices, matrix, normal, half, 0.0F, 0.0F,
                    1.0F, 1.0F, packedLight);
            vertex(vertices, matrix, normal, half, height, 0.0F,
                    1.0F, 0.0F, packedLight);
            vertex(vertices, matrix, normal, -half, height, 0.0F,
                    0.0F, 0.0F, packedLight);
            poseStack.popPose();
        }
    }

    private void renderCrystal(
            float height,
            float scale,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(
                0.5D - scale / 8.0F,
                height - scale / 6.0F,
                0.5D - scale / 8.0F
        );
        poseStack.scale(scale * 0.25F, scale / 3.0F, scale * 0.25F);
        VertexConsumer vertices = buffers.getBuffer(
                EtherealBloomRenderType.crystal()
        );
        crystalModel.render(
                poseStack,
                vertices,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    private static void quad(
            PoseStack poseStack,
            VertexConsumer vertices,
            float minX,
            float minY,
            float maxX,
            float maxY,
            float u0,
            float v0,
            float u1,
            float v1,
            int red,
            int green,
            int blue,
            int light
    ) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        coloredVertex(vertices, matrix, normal, minX, maxY, u0, v0,
                red, green, blue, light);
        coloredVertex(vertices, matrix, normal, maxX, maxY, u1, v0,
                red, green, blue, light);
        coloredVertex(vertices, matrix, normal, maxX, minY, u1, v1,
                red, green, blue, light);
        coloredVertex(vertices, matrix, normal, minX, minY, u0, v1,
                red, green, blue, light);
    }

    private static void vertex(
            VertexConsumer vertices,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light
    ) {
        coloredVertex(
                vertices,
                matrix,
                normal,
                x,
                y,
                z,
                u,
                v,
                255,
                255,
                255,
                light
        );
    }

    private static void coloredVertex(
            VertexConsumer vertices,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int light
    ) {
        coloredVertex(
                vertices,
                matrix,
                normal,
                x,
                y,
                0.0F,
                u,
                v,
                red,
                green,
                blue,
                light
        );
    }

    private static void coloredVertex(
            VertexConsumer vertices,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float z,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int light
    ) {
        vertices.vertex(matrix, x, y, z)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/" + path
        );
    }
}
