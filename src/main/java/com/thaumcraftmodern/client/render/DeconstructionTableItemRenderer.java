package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class DeconstructionTableItemRenderer
        extends BlockEntityWithoutLevelRenderer {
    private final DeconstructionTableModel model;
    private final ClassicThaumometerItemRenderer thaumometerRenderer;

    DeconstructionTableItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
        model = new DeconstructionTableModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        DeconstructionTableBlockEntityRenderer.LAYER
                )
        );
        thaumometerRenderer = new ClassicThaumometerItemRenderer();
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
        poses.pushPose();
        poses.translate(0.5D, 1.0D, 0.5D);
        poses.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.render(
                poses,
                buffers.getBuffer(RenderType.entityCutoutNoCull(
                        DeconstructionTableBlockEntityRenderer.TABLE_TEXTURE
                )),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
        poses.popPose();
        if (displayContext == ItemDisplayContext.GUI) {
            DeconstructionTableBlockEntityRenderer.renderThaumometer(
                    thaumometerRenderer,
                    poses,
                    buffers,
                    packedLight,
                    packedOverlay
            );
        }
    }
}
