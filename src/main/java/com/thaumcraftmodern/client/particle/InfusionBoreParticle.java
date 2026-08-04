package com.thaumcraftmodern.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** TC4 FXBoreParticles using the actual block/item particle atlas sprite. */
public final class InfusionBoreParticle extends TextureSheetParticle {
    private static final float MODERN_QUAD_SCALE = 1.0F / 7.0F;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float uo;
    private final float vo;

    private InfusionBoreParticle(ClientLevel level, double x, double y, double z,
            double targetX, double targetY, double targetZ) {
        super(level, x, y, z);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        uo = random.nextFloat() * 3.0F;
        vo = random.nextFloat() * 3.0F;
        quadSize = (random.nextFloat() * 0.3F + 0.4F)
                * MODERN_QUAD_SCALE;
        xd = random.nextGaussian() * 0.01D;
        yd = random.nextGaussian() * 0.01D;
        zd = random.nextGaussian() * 0.01D;
        gravity = 0.2F;
        hasPhysics = true;
        rCol = 0.6F;
        gCol = 0.6F;
        bCol = 0.6F;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0D));
        lifetime = base / 2 + random.nextInt(base);
    }

    public static InfusionBoreParticle forBlock(ClientLevel level,
            double x, double y, double z, double tx, double ty, double tz,
            BlockState state, BlockPos source) {
        InfusionBoreParticle particle = new InfusionBoreParticle(level,
                x, y, z, tx, ty, tz);
        Minecraft minecraft = Minecraft.getInstance();
        particle.setSprite(minecraft.getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(state));
        int color = minecraft.getBlockColors().getColor(state, level, source, 0);
        particle.tint(color);
        return particle;
    }

    public static InfusionBoreParticle forItem(ClientLevel level,
            double x, double y, double z, double tx, double ty, double tz,
            ItemStack stack) {
        InfusionBoreParticle particle = new InfusionBoreParticle(level,
                x, y, z, tx, ty, tz);
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, level,
                null, 0);
        particle.setSprite(model.getParticleIcon());
        particle.tint(minecraft.getItemColors().getColor(stack, 0));
        return particle;
    }

    private void tint(int color) {
        if (color == -1) return;
        rCol *= (color >> 16 & 255) / 255.0F;
        gCol *= (color >> 8 & 255) / 255.0F;
        bCol *= (color & 255) / 255.0F;
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
        move(xd, yd, zd);
        xd *= 0.985D;
        yd *= 0.985D;
        zd *= 0.985D;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double acceleration = distance < 4.0D ? 0.6D : 0.3D;
        if (distance < 4.0D) quadSize *= 0.9F;
        if (distance > 1.0E-6D) {
            xd += dx / distance * acceleration;
            yd += dy / distance * acceleration;
            zd += dz / distance * acceleration;
        }
        xd = Mth.clamp(xd, -0.35D, 0.35D);
        yd = Mth.clamp(yd, -0.35D, 0.35D);
        zd = Mth.clamp(zd, -0.35D, 0.35D);
    }

    private boolean reachedTargetBlock() {
        return Mth.floor(x) == Mth.floor(targetX)
                && Mth.floor(y) == Mth.floor(targetY)
                && Mth.floor(z) == Mth.floor(targetZ);
    }

    @Override protected float getU0() { return sprite.getU((uo + 1.0F) / 4.0F * 16.0F); }
    @Override protected float getU1() { return sprite.getU(uo / 4.0F * 16.0F); }
    @Override protected float getV0() { return sprite.getV(vo / 4.0F * 16.0F); }
    @Override protected float getV1() { return sprite.getV((vo + 1.0F) / 4.0F * 16.0F); }
    @Override protected int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.TERRAIN_SHEET; }
}
