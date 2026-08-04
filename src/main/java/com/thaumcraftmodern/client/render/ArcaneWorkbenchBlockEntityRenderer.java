package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

/** Renders only the casting tool; the empty workbench remains the block model. */
public final class ArcaneWorkbenchBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneWorkbenchBlockEntity> {
    private final ClassicWandItemRenderer wandRenderer =
            new ClassicWandItemRenderer();

    public ArcaneWorkbenchBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            ArcaneWorkbenchBlockEntity workbench,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack stack = workbench.wand().getItem(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof WandItem)) {
            return;
        }
        wandRenderer.renderOnArcaneWorkbench(
                stack,
                poseStack,
                buffers,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }
}
