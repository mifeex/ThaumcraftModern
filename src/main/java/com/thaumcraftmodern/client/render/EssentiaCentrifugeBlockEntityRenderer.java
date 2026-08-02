package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.EssentiaCentrifugeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class EssentiaCentrifugeBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaCentrifugeBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/centrifuge.png");
    private final ClassicCentrifugeModel model;
    public EssentiaCentrifugeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new ClassicCentrifugeModel(context.bakeLayer(ClassicCentrifugeModel.LAYER));
    }
    @Override public void render(EssentiaCentrifugeBlockEntity machine, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        pose.pushPose();
        pose.translate(.5, .5, .5);
        pose.mulPose(Axis.XP.rotationDegrees(180));
        model.renderBoxes(pose, out, light, overlay);
        pose.mulPose(Axis.YP.rotationDegrees(machine.rotation(partialTick)));
        model.renderSpin(pose, out, light, overlay);
        pose.popPose();
    }
}
