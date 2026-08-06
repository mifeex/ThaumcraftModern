package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 inventory fallback: north orientation and sinusoidal self-inflation. */
final class ArcaneBellowsItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ArcaneBellowsModel model;
    ArcaneBellowsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        model = new ArcaneBellowsModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ArcaneBellowsModel.LAYER));
    }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        float ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        float inflation = net.minecraft.util.Mth.sin(ticks / 8.0F) * 0.3F + 0.7F;
        poses.pushPose();
        // ItemWoodenDeviceRenderer restores the inventory origin and rotates
        // bellows 90 degrees before delegating to TileBellowsRenderer.
        poses.translate(0.5D, 0.5D, 0.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.translate(-0.5D, -0.5D, -0.5D);
        poses.translate(0.5D, -0.5D, 0.5D);
        ArcaneBellowsBlockEntityRenderer.rotateFromOrientation(net.minecraft.core.Direction.NORTH, poses);
        model.render(poses, buffers.getBuffer(RenderType.entityCutoutNoCull(
                ArcaneBellowsBlockEntityRenderer.TEXTURE)), light, overlay, inflation);
        poses.popPose();
    }
}
