package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.EssentiaReservoirBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Renders the original colored, gradually cycling essentia volume. */
public final class EssentiaReservoirBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaReservoirBlockEntity> {
    private static final ResourceLocation LIQUID = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "block/animatedglow");

    public EssentiaReservoirBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(EssentiaReservoirBlockEntity reservoir,
            float partialTick, PoseStack poses, MultiBufferSource buffers,
            int light, int overlay) {
        if (reservoir.totalAmount() <= 0 || reservoir.displayAspect() == null) return;
        float min = 3.0F / 16.0F;
        float max = 13.0F / 16.0F;
        float bottom = 3.0F / 16.0F;
        float top = bottom + 10.0F / 16.0F * reservoir.totalAmount()
                / EssentiaReservoirBlockEntity.CAPACITY;
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(LIQUID);
        VertexConsumer out = buffers.getBuffer(ClassicJarLiquidRenderType.get());
        Matrix4f matrix = poses.last().pose();
        int red = Math.round(255.0F * reservoir.red());
        int green = Math.round(255.0F * reservoir.green());
        int blue = Math.round(255.0F * reservoir.blue());
        cuboid(out, matrix, sprite, red, green, blue,
                min, bottom, min, max, top, max);
    }

    private static void cuboid(VertexConsumer out, Matrix4f matrix,
            TextureAtlasSprite sprite, int red, int green, int blue,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        quad(out, matrix, sprite, red, green, blue, minX,maxY,minZ, minX,maxY,maxZ, maxX,maxY,maxZ, maxX,maxY,minZ);
        quad(out, matrix, sprite, red, green, blue, minX,minY,maxZ, minX,minY,minZ, maxX,minY,minZ, maxX,minY,maxZ);
        quad(out, matrix, sprite, red, green, blue, minX,minY,minZ, minX,minY,maxZ, minX,maxY,maxZ, minX,maxY,minZ);
        quad(out, matrix, sprite, red, green, blue, maxX,minY,maxZ, maxX,minY,minZ, maxX,maxY,minZ, maxX,maxY,maxZ);
        quad(out, matrix, sprite, red, green, blue, maxX,minY,minZ, minX,minY,minZ, minX,maxY,minZ, maxX,maxY,minZ);
        quad(out, matrix, sprite, red, green, blue, minX,minY,maxZ, maxX,minY,maxZ, maxX,maxY,maxZ, minX,maxY,maxZ);
    }

    private static void quad(VertexConsumer out, Matrix4f matrix,
            TextureAtlasSprite sprite, int red, int green, int blue,
            float x1,float y1,float z1, float x2,float y2,float z2,
            float x3,float y3,float z3, float x4,float y4,float z4) {
        vertex(out,matrix,sprite,x1,y1,z1,0,0,red,green,blue);
        vertex(out,matrix,sprite,x2,y2,z2,0,1,red,green,blue);
        vertex(out,matrix,sprite,x3,y3,z3,1,1,red,green,blue);
        vertex(out,matrix,sprite,x4,y4,z4,1,0,red,green,blue);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix,
            TextureAtlasSprite sprite, float x,float y,float z, float u,float v,
            int red,int green,int blue) {
        out.vertex(matrix,x,y,z).color(red,green,blue,230)
                .uv(u == 0 ? sprite.getU0() : sprite.getU1(),
                        v == 0 ? sprite.getV0() : sprite.getV1()).endVertex();
    }
}
