package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** TC4's base bouncing frost shard (3 damage, four impacts at 0.5 bounce). */
public final class FrostShardEntity extends ThrowableItemProjectile {
    private int bouncesLeft = 3;
    private float damage = 3.0F;

    public FrostShardEntity(EntityType<? extends FrostShardEntity> type, Level level) {
        super(type, level);
    }

    @Override protected Item getDefaultItem() {
        return ModItems.ARCANE_RECIPE_COMPONENTS.get("focus_frost").get();
    }

    @Override protected float getGravity() { return 0.05F; }
    public void configure(float damage, int bounces) {
        this.damage = damage; this.bouncesLeft = bounces;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) level().addParticle(ParticleTypes.SNOWFLAKE,
                getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        if (tickCount > 200) discard();
    }

    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide) {
            DamageSource source = getOwner() instanceof LivingEntity owner
                    ? damageSources().mobProjectile(this, owner) : damageSources().thrown(this, this);
            hit.getEntity().hurt(source, damage);
        }
        bounce(hit.getLocation().subtract(position()).normalize());
    }

    @Override protected void onHitBlock(BlockHitResult hit) {
        if (!level().isClientSide) {
            BlockPos adjacent = hit.getBlockPos().relative(hit.getDirection());
            if (level().getBlockState(adjacent).is(Blocks.WATER))
                level().setBlock(adjacent, Blocks.ICE.defaultBlockState(), 3);
        }
        Vec3 velocity = getDeltaMovement();
        Direction.Axis axis = hit.getDirection().getAxis();
        setDeltaMovement(axis == Direction.Axis.X ? -velocity.x : velocity.x,
                axis == Direction.Axis.Y ? -velocity.y * 0.9D : velocity.y,
                axis == Direction.Axis.Z ? -velocity.z : velocity.z);
        finishBounce();
    }

    private void bounce(Vec3 normal) {
        Vec3 velocity = getDeltaMovement();
        setDeltaMovement(velocity.subtract(normal.scale(2.0D * velocity.dot(normal))));
        finishBounce();
    }

    private void finishBounce() {
        setDeltaMovement(getDeltaMovement().scale(0.5D));
        if (bouncesLeft-- <= 0) discard();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bounces", bouncesLeft);
        tag.putFloat("damage", damage);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bouncesLeft = tag.getInt("bounces");
        if (tag.contains("damage")) damage = tag.getFloat("damage");
    }
}
