package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class EssentiaJarBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaJarBlockEntity> {
    private static final ResourceLocation LIQUID = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "block/animatedglow");

    public EssentiaJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaJarBlockEntity jar, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (jar.amount() > 0 && jar.aspect() != null) {
            renderLiquid(jar.aspect(), jar.amount(), poseStack, buffers, packedOverlay);
        }
        if (jar.filter() != null) {
            ClassicJarLabelRenderer.render(jar.filter(), jar.filterFacing(),
                    poseStack, buffers, packedLight, packedOverlay);
        }
    }

    public static void renderLiquid(String aspect, int amount,
            PoseStack poseStack, MultiBufferSource buffers, int packedOverlay) {
        int color = AspectRegistryRuntime.find(aspect)
                .map(AspectDefinition::color).orElse(0xFFFFFF);
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        float min = 4.0F / 16.0F;
        float max = 12.0F / 16.0F;
        float bottom = 1.0F / 16.0F;
        float top = bottom + 10.0F / 16.0F
                * amount / EssentiaJarBlockEntity.CAPACITY;
        TextureAtlasSprite liquidSprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(LIQUID);
        VertexConsumer consumer = buffers.getBuffer(
                ClassicJarLiquidRenderType.get());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        quad(consumer, matrix, liquidSprite, red, green, blue,
                min, top, min, max, top, min, max, top, max, min, top, max);
        quad(consumer, matrix, liquidSprite, red, green, blue,
                min, bottom, max, max, bottom, max, max, bottom, min,
                min, bottom, min);
        quad(consumer, matrix, liquidSprite, red, green, blue,
                min, bottom, min, min, bottom, max, min, top, max,
                min, top, min);
        quad(consumer, matrix, liquidSprite, red, green, blue,
                max, bottom, max, max, bottom, min, max, top, min,
                max, top, max);
        quad(consumer, matrix, liquidSprite, red, green, blue,
                max, bottom, min, min, bottom, min, min, top, min,
                max, top, min);
        quad(consumer, matrix, liquidSprite, red, green, blue,
                min, bottom, max, max, bottom, max, max, top, max,
                min, top, max);
    }

    private static void quad(VertexConsumer out, Matrix4f pose,
            TextureAtlasSprite sprite,
            int red, int green, int blue,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(out, pose, sprite, x1, y1, z1, 0, 1, red, green, blue);
        vertex(out, pose, sprite, x2, y2, z2, 1, 1, red, green, blue);
        vertex(out, pose, sprite, x3, y3, z3, 1, 0, red, green, blue);
        vertex(out, pose, sprite, x4, y4, z4, 0, 0, red, green, blue);
    }

    private static void vertex(VertexConsumer out, Matrix4f pose,
            TextureAtlasSprite sprite,
            float x, float y, float z, float u, float v,
            int red, int green, int blue) {
        float atlasU = u == 0.0F ? sprite.getU0() : sprite.getU1();
        float atlasV = v == 0.0F ? sprite.getV0() : sprite.getV1();
        out.vertex(pose, x, y, z).color(red, green, blue, 255)
                .uv(atlasU, atlasV).endVertex();
    }
}
