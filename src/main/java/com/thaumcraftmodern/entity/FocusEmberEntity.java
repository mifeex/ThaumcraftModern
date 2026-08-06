package com.thaumcraftmodern.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** TC4's base fire-focus ember: zero gravity, 20 ticks, 0.95 velocity decay. */
public final class FocusEmberEntity extends ThrowableProjectile {
    private float damage = 2.0F;
    private int fireSeconds = 3;
    public FocusEmberEntity(EntityType<? extends FocusEmberEntity> type, Level level) {
        super(type, level);
    }

    @Override protected void defineSynchedData() {}
    @Override protected float getGravity() { return 0.0F; }
    public void configure(float damage, int fireSeconds) {
        this.damage = damage;
        this.fireSeconds = fireSeconds;
    }

    @Override public void tick() {
        setDeltaMovement(getDeltaMovement().scale(0.95D));
        super.tick();
        if (level().isClientSide) level().addParticle(ParticleTypes.FLAME,
                getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        if (tickCount > 20) discard();
    }

    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && !hit.getEntity().fireImmune()) {
            boolean hurt = getOwner() instanceof LivingEntity owner
                    ? hit.getEntity().hurt(damageSources().mobProjectile(this, owner), damage)
                    : hit.getEntity().hurt(damageSources().onFire(), damage);
            if (hurt) hit.getEntity().setSecondsOnFire(fireSeconds);
        }
    }

    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) discard();
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage); tag.putInt("Fire", fireSeconds);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Damage")) damage = tag.getFloat("Damage");
        if (tag.contains("Fire")) fireSeconds = tag.getInt("Fire");
    }
}
