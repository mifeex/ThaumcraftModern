package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Thin TC4 conduit arms for the side-programmable improved buffer. */
public final class AdvancedEssentiaBufferBlockEntityRenderer
        implements BlockEntityRenderer<AdvancedEssentiaBufferBlockEntity> {
    private static final ResourceLocation PIPE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/pipe_buffer.png");
    private static final float ARM_MIN = 7.0F / 16.0F;
    private static final float ARM_MAX = 9.0F / 16.0F;
    private static final float BODY_MIN = 4.0F / 16.0F;
    private static final float BODY_MAX = 12.0F / 16.0F;

    public AdvancedEssentiaBufferBlockEntityRenderer(
            BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AdvancedEssentiaBufferBlockEntity buffer,
            float partialTick, PoseStack poses, MultiBufferSource buffers,
            int light, int overlay) {
        if (buffer.getLevel() == null) return;
        VertexConsumer out = buffers.getBuffer(
                RenderType.entityCutoutNoCull(PIPE));
        for (Direction side : Direction.values()) {
            if (!buffer.isConnectable(side)
                    || EssentiaConnections.neighbour(buffer.getLevel(),
                            buffer.getBlockPos(), side).isEmpty()) continue;
            float minX = ARM_MIN, minY = ARM_MIN, minZ = ARM_MIN;
            float maxX = ARM_MAX, maxY = ARM_MAX, maxZ = ARM_MAX;
            switch (side) {
                case DOWN -> { minY = 0.0F; maxY = BODY_MIN; }
                case UP -> { minY = BODY_MAX; maxY = 1.0F; }
                case NORTH -> { minZ = 0.0F; maxZ = BODY_MIN; }
                case SOUTH -> { minZ = BODY_MAX; maxZ = 1.0F; }
                case WEST -> { minX = 0.0F; maxX = BODY_MIN; }
                case EAST -> { minX = BODY_MAX; maxX = 1.0F; }
            }
            EssentiaTubeBlockEntityRenderer.tubeCuboid(out, poses.last(),
                    minX, minY, minZ, maxX, maxY, maxZ, light, overlay);
        }
    }
}
