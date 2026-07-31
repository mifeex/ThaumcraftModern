package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Modern target-seeking equivalent of TC4's black type-5 FXWisp.
 */
public final class EldritchHealParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    private EldritchHealParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double targetOffsetX,
            double targetOffsetY,
            double targetOffsetZ,
            SpriteSet sprites
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        targetX = x + targetOffsetX;
        targetY = y + targetOffsetY;
        targetZ = z + targetOffsetZ;
        lifetime = (int) (36.0D / (random.nextDouble() * 0.3D + 0.7D));
        gravity = 0.0F;
        hasPhysics = false;
        quadSize = 0.5F;
        alpha = 0.5F;
        rCol = random.nextFloat() * 0.1F;
        gCol = random.nextFloat() * 0.1F;
        bCol = random.nextFloat() * 0.1F;
        setSpriteFromAge(sprites);
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
        move(xd, yd, zd);
        xd *= 0.985D;
        yd *= 0.985D;
        zd *= 0.985D;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 1.0E-6D) {
            xd = Mth.clamp(xd + dx / distance * 0.2D, -0.2D, 0.2D);
            yd = Mth.clamp(yd + dy / distance * 0.2D, -0.2D, 0.2D);
            zd = Mth.clamp(zd + dz / distance * 0.2D, -0.2D, 0.2D);
        }
        quadSize *= 0.965F;
        setSpriteFromAge(sprites);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0x00F000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public EldritchHealParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new EldritchHealParticle(
                    level,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    sprites
            );
        }
    }
}
