package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Modern adapter for the two {@code FXWisp} groups emitted by TC4 Nitor. */
public final class NitorWispParticle extends TextureSheetParticle {
    private static final float LARGE_SIZE = 0.25F;
    private static final float SMALL_SIZE = 0.125F;
    private static final float LARGE_GRAVITY = -0.025F;
    private static final float SMALL_GRAVITY = -0.02F;
    private final float originalSize;

    private NitorWispParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double targetX,
            double targetY,
            double targetZ,
            SpriteSet sprites,
            boolean large
    ) {
        super(level, x, y, z);
        lifetime = (int) (36.0D / (random.nextDouble() * 0.3D + 0.7D));
        originalSize = large ? LARGE_SIZE : SMALL_SIZE;
        quadSize = originalSize;
        gravity = large ? LARGE_GRAVITY : SMALL_GRAVITY;
        alpha = 0.5F;
        hasPhysics = true;
        setSize(0.1F, 0.1F);
        xd = (targetX - x) / lifetime;
        yd = (targetY - y) / lifetime;
        zd = (targetZ - z) / lifetime;
        if (large) {
            rCol = 0.7F + random.nextFloat() * 0.3F;
            gCol = 0.2F;
            bCol = 0.2F;
        } else {
            rCol = 0.5F + random.nextFloat() * 0.3F;
            gCol = 0.5F + random.nextFloat() * 0.3F;
            bCol = 0.2F;
        }
        pickSprite(sprites);
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
        yd -= 0.04D * gravity;
        move(xd, yd, zd);
        xd *= 0.98D;
        yd *= 0.98D;
        zd *= 0.98D;
        if (onGround) {
            xd *= 0.7D;
            zd *= 0.7D;
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float remaining = (lifetime - (age + partialTick)) / lifetime;
        return originalSize * Math.max(0.0F, remaining);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0x00F000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return NitorParticleRenderType.INSTANCE;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final boolean large;

        public Provider(SpriteSet sprites, boolean large) {
            this.sprites = sprites;
            this.large = large;
        }

        @Override
        public NitorWispParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double targetX,
                double targetY,
                double targetZ
        ) {
            return new NitorWispParticle(
                    level,
                    x,
                    y,
                    z,
                    targetX,
                    targetY,
                    targetZ,
                    sprites,
                    large
            );
        }
    }
}
