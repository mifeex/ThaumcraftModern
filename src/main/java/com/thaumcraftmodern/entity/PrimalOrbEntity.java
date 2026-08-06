package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

import java.util.Random;

/** The base (non-seeker) TC4 primal orb: slow, erratic and explosive. */
public final class PrimalOrbEntity extends ThrowableProjectile {
    private boolean seeker;
    public PrimalOrbEntity(EntityType<? extends PrimalOrbEntity> type, Level level) {
        super(type, level);
    }

    @Override protected void defineSynchedData() {}

    @Override protected float getGravity() { return 0.001F; }
    public void setSeeker(boolean seeker) { this.seeker = seeker; }

    @Override public void tick() {
        if (seeker && !level().isClientSide && getOwner() instanceof net.minecraft.world.entity.LivingEntity owner) {
            net.minecraft.world.entity.LivingEntity target = level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class, getBoundingBox().inflate(12.0D),
                    entity -> entity != owner && entity.isAlive()).stream()
                    .min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (target != null) {
                net.minecraft.world.phys.Vec3 desired = target.getEyePosition().subtract(position()).normalize().scale(0.08D);
                setDeltaMovement(getDeltaMovement().scale(0.88D).add(desired));
            }
        }
        if (tickCount > 20) {
            Random seeded = new Random(getId() + tickCount);
            setDeltaMovement(getDeltaMovement().add(
                    (seeded.nextFloat() - seeded.nextFloat()) * 0.01F,
                    (seeded.nextFloat() - seeded.nextFloat()) * 0.01F,
                    (seeded.nextFloat() - seeded.nextFloat()) * 0.01F));
        }
        super.tick();
        if (level().isClientSide) {
            int[] colors = {0xFFFF7E, 0xFF4500, 0x4F69CC, 0x35A64A, 0x9A62C7, 0xA0E5FF};
            int color = colors[Math.floorMod(tickCount, colors.length)];
            level().addParticle(new DustParticleOptions(new Vector3f(
                            ((color >> 16) & 255) / 255.0F,
                            ((color >> 8) & 255) / 255.0F,
                            (color & 255) / 255.0F), 1.4F),
                    getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (tickCount > 5000) discard();
    }

    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 2.0F, true,
                    Level.ExplosionInteraction.MOB);
            if (random.nextInt(100) < 1) {
                if (random.nextBoolean()) taintSplosion();
                else if (result instanceof BlockHitResult blockHit) createRandomNode(blockHit);
            }
            discard();
        }
    }

    private void taintSplosion() {
        var serverLevel = (net.minecraft.server.level.ServerLevel) level();
        int x = blockPosition().getX();
        int z = blockPosition().getZ();
        for (int index = 0; index < 10; index++) {
            int xx = x + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            int zz = z + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            if (random.nextBoolean()) continue;
            int yy = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xx, zz);
            BlockPos surface = new BlockPos(xx, yy, zz);
            TaintBiomeService.taintColumn(serverLevel, surface);
            if (serverLevel.getBlockState(surface).canBeReplaced())
                serverLevel.setBlock(surface, ModBlocks.TAINT_FIBRES.get().defaultBlockState(), 3);
        }
    }

    private void createRandomNode(BlockHitResult hit) {
        BlockPos position = hit.getBlockPos().relative(hit.getDirection());
        if (!level().getBlockState(position).canBeReplaced()) position = position.above();
        if (!level().getBlockState(position).canBeReplaced()) return;
        level().setBlock(position, ModBlocks.AURA_NODE.get().defaultBlockState(), 3);
        if (level().getBlockEntity(position) instanceof AuraNodeBlockEntity node)
            node.initializeOnce(AuraNodeFactory.newWorldNode());
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putBoolean("Seeker", seeker); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { seeker = tag.getBoolean("Seeker"); }
}
