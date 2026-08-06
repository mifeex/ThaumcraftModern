package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.world.block.entity.WardedBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Renders the protected state so warding is visually identical to the source block. */
public final class WardedBlockEntityRenderer implements BlockEntityRenderer<WardedBlockEntity> {
    public WardedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(WardedBlockEntity entity, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        if (entity.stored() != null)
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    entity.stored(), pose, buffers, light, overlay);
    }
}
