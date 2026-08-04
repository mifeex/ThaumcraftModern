package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BottledTaintProjectile extends ThrowableItemProjectile {
    public static final double EFFECT_RADIUS = 5.0D;
    public static final int INFECTION_TICKS = 100;
    public static final int TERRAIN_ATTEMPTS = 10;

    public BottledTaintProjectile(EntityType<? extends BottledTaintProjectile> type, Level level) {
        super(type, level);
    }

    public BottledTaintProjectile(LivingEntity owner, Level level) {
        super(ModEntities.BOTTLED_TAINT.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BOTTLED_TAINT.get();
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (result.getEntity() == getOwner()) return;
        super.onHitEntity(result);
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() == getOwner()) return;
        super.onHit(result);
        if (!(level() instanceof ServerLevel server)) return;

        AABB area = getBoundingBox().inflate(EFFECT_RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area)) {
            String typePath = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getPath();
            boolean tainted = typePath.contains("taint")
                    || target.hasEffect(ModEffects.FLUX_TAINT.get());
            if (!target.getMobType().equals(net.minecraft.world.entity.MobType.UNDEAD)
                    && !tainted) {
                target.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT.get(), INFECTION_TICKS));
            }
        }
        BlockPos origin = blockPosition();
        for (int attempt = 0; attempt < TERRAIN_ATTEMPTS; attempt++) {
            int x = origin.getX() + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
            int z = origin.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
            BlockPos column = new BlockPos(x, origin.getY(), z);
            if (random.nextBoolean() || TaintBiomeService.isTainted(server, column)) continue;
            TaintBiomeService.taintColumn(server, column);
            BlockPos surface = server.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    column
            );
            if (server.getBlockState(surface).canBeReplaced()
                    && server.getBlockState(surface.below()).isFaceSturdy(server, surface.below(), net.minecraft.core.Direction.UP)) {
                server.setBlock(surface,
                        com.thaumcraftmodern.registry.ModBlocks.TAINT_FIBRES.get().defaultBlockState(), 3);
            }
        }
        server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 100, 1.0D, 1.0D, 1.0D, 0.1D);
        server.playSound(null, blockPosition(), SoundEvents.SPLASH_POTION_BREAK,
                SoundSource.NEUTRAL, 1.0F, 0.8F + random.nextFloat() * 0.2F);
        discard();
    }
}
