package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * TC4 {@code FXBurst}: a stationary 31-frame full-bright node-atlas burst.
 */
public final class NodeBurstParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private NodeBurstParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet sprites
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        lifetime = 31;
        gravity = 0.0F;
        hasPhysics = false;
        quadSize = 1.8F;
        alpha = 0.75F;
        rCol = 1.0F;
        gCol = 1.0F;
        bCol = 1.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0x00F000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public NodeBurstParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new NodeBurstParticle(level, x, y, z, sprites);
        }
    }
}
