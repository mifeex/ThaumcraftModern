package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.item.WardedJarItem;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Modern port of TC4's ItemJarFilledRenderer. */
final class WardedJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    WardedJarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poses, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        WardedJarItem.contents(stack).ifPresent(contents -> {
            if (contents.aspect() != null && contents.amount() > 0) {
                EssentiaJarBlockEntityRenderer.renderLiquid(
                        contents.aspect(), contents.amount(), poses, buffers,
                        packedOverlay);
            }
        });
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ModBlocks.WARDED_JAR.get().defaultBlockState(),
                poses, buffers, packedLight, packedOverlay);
    }
}
