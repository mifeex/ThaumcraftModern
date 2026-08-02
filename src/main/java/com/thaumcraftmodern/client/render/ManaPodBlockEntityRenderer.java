package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.ManaPodBlock;
import com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Modern pose-stack port of TC4 TileManaPodRenderer. */
public final class ManaPodBlockEntityRenderer
        implements BlockEntityRenderer<ManaPodBlockEntity> {
    private static final ResourceLocation POD0 = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/manapod_0.png"
    );
    private static final ResourceLocation POD2 = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/manapod_2.png"
    );
    private static final int BASE_RED = 37;
    private static final int BASE_GREEN = 157;
    private static final int BASE_BLUE = 117;

    private final ClassicManaPodModel model;

    public ManaPodBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        model = new ClassicManaPodModel(
                context.bakeLayer(ClassicManaPodModel.LAYER)
        );
    }

    @Override
    public void render(
            ManaPodBlockEntity pod,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        int age = pod.getBlockState().getValue(ManaPodBlock.AGE);
        if (age <= 1) {
            return;
        }

        pose.pushPose();
        pose.translate(0.5D, 0.75D, 0.5D);
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));

        if (age > 2) {
            float pulse = pulse(pod, partialTick);
            pose.pushPose();
            pose.translate(0.0D, 0.1D, 0.0D);
            float scale = 0.125F * age * pulse;
            pose.scale(scale, scale, scale);
            VertexConsumer inner = buffers.getBuffer(
                    RenderType.entityTranslucent(POD0)
            );
            model.renderInner(
                    pose,
                    inner,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay
            );
            pose.popPose();
        }

        float scale = 0.15F * age;
        pose.scale(scale, scale, scale);
        int color = color(pod, age);
        VertexConsumer outer = buffers.getBuffer(
                RenderType.entityTranslucent(POD2)
        );
        model.renderOuter(
                pose,
                outer,
                packedLight,
                packedOverlay,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                0.9F
        );
        pose.popPose();
    }

    static int color(ManaPodBlockEntity pod, int age) {
        AspectDefinition definition = pod.aspect() == null
                ? null
                : AspectRegistryRuntime.find(pod.aspect()).orElse(null);
        if (definition == null) {
            return rgb(BASE_RED, BASE_GREEN, BASE_BLUE);
        }
        int aspectColor = definition.color();
        if (age == ManaPodBlock.MAX_AGE) {
            return aspectColor;
        }
        int mix = age - 2;
        return rgb(
                (BASE_RED + ((aspectColor >> 16) & 0xFF) * mix) / (mix + 1),
                (BASE_GREEN + ((aspectColor >> 8) & 0xFF) * mix) / (mix + 1),
                (BASE_BLUE + (aspectColor & 0xFF) * mix) / (mix + 1)
        );
    }

    private static float pulse(ManaPodBlockEntity pod, float partialTick) {
        long gameTime = pod.getLevel() == null
                ? 0L : pod.getLevel().getGameTime();
        return (float) Math.sin(
                (gameTime + partialTick + pod.getBlockPos().hashCode() % 100)
                        / 8.0F
        ) * 0.1F + 0.9F;
    }

    private static int rgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }
}
