package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.nodejar.NodeJarCodec;
import com.thaumcraftmodern.nodejar.NodeJarData;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Modern equivalent of TC4 4.2.3.5's {@code ItemJarNodeRenderer}.
 */
final class JarredAuraNodeItemRenderer
        extends BlockEntityWithoutLevelRenderer {
    JarredAuraNodeItemRenderer() {
        this(Minecraft.getInstance());
    }

    private JarredAuraNodeItemRenderer(Minecraft minecraft) {
        super(
                minecraft.getBlockEntityRenderDispatcher(),
                minecraft.getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        NodeJarData data;
        try {
            data = NodeJarCodec.read(stack).orElse(null);
        } catch (RuntimeException exception) {
            data = null;
        }
        if (data != null) {
            ClassicAuraNodeRenderer.renderJarItemNode(
                    data.node(),
                    poseStack,
                    buffers
            );
        }
        /*
         * TC4's ItemJarNodeRenderer finished by asking RenderBlocks to draw
         * BlockJar. Reuse the same baked model as the placed block so the item
         * and world object share the original two-cuboid geometry, UV mapping,
         * and jar_side/jar_top/jar_bottom textures.
         */
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        ModBlocks.JARRED_AURA_NODE.get().defaultBlockState(),
                        poseStack,
                        buffers,
                        packedLight,
                        packedOverlay
                );
    }
}
