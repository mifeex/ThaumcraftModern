package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Verbatim vertex, UV, normal and triangle data from TC4 ModelEldritchCap. */
public final class ClassicFluxScrubberModel {
    private static final float[][] V={
            {-.5f,-.5f,0},{-.5f,.5f,0},{.5f,.5f,0},{.5f,-.5f,0},
            {-.25f,-.25f,.8f},{.25f,-.25f,.8f},{.25f,.25f,.8f},{-.25f,.25f,.8f},
            {0,-.375f,.4f},{.375f,0,.4f},{0,.375f,.4f},{-.375f,0,.4f},
            {-.125f,-.125f,.8f},{-.125f,.125f,.8f},{.125f,.125f,.8f},{.125f,-.125f,.8f},
            {-.125f,-.125f,1},{.125f,-.125f,1},{.125f,.125f,1},{-.125f,.125f,1}};
    private static final float[][] UV={{0,.5f},{0,0},{.5f,0},{.5f,.5f},{0,.75f},{.25f,.75f},{.25f,1},{0,1},{1,.5f},{.75f,.75f},{1,1},{.5f,1},{.8438f,.0313f},{.8438f,.2188f},{.6563f,.2188f},{.6563f,.0313f},{.8438f,.3438f},{.6563f,.3438f}};
    private static final float[][] N={{0,0,-1},{0,0,1},{0,-.9545f,.2983f},{.9545f,0,.2983f},{0,.9545f,.2983f},{-.9545f,0,.2983f},{0,0,-1},{0,0,1},{0,-1,0},{1,0,0},{0,1,0},{-1,0,0}};
    private static final int[][] CAP={{1,1,1,2,2,1,3,3,1},{3,3,1,4,4,1,1,1,1},{5,5,2,6,6,2,7,7,2},{7,7,2,8,8,2,5,5,2},{1,4,3,4,9,3,9,10,3},{4,9,3,6,11,3,9,10,3},{6,11,3,5,12,3,9,10,3},{5,12,3,1,4,3,9,10,3},{4,4,4,3,9,4,10,10,4},{3,9,4,7,11,4,10,10,4},{7,11,4,6,12,4,10,10,4},{6,12,4,4,4,4,10,10,4},{3,4,5,2,9,5,11,10,5},{2,9,5,8,11,5,11,10,5},{8,11,5,7,12,5,11,10,5},{7,12,5,3,4,5,11,10,5},{2,4,6,1,9,6,12,10,6},{1,9,6,5,11,6,12,10,6},{5,11,6,8,12,6,12,10,6},{8,12,6,2,4,6,12,10,6}};
    private static final int[][] TIP={{13,13,7,14,14,7,15,15,7},{15,15,7,16,16,7,13,13,7},{17,16,8,18,13,8,19,14,8},{19,14,8,20,15,8,17,16,8},{13,15,9,16,14,9,18,17,9},{18,17,9,17,18,9,13,15,9},{16,15,10,15,14,10,19,17,10},{19,17,10,18,18,10,16,15,10},{15,15,11,14,14,11,20,17,11},{20,17,11,19,18,11,15,15,11},{14,15,12,13,14,12,17,17,12},{17,17,12,20,18,12,14,15,12}};
    public void renderCap(PoseStack poses, VertexConsumer out,int light,int overlay){ render(CAP,poses,out,light,overlay); }
    public void renderTip(PoseStack poses, VertexConsumer out,int light,int overlay){ render(TIP,poses,out,light,overlay); }
    private static void render(int[][] tris,PoseStack poses,VertexConsumer out,int light,int overlay){
        PoseStack.Pose pose=poses.last(); Matrix4f matrix=pose.pose(); Matrix3f normal=pose.normal();
        for(int[] tri:tris) for(int o=0;o<9;o+=3){ float[] p=V[tri[o]-1],uv=UV[tri[o+1]-1],n=N[tri[o+2]-1];
            out.vertex(matrix,p[0],p[1],p[2]).color(255,255,255,255).uv(uv[0],1-uv[1]).overlayCoords(overlay).uv2(light).normal(normal,n[0],n[1],n[2]).endVertex(); }
    }
}
