package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.NodeVisibilityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Objects;

final class AuraNodeBlockEntityRenderer
        implements BlockEntityRenderer<AuraNodeBlockEntity> {
    private final Item thaumometer;
    private final Item goggles;
    private final ClassicNodeDrainRenderer drainRenderer =
            new ClassicNodeDrainRenderer();

    AuraNodeBlockEntityRenderer(
            BlockEntityRendererProvider.Context context,
            Item thaumometer,
            Item goggles
    ) {
        Objects.requireNonNull(context, "context");
        this.thaumometer = Objects.requireNonNull(thaumometer, "thaumometer");
        this.goggles = Objects.requireNonNull(goggles, "goggles");
    }

    @Override
    public void render(
            AuraNodeBlockEntity node,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        Player player = Minecraft.getInstance().player;
        NodeVisibilityService.Visibility visibility = player == null
                ? NodeVisibilityService.Visibility.SUBTLE
                : NodeVisibilityService.decideFromPlayer(
                        player,
                        stack -> stack.is(thaumometer),
                        com.thaumcraftmodern.item.RevealingGear::equipped
                );
        ClassicAuraNodeRenderer.renderWorldNode(
                node.snapshotState(),
                node.getBlockPos(),
                visibility,
                partialTick,
                poseStack,
                buffers
        );
        drainRenderer.render(node, partialTick, poseStack, buffers);
    }

    @Override
    public int getViewDistance() {
        return ClassicAuraNodeRenderer.VIEW_DISTANCE;
    }
}
