package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Modern renderer for TC4 {@code FXBubble}. It uses the original frames
 * 16-18 from {@code textures/misc/particles.png}.
 */
public final class CrucibleBubbleParticle extends TextureSheetParticle {
    private static final int LAST_FRAME = 2;

    private final SpriteSet sprites;
    private double bubbleAcceleration;

    private CrucibleBubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double firstParameter,
            double secondParameter,
            double thirdParameter,
            SpriteSet sprites,
            boolean froth
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        hasPhysics = false;
        quadSize = 0.1F * (random.nextFloat() * 0.3F + 0.2F);
        xd = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        yd = random.nextFloat() * 0.02F;
        zd = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;

        if (froth) {
            boolean down = secondParameter < 0.0D;
            rCol = 0.5F;
            gCol = 0.5F;
            bCol = 0.7F;
            quadSize *= 0.75F;
            lifetime = down
                    ? 12 + random.nextInt(12)
                    : 4 + random.nextInt(3);
            bubbleAcceleration = down ? -0.005D : -0.001D;
            xd /= 5.0D;
            yd /= 10.0D;
            zd /= 5.0D;
        } else {
            rCol = (float) firstParameter;
            gCol = (float) secondParameter;
            bCol = (float) thirdParameter;
            lifetime = (int) (3.0D + 8.0D
                    / (random.nextDouble() * 0.8D + 0.2D));
            bubbleAcceleration = 0.002D;
        }
        setClassicFrame();
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

        yd += bubbleAcceleration;
        if (bubbleAcceleration > 0.0D) {
            xd += (random.nextFloat() - random.nextFloat()) * 0.01F;
            zd += (random.nextFloat() - random.nextFloat()) * 0.01F;
        }
        move(xd, yd, zd);
        xd *= 0.85D;
        yd *= 0.85D;
        zd *= 0.85D;
        setClassicFrame();
    }

    private void setClassicFrame() {
        int remaining = lifetime - age;
        int frame = remaining <= 1
                ? LAST_FRAME
                : remaining == 2 ? LAST_FRAME - 1 : 0;
        setSprite(sprites.get(frame, LAST_FRAME));
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
        private final boolean froth;

        public Provider(SpriteSet sprites, boolean froth) {
            this.sprites = sprites;
            this.froth = froth;
        }

        @Override
        public CrucibleBubbleParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double firstParameter,
                double secondParameter,
                double thirdParameter
        ) {
            return new CrucibleBubbleParticle(
                    level,
                    x,
                    y,
                    z,
                    firstParameter,
                    secondParameter,
                    thirdParameter,
                    sprites,
                    froth
            );
        }
    }
}
