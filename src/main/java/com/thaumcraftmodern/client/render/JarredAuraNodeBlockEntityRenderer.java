package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.NodeJarData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.Objects;

final class JarredAuraNodeBlockEntityRenderer
        implements BlockEntityRenderer<JarredAuraNodeBlockEntity> {
    JarredAuraNodeBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    public void render(
            JarredAuraNodeBlockEntity jar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        jar.data()
                .map(NodeJarData::node)
                .ifPresent(node -> ClassicAuraNodeRenderer.renderJarNode(
                        node,
                        jar.getBlockPos(),
                        partialTick,
                        poseStack,
                        buffers
                ));
    }

    @Override
    public int getViewDistance() {
        return ClassicAuraNodeRenderer.VIEW_DISTANCE;
    }
}
