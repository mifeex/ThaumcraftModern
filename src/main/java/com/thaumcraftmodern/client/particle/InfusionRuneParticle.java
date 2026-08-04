package com.thaumcraftmodern.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** TC4 FXBlockRunes timing, colors and one of the sixteen rune cells. */
public final class InfusionRuneParticle extends SingleQuadParticle {
    private static final float MODERN_QUAD_SCALE = 1.0F / 7.0F;
    private final int rune;
    private final int faceRotation;
    private final double faceOffsetX;
    private final double faceOffsetY;

    public InfusionRuneParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        rune = random.nextInt(16);
        faceRotation = random.nextInt(4) * 90;
        faceOffsetX = random.nextFloat() * 0.2D;
        faceOffsetY = -0.3D + random.nextFloat() * 0.6D;
        rCol = 0.5F + random.nextFloat() * 0.2F;
        gCol = 0.1F;
        bCol = 0.7F + random.nextFloat() * 0.3F;
        lifetime = 75;
        gravity = -0.03F;
        hasPhysics = false;
        quadSize = MODERN_QUAD_SCALE * 0.3F
                * (float) (1.0D + random.nextGaussian() * 0.1D);
        roll = random.nextInt(4) * ((float) Math.PI / 2.0F);
        oRoll = roll;
        alpha = 0.0F;
    }

    @Override
    public void render(VertexConsumer vertices, Camera camera, float partialTick) {
        Vec3 cameraPosition = camera.getPosition();
        double px = Mth.lerp(partialTick, xo, x) - cameraPosition.x;
        double py = Mth.lerp(partialTick, yo, y) - cameraPosition.y;
        double pz = Mth.lerp(partialTick, zo, z) - cameraPosition.z;
        float size = quadSize;
        addVertex(vertices, px, py, pz, -0.5D * size, 0.5D * size,
                getU1(), getV1());
        addVertex(vertices, px, py, pz, 0.5D * size, 0.5D * size,
                getU1(), getV0());
        addVertex(vertices, px, py, pz, 0.5D * size, -0.5D * size,
                getU0(), getV0());
        addVertex(vertices, px, py, pz, -0.5D * size, -0.5D * size,
                getU0(), getV1());
    }

    private void addVertex(VertexConsumer vertices, double px, double py,
            double pz, double localX, double localY, float u, float v) {
        double tx = localX + faceOffsetX;
        double ty = localY + faceOffsetY;
        double tz = -0.51D;
        double rotatedZX = -ty;
        double rotatedZY = tx;
        float angle = faceRotation * ((float) Math.PI / 180.0F);
        double sin = Mth.sin(angle);
        double cos = Mth.cos(angle);
        double rx = rotatedZX * cos + tz * sin;
        double rz = tz * cos - rotatedZX * sin;
        vertices.vertex(px + rx, py + rotatedZY, pz + rz)
                .uv(u, v).color(rCol, gCol, bCol, alpha)
                .uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        float fadeIn = lifetime / 5.0F;
        alpha = (age <= fadeIn ? age / fadeIn
                : (lifetime - age) / (float) lifetime) * 0.5F;
        yd -= 0.04D * gravity;
        move(xd, yd, zd);
    }

    @Override protected float getU0() { return rune / 16.0F; }
    @Override protected float getU1() { return getU0() + 0.0624375F; }
    @Override protected float getV0() { return 0.375F; }
    @Override protected float getV1() { return getV0() + 0.0624375F; }
    @Override protected int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }
    @Override public ParticleRenderType getRenderType() { return InfusionParticleRenderType.INSTANCE; }
}
