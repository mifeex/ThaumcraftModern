package com.thaumcraftmodern.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** TC4 blockSparkle color 32768 and FXGeneric frames 112..120. */
public final class TravelSparkleParticle extends TextureSheetParticle {
    private static final float BASE_RED = 0.0F;
    private static final float BASE_GREEN = 128.0F / 255.0F;
    private static final float BASE_BLUE = 0.0F;
    private static final float COLOR_VARIANCE = 0.2F;
    private static final float ALPHA = 0.9F;
    private final SpriteSet sprites;
    private final int delay;

    private TravelSparkleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        hasPhysics = false;
        gravity = 0.0F;
        xd = xSpeed;
        yd = ySpeed;
        zd = zSpeed;
        delay = random.nextInt(10);
        lifetime = 5 + random.nextInt(8) + delay;
        quadSize = (0.7F + random.nextFloat() * 0.4F) * 0.1F;
        rCol = varied(BASE_RED);
        gCol = varied(BASE_GREEN);
        bCol = varied(BASE_BLUE);
        alpha = delay == 0 ? ALPHA * 0.5F : 0.0F;
        setSpriteFromAge(sprites);
    }

    private float varied(float base) {
        return Mth.clamp(
                base - COLOR_VARIANCE
                        + random.nextFloat() * COLOR_VARIANCE * 2.0F,
                0.0F,
                1.0F
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }
        setSpriteFromAge(sprites);
        if (age < delay) {
            alpha = 0.0F;
        } else {
            alpha = age >= lifetime - 1 ? ALPHA * 0.5F : ALPHA;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TravelSparkleParticleRenderType.INSTANCE;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public TravelSparkleParticle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new TravelSparkleParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, sprites
            );
        }
    }
}
