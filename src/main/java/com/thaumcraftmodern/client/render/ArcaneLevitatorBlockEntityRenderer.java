package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.ArcaneLevitatorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ArcaneLevitatorBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneLevitatorBlockEntity> {
    private static final ResourceLocation GLOW = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "block/animatedglow");

    public ArcaneLevitatorBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {}

    @Override
    public void render(ArcaneLevitatorBlockEntity tile, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (tile.getLevel() == null
                || ArcaneLevitatorBlockEntity.gettingPower(tile.getLevel(), tile.getBlockPos())
                || ArcaneLevitatorBlockEntity.rangeAbove(tile.getLevel(), tile.getBlockPos()) <= 0) return;
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(GLOW);
        VertexConsumer vertices = buffers.getBuffer(ArcaneLevitatorRenderType.get());
        PoseStack.Pose current = pose.last();
        if (shouldRenderFace(tile, Direction.UP))
            quad(vertices, current.pose(), current.normal(), sprite,
                    .01F,1.001F,.01F, .01F,1.001F,.99F, .99F,1.001F,.99F, .99F,1.001F,.01F,
                    0,1,0, 0,160,0);
        if (shouldRenderFace(tile, Direction.WEST))
            quad(vertices, current.pose(), current.normal(), sprite,
                    -.001F,.90F,.99F, -.001F,.10F,.99F, -.001F,.10F,.01F, -.001F,.90F,.01F,
                    -1,0,0, 221,17,255);
        if (shouldRenderFace(tile, Direction.EAST))
            quad(vertices, current.pose(), current.normal(), sprite,
                    1.001F,.90F,.01F, 1.001F,.10F,.01F, 1.001F,.10F,.99F, 1.001F,.90F,.99F,
                    1,0,0, 221,17,255);
        if (shouldRenderFace(tile, Direction.NORTH))
            quad(vertices, current.pose(), current.normal(), sprite,
                    .99F,.90F,-.001F, .99F,.10F,-.001F, .01F,.10F,-.001F, .01F,.90F,-.001F,
                    0,0,-1, 221,17,255);
        if (shouldRenderFace(tile, Direction.SOUTH))
            quad(vertices, current.pose(), current.normal(), sprite,
                    .01F,.90F,1.001F, .01F,.10F,1.001F, .99F,.10F,1.001F, .99F,.90F,1.001F,
                    0,0,1, 221,17,255);
    }

    private static boolean shouldRenderFace(ArcaneLevitatorBlockEntity tile, Direction face) {
        var level = tile.getLevel();
        var pos = tile.getBlockPos();
        return level != null && Block.shouldRenderFace(
                tile.getBlockState(), level, pos, face, pos.relative(face));
    }

    private static void quad(VertexConsumer out, Matrix4f matrix, Matrix3f normal,
            TextureAtlasSprite sprite, float... v) {
        for (int i=0;i<4;i++) {
            float u = (i==0 || i==1) ? sprite.getU0() : sprite.getU1();
            float vv = (i==0 || i==3) ? sprite.getV0() : sprite.getV1();
            vertex(out,matrix,normal,v[i*3],v[i*3+1],v[i*3+2],u,vv,
                    (int)v[12],(int)v[13],(int)v[14],(int)v[15],(int)v[16],(int)v[17]);
        }
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Matrix3f normal,
            float x,float y,float z,float u,float v,int nx,int ny,int nz,int r,int g,int b) {
        out.vertex(matrix,x,y,z).color(r,g,b,208).uv(u,v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal,nx,ny,nz).endVertex();
    }
}
