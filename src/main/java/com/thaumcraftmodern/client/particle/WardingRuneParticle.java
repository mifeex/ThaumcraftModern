package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

/** Colored rune indicator used by the TC4 paving stone of warding. */
public final class WardingRuneParticle extends TextureSheetParticle {
    public enum State {
        ACTIVE,
        DISABLED,
        BLOCKED
    }

    private WardingRuneParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet sprites,
            State state
    ) {
        super(level, x, y, z);
        hasPhysics = false;
        xd = 0.0D;
        yd = 0.0D;
        zd = 0.0D;
        roll = random.nextInt(4) * ((float) Math.PI / 2.0F);
        oRoll = roll;
        quadSize = 0.15F * (float) (1.0D + random.nextGaussian() * 0.1D);
        alpha = 0.0F;
        switch (state) {
            case ACTIVE -> {
                rCol = 0.6F + random.nextFloat() * 0.4F;
                gCol = 0.0F;
                bCol = 0.3F + random.nextFloat() * 0.7F;
                lifetime = 60;
                gravity = 0.0F;
            }
            case DISABLED -> {
                rCol = 0.2F + random.nextFloat() * 0.4F;
                gCol = random.nextFloat() * 0.3F;
                bCol = 0.8F + random.nextFloat() * 0.2F;
                lifetime = 60;
                gravity = -0.02F;
            }
            case BLOCKED -> {
                rCol = 0.9F + random.nextFloat() * 0.1F;
                gCol = random.nextFloat() * 0.3F;
                bCol = random.nextFloat() * 0.3F;
                lifetime = 72;
                gravity = -0.02F;
            }
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
        float fadeIn = lifetime / 5.0F;
        alpha = (age <= fadeIn
                ? age / fadeIn
                : (lifetime - age) / (float) lifetime) * 0.5F;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final State state;

        public Provider(SpriteSet sprites, State state) {
            this.sprites = sprites;
            this.state = state;
        }

        @Override
        public WardingRuneParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new WardingRuneParticle(level, x, y, z, sprites, state);
        }
    }
}
