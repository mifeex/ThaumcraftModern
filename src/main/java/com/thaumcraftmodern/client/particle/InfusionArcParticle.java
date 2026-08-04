package com.thaumcraftmodern.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

import java.awt.Color;

/** Exact motion and atlas frames of TC4 FXEssentiaTrail and FXBoreSparkle. */
public final class InfusionArcParticle extends SingleQuadParticle {
    /** TC4 renders these particles at 0.1 * particleScale. SingleQuadParticle's
     * quadSize is already the rendered size, so no additional 1/7 conversion
     * belongs here. The old conversion made an essentia mote smaller than its
     * own 0.02 expiry threshold and it vanished on its first client tick. */
    private static final float TC4_RENDER_SCALE = 0.1F;
    public enum Kind {
        ESSENTIA,
        BORE_SPARKLE
    }

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int sequence;
    private final Kind kind;

    private InfusionArcParticle(ClientLevel level, double x, double y, double z,
            double targetX, double targetY, double targetZ, int sequence,
            int color, float scale, Kind kind) {
        super(level, x, y, z);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.sequence = sequence;
        this.kind = kind;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        int distanceTicks = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz)
                * (kind == Kind.ESSENTIA ? 30.0D : 3.0D)));
        lifetime = distanceTicks / 2 + random.nextInt(distanceTicks);
        hasPhysics = true;
        gravity = 0.2F;
        if (kind == Kind.ESSENTIA) {
            quadSize = TC4_RENDER_SCALE
                    * (Mth.sin(sequence / 2.0F) * 0.1F + 1.0F)
                    * Math.max(0.15F, scale);
            xd = Mth.sin(sequence / 4.0F) * 0.015F + random.nextGaussian() * 0.002D;
            yd = 0.1F + Mth.sin(sequence / 3.0F) * 0.01F;
            zd = Mth.sin(sequence / 2.0F) * 0.015F + random.nextGaussian() * 0.002D;
            Color tint = new Color(color);
            rCol = varied(tint.getRed());
            gCol = varied(tint.getGreen());
            bCol = varied(tint.getBlue());
            alpha = 0.5F;
        } else {
            quadSize = TC4_RENDER_SCALE
                    * (random.nextFloat() * 0.5F + 0.5F);
            xd = random.nextGaussian() * 0.01D;
            yd = random.nextGaussian() * 0.01D;
            zd = random.nextGaussian() * 0.01D;
            rCol = 0.4F + random.nextFloat() * 0.2F;
            gCol = 0.2F;
            bCol = 0.6F + random.nextFloat() * 0.3F;
            alpha = 1.0F;
        }
    }

    public static InfusionArcParticle essentia(ClientLevel level,
            double x, double y, double z, double tx, double ty, double tz,
            int sequence, int color, float scale) {
        return new InfusionArcParticle(level, x, y, z, tx, ty, tz,
                sequence, color, scale, Kind.ESSENTIA);
    }

    public static InfusionArcParticle boreSparkle(ClientLevel level,
            double x, double y, double z, double tx, double ty, double tz) {
        return new InfusionArcParticle(level, x, y, z, tx, ty, tz,
                0, 0, 1.0F, Kind.BORE_SPARKLE);
    }

    private float varied(int component) {
        float base = component / 255.0F;
        float variance = base * 0.2F;
        return base - variance + random.nextFloat() * variance;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime || reachedTargetBlock()) {
            remove();
            return;
        }
        if (kind == Kind.ESSENTIA) yd += 0.01D * gravity;
        move(xd, yd, zd);
        xd *= 0.985D;
        yd *= 0.985D;
        zd *= 0.985D;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (kind == Kind.ESSENTIA) {
            xd = Mth.clamp(xd, -0.05D, 0.05D);
            yd = Mth.clamp(yd, -0.05D, 0.05D);
            zd = Mth.clamp(zd, -0.05D, 0.05D);
            if (distance < 2.0D) quadSize *= 0.98F;
            if (quadSize < 0.02F) {
                remove();
                return;
            }
            accelerate(dx, dy, dz, distance, 0.01D / Math.min(1.0D, distance));
        } else {
            double acceleration = distance < 4.0D ? 0.6D : 0.3D;
            if (distance < 4.0D) quadSize *= 0.9F;
            accelerate(dx, dy, dz, distance, acceleration);
            xd = Mth.clamp(xd, -0.35D, 0.35D);
            yd = Mth.clamp(yd, -0.35D, 0.35D);
            zd = Mth.clamp(zd, -0.35D, 0.35D);
        }
    }

    private void accelerate(double dx, double dy, double dz, double distance,
            double acceleration) {
        if (distance <= 1.0E-6D) return;
        xd += dx / distance * acceleration;
        yd += dy / distance * acceleration;
        zd += dz / distance * acceleration;
    }

    private boolean reachedTargetBlock() {
        return Mth.floor(x) == Mth.floor(targetX)
                && Mth.floor(y) == Mth.floor(targetY)
                && Mth.floor(z) == Mth.floor(targetZ);
    }

    @Override
    public float getQuadSize(float partialTick) {
        if (kind == Kind.BORE_SPARKLE) {
            return quadSize * (Mth.sin(age / 3.0F) * 0.5F + 1.0F);
        }
        return quadSize * (Mth.sin((age - sequence) / 5.0F) * 0.25F + 1.0F);
    }

    @Override protected float getU0() {
        return kind == Kind.ESSENTIA ? 0.5625F : (age % 4) / 16.0F;
    }
    @Override protected float getU1() { return getU0() + 0.0624375F; }
    @Override protected float getV0() { return kind == Kind.ESSENTIA ? 0.0625F : 0.25F; }
    @Override protected float getV1() { return getV0() + 0.0624375F; }
    @Override protected int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }
    @Override public ParticleRenderType getRenderType() { return InfusionParticleRenderType.INSTANCE; }
}
