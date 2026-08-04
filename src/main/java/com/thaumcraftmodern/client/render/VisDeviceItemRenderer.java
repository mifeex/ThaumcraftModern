package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.item.VisDeviceBlockItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.visnet.NodeStabilizerBlockEntity;
import com.thaumcraftmodern.visnet.NodeTransducerBlockEntity;
import com.thaumcraftmodern.visnet.VisChargeRelayBlockEntity;
import com.thaumcraftmodern.visnet.VisRelayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

final class VisDeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    VisDeviceItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
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
        if (!(stack.getItem() instanceof VisDeviceBlockItem item)) {
            return;
        }
        BlockEntity tile = switch (item.kind()) {
            case STABILIZER -> new NodeStabilizerBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.NODE_STABILIZER.get().defaultBlockState());
            case ADVANCED_STABILIZER -> new NodeStabilizerBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.ADVANCED_NODE_STABILIZER.get().defaultBlockState());
            case TRANSDUCER -> new NodeTransducerBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.NODE_TRANSDUCER.get().defaultBlockState());
            case RELAY -> new VisRelayBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.VIS_RELAY.get().defaultBlockState());
            case CHARGER -> new VisChargeRelayBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.VIS_CHARGE_RELAY.get().defaultBlockState());
        };
        poses.pushPose();
        if (item.kind() == VisDeviceBlockItem.Kind.RELAY) {
            // ItemMetalDeviceRenderer meta 14, including the legacy TEISR
            // origin restoration, copied in the same operation order.
            poses.translate(0.5D, 0.5D, 0.5D);
            poses.scale(1.5F, 1.5F, 1.5F);
            poses.translate(-0.5D, -0.25D, -0.5D);
        }
        renderTile(tile, poses, buffers, packedLight, packedOverlay);
        poses.popPose();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderTile(
            BlockEntity tile,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        var renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().getRenderer(tile);
        if (renderer != null) {
            renderer.render(
                    tile,
                    Minecraft.getInstance().getFrameTime(),
                    poses,
                    buffers,
                    packedLight,
                    packedOverlay
            );
        }
    }
}
