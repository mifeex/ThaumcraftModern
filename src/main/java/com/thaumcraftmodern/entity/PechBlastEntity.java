package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

/**
 * TC4 Pech's Curse projectile: three magic damage, followed by one of poison,
 * slowness II, or weakness. Ten percent of Pech casts apply all three.
 */
public final class PechBlastEntity extends ThrowableProjectile {
    private static final DustParticleOptions PARTICLE =
            new DustParticleOptions(new Vector3f(0.3F, 0.15F, 0.45F), 0.8F);
    private boolean nightshade;

    public PechBlastEntity(
            EntityType<? extends PechBlastEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public PechBlastEntity(
            LivingEntity owner,
            boolean nightshade,
            Level level
    ) {
        super(ModEntities.PECH_BLAST.get(), owner, level);
        this.nightshade = nightshade;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return 0.025F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int index = 0; index < 3; index++) {
                level().addParticle(
                        PARTICLE,
                        getX() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                        getY() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                        getZ() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
        if (tickCount > 500) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        LivingEntity owner = getOwner() instanceof LivingEntity living
                ? living
                : null;
        for (LivingEntity target : level().getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(2.0D),
                candidate -> candidate != owner
                        && !(candidate instanceof LegacyThaumcraftMob mob
                        && mob.kind() == LegacyMobKind.PECH)
        )) {
            DamageSource source = damageSources().indirectMagic(this, owner);
            target.hurt(source, 3.0F);
            int selection = random.nextInt(3);
            if (nightshade || selection == 0) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        100,
                        1
                ));
            }
            if (nightshade || selection == 1) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        100,
                        2
                ));
            }
            if (nightshade || selection == 2) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        100,
                        1
                ));
            }
        }
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 3) {
            for (int index = 0; index < 27; index++) {
                level().addParticle(
                        PARTICLE,
                        getX() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                        getY() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                        getZ() + (random.nextDouble() - random.nextDouble()) * 0.3D,
                        (random.nextDouble() - random.nextDouble()) * 0.2D,
                        (random.nextDouble() - random.nextDouble()) * 0.2D,
                        (random.nextDouble() - random.nextDouble()) * 0.2D
                );
            }
            return;
        }
        super.handleEntityEvent(eventId);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Nightshade", nightshade);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        nightshade = tag.getBoolean("Nightshade");
    }
}
