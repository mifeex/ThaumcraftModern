package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class EnergizedAuraNodeBlockEntityRenderer
        implements BlockEntityRenderer<EnergizedAuraNodeBlockEntity> {
    private static final float OUTER_RING_HALF_SIZE = 0.40F;
    private static final float INNER_RING_HALF_SIZE = 0.33F;
    private static final float AURA_LAYER_OPACITY = 0.32F;

    public EnergizedAuraNodeBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            EnergizedAuraNodeBlockEntity tile,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        ClassicAuraNodeRenderer.renderEnergizedNode(
                tile.displayState(),
                tile.getBlockPos(),
                AURA_LAYER_OPACITY,
                partialTick,
                pose,
                buffers
        );
        int frame = Math.floorMod(
                System.nanoTime() / 40_000_000L + tile.getBlockPos().getX(),
                16
        );
        float u0 = frame / 16.0F;
        float u1 = (frame + 1) / 16.0F;
        int innerFrame = Math.floorMod(frame - 1, 16);
        float innerU0 = innerFrame / 16.0F;
        float innerU1 = (innerFrame + 1) / 16.0F;
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera()
                .rotation());
        VertexConsumer consumer = buffers.getBuffer(
                ClassicNodeRenderTypes.energizedRing());
        var matrix = pose.last();
        ringQuad(consumer, matrix, OUTER_RING_HALF_SIZE, u0, u1, 0.95F);
        ringQuad(consumer, matrix, INNER_RING_HALF_SIZE,
                innerU0, innerU1, 0.70F);
        pose.popPose();
    }

    private static void ringQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float halfSize,
            float u0,
            float u1,
            float alpha
    ) {
        quad(consumer, pose, -halfSize, -halfSize, u0, 1.0F, alpha);
        quad(consumer, pose, halfSize, -halfSize, u1, 1.0F, alpha);
        quad(consumer, pose, halfSize, halfSize, u1, 0.0F, alpha);
        quad(consumer, pose, -halfSize, halfSize, u0, 0.0F, alpha);
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            float alpha
    ) {
        consumer.vertex(pose.pose(), x, y, 0)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 0, 1)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(EnergizedAuraNodeBlockEntity tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
