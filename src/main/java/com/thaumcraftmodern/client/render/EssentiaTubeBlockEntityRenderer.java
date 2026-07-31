package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** TC4's six-pixel conduit overlap used by jars, alembics and other endpoints. */
public final class EssentiaTubeBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaTubeBlockEntity> {
    private static final ResourceLocation PIPE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/pipe_1.png");
    private static final ResourceLocation RESTRICTED = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/pipe_restrict.png");
    private static final ResourceLocation FILTER_CORE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/pipe_filter_core.png");
    private static final ResourceLocation VALVE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/pipe_valve.png");
    private static final ResourceLocation VALVE_MODEL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/model/valve.png");
    private static final float ARM_MIN = 7.0F / 16.0F;
    private static final float ARM_MAX = 9.0F / 16.0F;
    private static final float EXTENSION = 6.0F / 16.0F;
    private final ModelPart oneWayValveRod;

    public EssentiaTubeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        oneWayValveRod = createOneWayValveRod();
    }

    @Override
    public void render(EssentiaTubeBlockEntity tube, float partialTick,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (tube.getLevel() == null) return;
        ResourceLocation texture = tube.policy().restrictedSuction()
                ? RESTRICTED : PIPE;
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        for (Direction side : Direction.values()) {
            if (!tube.isConnectable(side)) continue;
            var remote = EssentiaConnections.neighbour(
                    tube.getLevel(), tube.getBlockPos(), side).orElse(null);
            if (remote == null || !remote.renderExtendedTube()) continue;
            float minX = ARM_MIN, minY = ARM_MIN, minZ = ARM_MIN;
            float maxX = ARM_MAX, maxY = ARM_MAX, maxZ = ARM_MAX;
            /*
             * The multipart block model already draws the arm from the tube
             * centre to this block's face. The BER owns only TC4's six-pixel
             * overlap inside the neighbouring endpoint. Repeating the whole
             * arm here produces coplanar faces, z-fighting and long seams.
             */
            switch (side) {
                case DOWN -> {
                    minY = -EXTENSION;
                    maxY = 0.0F;
                }
                case UP -> {
                    minY = 1.0F;
                    maxY = 1.0F + EXTENSION;
                }
                case NORTH -> {
                    minZ = -EXTENSION;
                    maxZ = 0.0F;
                }
                case SOUTH -> {
                    minZ = 1.0F;
                    maxZ = 1.0F + EXTENSION;
                }
                case WEST -> {
                    minX = -EXTENSION;
                    maxX = 0.0F;
                }
                case EAST -> {
                    minX = 1.0F;
                    maxX = 1.0F + EXTENSION;
                }
            }
            tubeCuboid(out, poses.last(), minX, minY, minZ, maxX, maxY, maxZ,
                    light, overlay);
        }
        if (tube.policy().directional()) {
            renderOneWayRings(tube, poses, buffers, light, overlay);
        }
        if (tube.policy().filtered()) {
            renderFilterCore(tube, poses, buffers, light, overlay);
        }
        if (tube.policy().redstoneValve()) {
            renderValve(tube, partialTick, poses, buffers, light, overlay);
        }
    }

    private static void renderFilterCore(EssentiaTubeBlockEntity tube,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        int color = tube.filter() == null ? 0xFFFFFF
                : AspectRegistryRuntime.find(tube.filter())
                        .map(AspectDefinition::color).orElse(0xFFFFFF);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float min = 5.48F / 16.0F;
        float max = 10.52F / 16.0F;
        cuboid(buffers.getBuffer(RenderType.entityCutoutNoCull(FILTER_CORE)),
                poses.last(), min, min, min, max, max, max,
                red, green, blue, light, overlay);
    }

    private static void renderValve(EssentiaTubeBlockEntity tube, float partialTick,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        // FACING is updated together with the block state and is therefore the
        // client render authority. Reading the BE field here can show the old
        // orientation until a chunk reload after a wand rotation.
        Direction side = tube.getBlockState().getValue(EssentiaTubeBlock.FACING);
        float x0 = 7.0F/16.0F, y0 = 7.0F/16.0F, z0 = 7.0F/16.0F;
        float x1 = 9.0F/16.0F, y1 = 9.0F/16.0F, z1 = 9.0F/16.0F;
        float rotation = tube.valveRotation(partialTick);
        float retraction = rotation / 360.0F * 0.12F;
        // TC4's rod occupies pixels 2..4. As the valve travels inward by
        // 0.12 blocks, this visible bridge collapses almost completely.
        float near = 2.0F/16.0F;
        float far = 4.0F/16.0F;
        float shaftFar = Math.max(near, far - retraction);
        switch (side) {
            case DOWN -> { y0 = 0.5F-shaftFar; y1 = 0.5F-near; }
            case UP -> { y0 = 0.5F+near; y1 = 0.5F+shaftFar; }
            case NORTH -> { z0 = 0.5F-shaftFar; z1 = 0.5F-near; }
            case SOUTH -> { z0 = 0.5F+near; z1 = 0.5F+shaftFar; }
            case WEST -> { x0 = 0.5F-shaftFar; x1 = 0.5F-near; }
            case EAST -> { x0 = 0.5F+near; x1 = 0.5F+shaftFar; }
        }
        tubeCuboid(buffers.getBuffer(RenderType.entityCutoutNoCull(PIPE)),
                poses.last(), x0,y0,z0,x1,y1,z1, light,overlay);

        float cx = 0.5F + side.getStepX() * (far - retraction);
        float cy = 0.5F + side.getStepY() * (far - retraction);
        float cz = 0.5F + side.getStepZ() * (far - retraction);
        float angle = (float)Math.toRadians(-rotation * 1.5F);
        float[] u = side.getAxis() == Direction.Axis.Y
                ? new float[]{1,0,0} : new float[]{0,1,0};
        float[] v = cross(side, u);
        rotateBasis(u, v, angle);
        float half = 4.0F/16.0F;
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(VALVE));
        extrudedWheel(out, poses.last(), cx,cy,cz,u,v,side,half,
                0.05F,light,overlay);
    }

    private static float[] cross(Direction normal, float[] u) {
        float nx=normal.getStepX(), ny=normal.getStepY(), nz=normal.getStepZ();
        return new float[]{ny*u[2]-nz*u[1], nz*u[0]-nx*u[2], nx*u[1]-ny*u[0]};
    }

    private static void rotateBasis(float[] u, float[] v, float angle) {
        float cos=(float)Math.cos(angle), sin=(float)Math.sin(angle);
        for (int i=0;i<3;i++) { float old=u[i]; u[i]=old*cos+v[i]*sin; v[i]=v[i]*cos-old*sin; }
    }

    /** Port of TC4 ExtrudedSpriteRenderHelper for an arbitrarily oriented wheel. */
    private static void extrudedWheel(VertexConsumer out, PoseStack.Pose pose,
            float cx,float cy,float cz,float[] u,float[] v,Direction face,
            float half,float thickness,int light,int overlay) {
        float[] n={face.getStepX(),face.getStepY(),face.getStepZ()};
        // The back face touches the pipe; extrusion grows out towards the tap.
        spriteQuad(out,pose,cx,cy,cz,u,v,n,half,thickness,
                -1,-1, 1,-1, 1,1, -1,1, 1,1,0,0,1,light,overlay);
        spriteQuad(out,pose,cx,cy,cz,u,v,n,half,0,
                -1,1, 1,1, 1,-1, -1,-1, 1,0,0,1,-1,light,overlay);

        float pixel=1.0F/16.0F;
        for(int i=0;i<16;i++) {
            float a=-1.0F+2.0F*i/16.0F;
            float b=-1.0F+2.0F*(i+1)/16.0F;
            float sample=1.0F-(i+0.5F)*pixel;
            sideQuad(out,pose,cx,cy,cz,u,v,n,half,thickness,a,-1,a,1,
                    sample,1,sample,0,-u[0],-u[1],-u[2],light,overlay);
            sideQuad(out,pose,cx,cy,cz,u,v,n,half,thickness,b,1,b,-1,
                    sample,0,sample,1,u[0],u[1],u[2],light,overlay);
            sideQuad(out,pose,cx,cy,cz,v,u,n,half,thickness,a,-1,a,1,
                    1,sample,0,sample,-v[0],-v[1],-v[2],light,overlay);
            sideQuad(out,pose,cx,cy,cz,v,u,n,half,thickness,b,1,b,-1,
                    0,sample,1,sample,v[0],v[1],v[2],light,overlay);
        }
    }

    private static void spriteQuad(VertexConsumer out,PoseStack.Pose pose,
            float cx,float cy,float cz,float[] u,float[] v,float[] n,float half,float depth,
            float ua,float va,float ub,float vb,float uc,float vc,float ud,float vd,
            float u0,float v0,float u1,float v1,float normalSign,int light,int overlay) {
        float nx=n[0]*normalSign,ny=n[1]*normalSign,nz=n[2]*normalSign;
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ua,va,depth,u0,v0,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ub,vb,depth,u1,v0,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,uc,vc,depth,u1,v1,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ud,vd,depth,u0,v1,nx,ny,nz,light,overlay);
    }

    private static void sideQuad(VertexConsumer out,PoseStack.Pose pose,
            float cx,float cy,float cz,float[] u,float[] v,float[] n,float half,float thickness,
            float ua,float va,float ub,float vb,float u0,float v0,float u1,float v1,
            float nx,float ny,float nz,int light,int overlay) {
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ua,va,0,u0,v0,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ua,va,thickness,u0,v0,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ub,vb,thickness,u1,v1,nx,ny,nz,light,overlay);
        spriteVertex(out,pose,cx,cy,cz,u,v,n,half,ub,vb,0,u1,v1,nx,ny,nz,light,overlay);
    }

    private static void spriteVertex(VertexConsumer out,PoseStack.Pose pose,
            float cx,float cy,float cz,float[] u,float[] v,float[] n,float half,
            float su,float sv,float depth,float texU,float texV,
            float nx,float ny,float nz,int light,int overlay) {
        vertex(out,pose,cx+u[0]*half*su+v[0]*half*sv+n[0]*depth,
                cy+u[1]*half*su+v[1]*half*sv+n[1]*depth,
                cz+u[2]*half*su+v[2]*half*sv+n[2]*depth,
                texU,texV,nx,ny,nz,1,1,1,light,overlay);
    }

    private void renderOneWayRings(EssentiaTubeBlockEntity tube,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        // The block state is the client-synchronised render authority. Reading
        // the BE field here leaves an already placed vertical tube stuck on
        // its old default NORTH axis until the chunk is reloaded.
        Direction face = tube.getBlockState().getValue(EssentiaTubeBlock.FACING);
        if (EssentiaConnections.neighbour(
                tube.getLevel(), tube.getBlockPos(), face.getOpposite()
        ).isEmpty()) {
            return;
        }
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        if (face.getStepY() == 0) {
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        } else {
            poses.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poses.mulPose(Axis.XP.rotationDegrees(90.0F * face.getStepY()));
        }
        poses.mulPose(new Quaternionf().rotationAxis(
                (float) (Math.PI / 2.0D),
                face.getStepX(), face.getStepY(), face.getStepZ()
        ));
        poses.scale(1.1F, 0.5F, 1.1F);
        poses.translate(0.0D, -0.5D, 0.0D);
        VertexConsumer out = buffers.getBuffer(
                RenderType.entityCutoutNoCull(VALVE_MODEL)
        );
        for (int ring = 0; ring < 3; ring++) {
            oneWayValveRod.render(
                    poses,
                    out,
                    light,
                    overlay,
                    0.45F,
                    0.5F,
                    1.0F,
                    1.0F
            );
            poses.translate(0.0D, -0.25D, 0.0D);
        }
        poses.popPose();
    }

    private static ModelPart createOneWayValveRod() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
                "valve_rod",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .mirror()
                        .addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 32)
                .bakeRoot()
                .getChild("valve_rod");
    }

    @Override
    public boolean shouldRenderOffScreen(EssentiaTubeBlockEntity tube) {
        return true;
    }

    private static void cuboid(VertexConsumer out, PoseStack.Pose pose,
            float x0, float y0, float z0, float x1, float y1, float z1,
            int light, int overlay) {
        cuboid(out, pose, x0,y0,z0,x1,y1,z1, 1,1,1,light,overlay);
    }

    /** Matches TC4 TubeConduitRenderHelper's coordinate-derived pipe UVs. */
    private static void tubeCuboid(VertexConsumer out, PoseStack.Pose pose,
            float x0,float y0,float z0,float x1,float y1,float z1,
            int light,int overlay) {
        cuboid(out, pose, x0,y0,z0,x1,y1,z1, 1,1,1,light,overlay);
    }

    private static void cuboidUv(VertexConsumer out, PoseStack.Pose pose,
            float x0,float y0,float z0,float x1,float y1,float z1,
            float u0,float v0,float u1,float v1,int light,int overlay) {
        quadUv(out,pose,x0,y0,z1,x1,y0,z1,x1,y0,z0,x0,y0,z0,0,-1,0,u0,v0,u1,v1,light,overlay);
        quadUv(out,pose,x0,y1,z0,x1,y1,z0,x1,y1,z1,x0,y1,z1,0,1,0,u0,v0,u1,v1,light,overlay);
        quadUv(out,pose,x0,y0,z0,x1,y0,z0,x1,y1,z0,x0,y1,z0,0,0,-1,u0,v0,u1,v1,light,overlay);
        quadUv(out,pose,x1,y0,z1,x0,y0,z1,x0,y1,z1,x1,y1,z1,0,0,1,u0,v0,u1,v1,light,overlay);
        quadUv(out,pose,x0,y0,z1,x0,y0,z0,x0,y1,z0,x0,y1,z1,-1,0,0,u0,v0,u1,v1,light,overlay);
        quadUv(out,pose,x1,y0,z0,x1,y0,z1,x1,y1,z1,x1,y1,z0,1,0,0,u0,v0,u1,v1,light,overlay);
    }

    private static void quadUv(VertexConsumer out, PoseStack.Pose pose,
            float x0,float y0,float z0,float x1,float y1,float z1,
            float x2,float y2,float z2,float x3,float y3,float z3,
            float nx,float ny,float nz,float u0,float v0,float u1,float v1,
            int light,int overlay) {
        vertex(out,pose,x0,y0,z0,u0,v1,nx,ny,nz,1,1,1,light,overlay);
        vertex(out,pose,x1,y1,z1,u1,v1,nx,ny,nz,1,1,1,light,overlay);
        vertex(out,pose,x2,y2,z2,u1,v0,nx,ny,nz,1,1,1,light,overlay);
        vertex(out,pose,x3,y3,z3,u0,v0,nx,ny,nz,1,1,1,light,overlay);
    }

    private static void cuboid(VertexConsumer out, PoseStack.Pose pose,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float red, float green, float blue, int light, int overlay) {
        boolean xOutside = x0 < 0.0F || x1 > 1.0F;
        boolean yOutside = y0 < 0.0F || y1 > 1.0F;
        boolean zOutside = z0 < 0.0F || z1 > 1.0F;
        float uMinX = xOutside ? 0.0F : x0;
        float uMaxX = xOutside ? 1.0F : x1;
        float uMinZ = zOutside ? 0.0F : z0;
        float uMaxZ = zOutside ? 1.0F : z1;
        float uNorthAtMinX = xOutside ? 1.0F : 1.0F - x0;
        float uNorthAtMaxX = xOutside ? 0.0F : 1.0F - x1;
        float uEastAtMinZ = zOutside ? 1.0F : 1.0F - z0;
        float uEastAtMaxZ = zOutside ? 0.0F : 1.0F - z1;
        float vDownAtMaxZ = zOutside ? 1.0F : z1;
        float vDownAtMinZ = zOutside ? 0.0F : z0;
        float vUpMin = zOutside ? 0.0F : z0;
        float vUpMax = zOutside ? 1.0F : z1;
        float vSideMin = yOutside ? 0.0F : 1.0F - y1;
        float vSideMax = yOutside ? 1.0F : 1.0F - y0;

        vertex(out,pose,x0,y0,z1,uMinX,vDownAtMaxZ,0,-1,0,red,green,blue,light,overlay);
        vertex(out,pose,x0,y0,z0,uMinX,vDownAtMinZ,0,-1,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z0,uMaxX,vDownAtMinZ,0,-1,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z1,uMaxX,vDownAtMaxZ,0,-1,0,red,green,blue,light,overlay);

        vertex(out,pose,x0,y1,z0,uMinX,vUpMin,0,1,0,red,green,blue,light,overlay);
        vertex(out,pose,x0,y1,z1,uMinX,vUpMax,0,1,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y1,z1,uMaxX,vUpMax,0,1,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y1,z0,uMaxX,vUpMin,0,1,0,red,green,blue,light,overlay);

        vertex(out,pose,x0,y1,z0,uNorthAtMinX,vSideMin,0,0,-1,red,green,blue,light,overlay);
        vertex(out,pose,x1,y1,z0,uNorthAtMaxX,vSideMin,0,0,-1,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z0,uNorthAtMaxX,vSideMax,0,0,-1,red,green,blue,light,overlay);
        vertex(out,pose,x0,y0,z0,uNorthAtMinX,vSideMax,0,0,-1,red,green,blue,light,overlay);

        vertex(out,pose,x0,y1,z1,uMinX,vSideMin,0,0,1,red,green,blue,light,overlay);
        vertex(out,pose,x0,y0,z1,uMinX,vSideMax,0,0,1,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z1,uMaxX,vSideMax,0,0,1,red,green,blue,light,overlay);
        vertex(out,pose,x1,y1,z1,uMaxX,vSideMin,0,0,1,red,green,blue,light,overlay);

        vertex(out,pose,x0,y1,z1,uMaxZ,vSideMin,-1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x0,y1,z0,uMinZ,vSideMin,-1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x0,y0,z0,uMinZ,vSideMax,-1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x0,y0,z1,uMaxZ,vSideMax,-1,0,0,red,green,blue,light,overlay);

        vertex(out,pose,x1,y1,z0,uEastAtMinZ,vSideMin,1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y1,z1,uEastAtMaxZ,vSideMin,1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z1,uEastAtMaxZ,vSideMax,1,0,0,red,green,blue,light,overlay);
        vertex(out,pose,x1,y0,z0,uEastAtMinZ,vSideMax,1,0,0,red,green,blue,light,overlay);
    }

    private static void quad(VertexConsumer out, PoseStack.Pose pose,
            float x0,float y0,float z0, float x1,float y1,float z1,
            float x2,float y2,float z2, float x3,float y3,float z3,
            float nx,float ny,float nz, int light,int overlay) {
        quad(out,pose,x0,y0,z0,x1,y1,z1,x2,y2,z2,x3,y3,z3,nx,ny,nz,1,1,1,light,overlay);
    }

    private static void quad(VertexConsumer out, PoseStack.Pose pose,
            float x0,float y0,float z0, float x1,float y1,float z1,
            float x2,float y2,float z2, float x3,float y3,float z3,
            float nx,float ny,float nz,float red,float green,float blue,int light,int overlay) {
        vertex(out, pose, x0,y0,z0, 0,1,nx,ny,nz,red,green,blue,light,overlay);
        vertex(out, pose, x1,y1,z1, 1,1,nx,ny,nz,red,green,blue,light,overlay);
        vertex(out, pose, x2,y2,z2, 1,0,nx,ny,nz,red,green,blue,light,overlay);
        vertex(out, pose, x3,y3,z3, 0,0,nx,ny,nz,red,green,blue,light,overlay);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose,
            float x,float y,float z,float u,float v,float nx,float ny,float nz,
            float red,float green,float blue,int light,int overlay) {
        Matrix4f matrix = pose.pose(); Matrix3f normal = pose.normal();
        out.vertex(matrix,x,y,z).color(red,green,blue,1.0F).uv(u,v)
                .overlayCoords(overlay).uv2(light).normal(normal,nx,ny,nz).endVertex();
    }
}
