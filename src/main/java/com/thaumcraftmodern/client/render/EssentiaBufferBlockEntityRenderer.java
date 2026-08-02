package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Draws TC4's thin conduit arms from the buffer body to connected neighbours. */
public final class EssentiaBufferBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaBufferBlockEntity> {
    private static final ResourceLocation PIPE_BUFFER = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/block/pipe_buffer.png"
    );
    private static final float ARM_MIN = 7.0F / 16.0F;
    private static final float ARM_MAX = 9.0F / 16.0F;
    private static final float BODY_MIN = 4.0F / 16.0F;
    private static final float BODY_MAX = 12.0F / 16.0F;

    public EssentiaBufferBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(EssentiaBufferBlockEntity buffer, float partialTick,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (buffer.getLevel() == null) {
            return;
        }
        VertexConsumer out = buffers.getBuffer(
                RenderType.entityCutoutNoCull(PIPE_BUFFER)
        );
        for (Direction side : Direction.values()) {
            if (!buffer.sideOpen(side)
                    || EssentiaConnections.neighbour(
                            buffer.getLevel(), buffer.getBlockPos(), side
                    ).isEmpty()) {
                continue;
            }
            float minX = ARM_MIN;
            float minY = ARM_MIN;
            float minZ = ARM_MIN;
            float maxX = ARM_MAX;
            float maxY = ARM_MAX;
            float maxZ = ARM_MAX;
            switch (side) {
                case DOWN -> minY = 0.0F;
                case UP -> maxY = 1.0F;
                case NORTH -> minZ = 0.0F;
                case SOUTH -> maxZ = 1.0F;
                case WEST -> minX = 0.0F;
                case EAST -> maxX = 1.0F;
            }
            switch (side) {
                case DOWN -> maxY = BODY_MIN;
                case UP -> minY = BODY_MAX;
                case NORTH -> maxZ = BODY_MIN;
                case SOUTH -> minZ = BODY_MAX;
                case WEST -> maxX = BODY_MIN;
                case EAST -> minX = BODY_MAX;
            }
            EssentiaTubeBlockEntityRenderer.tubeCuboid(
                    out,
                    poses.last(),
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    light,
                    overlay
            );
        }
    }
}
