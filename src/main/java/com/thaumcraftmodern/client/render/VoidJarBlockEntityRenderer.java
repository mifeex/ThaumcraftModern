package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class VoidJarBlockEntityRenderer implements BlockEntityRenderer<VoidJarBlockEntity> {
    public VoidJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(VoidJarBlockEntity jar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (jar.aspect() != null && jar.amount() > 0) {
            EssentiaJarBlockEntityRenderer.renderLiquid(jar.aspect(), jar.amount(),
                    poseStack, buffers, packedOverlay);
        }
        if (jar.filter() != null) {
            ClassicJarLabelRenderer.render(jar.filter(), jar.filterFacing(),
                    poseStack, buffers, packedLight, packedOverlay);
        }
    }
}
