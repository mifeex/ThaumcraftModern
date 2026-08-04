package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.item.CrystalClusterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class CrystalClusterItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final CrystalClusterModel model;

    CrystalClusterItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
        model = new CrystalClusterModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        CrystalClusterModel.LAYER
                )
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (!(stack.getItem() instanceof CrystalClusterItem clusterItem)) {
            return;
        }
        poses.pushPose();
        // The legacy TEISR restored the inventory origin by +0.5 on every
        // axis before calling renderItemCluster.
        poses.translate(0.5D, 0.5D, 0.5D);
        CrystalClusterRenderer.renderItemCluster(
                model,
                clusterItem.variant(),
                poses,
                buffers,
                packedLight
        );
        poses.popPose();
    }
}
