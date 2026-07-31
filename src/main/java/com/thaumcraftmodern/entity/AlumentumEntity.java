package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** TC4 Alumentum projectile with its exact 1.66 explosion strength. */
public final class AlumentumEntity extends ThrowableItemProjectile {
    public static final float EXPLOSION_STRENGTH = 1.66F;

    public AlumentumEntity(
            EntityType<? extends AlumentumEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public AlumentumEntity(LivingEntity owner, Level level) {
        super(ModEntities.ALUMENTUM.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ALUMENTUM.get();
    }

    @Override
    protected float getGravity() {
        return 0.03F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            return;
        }
        for (int index = 0; index < 3; index++) {
            level().addParticle(
                    ParticleTypes.WITCH,
                    getX() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                    getY() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                    getZ() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                    0.0D,
                    0.02D,
                    0.0D
            );
            level().addParticle(
                    ParticleTypes.END_ROD,
                    getX() + (random.nextDouble() - random.nextDouble()) * 0.1D,
                    getY() + (random.nextDouble() - random.nextDouble()) * 0.1D,
                    getZ() + (random.nextDouble() - random.nextDouble()) * 0.1D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(
                    this,
                    getX(),
                    getY(),
                    getZ(),
                    EXPLOSION_STRENGTH,
                    Level.ExplosionInteraction.MOB
            );
            discard();
        }
    }
}
