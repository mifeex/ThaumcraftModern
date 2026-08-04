package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Hostile warp observer whose three back limbs attack independently. */
public final class FacelessWitnessEntity extends Monster {
    private static final EntityDataAccessor<Integer> ATTACK_VARIANT =
            SynchedEntityData.defineId(
                    FacelessWitnessEntity.class,
                    EntityDataSerializers.INT
            );
    private static final byte ALERT_EVENT = 74;
    private static final byte FIRST_ATTACK_EVENT = 70;
    private static final int ATTACK_VARIANTS = 4;
    private static final int ATTACK_ANIMATION_TICKS = 14;
    private static final int ALERT_ANIMATION_TICKS = 22;
    private static final double TELEPORT_RANGE = 8.0D;
    private static final double BLINDNESS_RANGE = 3.0D;

    private int clientAttackStart = Integer.MIN_VALUE;
    private int clientAlertStart = Integer.MIN_VALUE;
    private int nextIdleSoundTick;
    private int nextBlindnessTick;
    private int nextCombatTeleportTick;

    public FacelessWitnessEntity(
            EntityType<? extends FacelessWitnessEntity> type,
            Level level
    ) {
        super(type, level);
        xpReward = 14;
        nextIdleSoundTick = 40 + getRandom().nextInt(61);
        nextCombatTeleportTick = 60 + getRandom().nextInt(41);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 55.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ATTACK_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(this, Player.class, true)
        );
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = getTarget();
        super.setTarget(target);
        if (!level().isClientSide && previous == null && target != null) {
            level().broadcastEntityEvent(this, ALERT_EVENT);
            playSound(ModSounds.WITNESS_ALERT.get(), 1.0F, getVoicePitch());
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        int variant = getRandom().nextInt(ATTACK_VARIANTS);
        entityData.set(ATTACK_VARIANT, variant);
        level().broadcastEntityEvent(this, (byte) (FIRST_ATTACK_EVENT + variant));
        playSound(
                ModSounds.WITNESS_ATTACK.get(),
                1.0F,
                0.94F + getRandom().nextFloat() * 0.12F
        );
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 1));
            if (variant == 1) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 0));
            } else if (variant == 2) {
                double dx = getX() - living.getX();
                double dz = getZ() - living.getZ();
                double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                living.push(dx / length * 0.28D, 0.08D, dz / length * 0.28D);
            } else if (variant == 3) {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 180, 1));
            }
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event >= FIRST_ATTACK_EVENT
                && event < FIRST_ATTACK_EVENT + ATTACK_VARIANTS) {
            entityData.set(ATTACK_VARIANT, event - FIRST_ATTACK_EVENT);
            clientAttackStart = tickCount;
            return;
        }
        if (event == ALERT_EVENT) {
            clientAlertStart = tickCount;
            return;
        }
        super.handleEntityEvent(event);
    }

    public int attackVariant() {
        return entityData.get(ATTACK_VARIANT);
    }

    public float attackAnimation(float partialTick) {
        return progress(clientAttackStart, ATTACK_ANIMATION_TICKS, partialTick);
    }

    public float alertAnimation(float partialTick) {
        return progress(clientAlertStart, ALERT_ANIMATION_TICKS, partialTick);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !isAlive()) {
            return;
        }
        if (tickCount >= nextIdleSoundTick) {
            playSound(ModSounds.WITNESS_IDLE.get(), 0.95F, getVoicePitch());
            nextIdleSoundTick = tickCount + 120 + getRandom().nextInt(101);
        }
        applyProximityBlindness();
        if (tickCount >= nextCombatTeleportTick) {
            LivingEntity anchor = combatAnchor();
            if (anchor != null) {
                teleportAround(anchor);
            }
            nextCombatTeleportTick = tickCount + 60 + getRandom().nextInt(41);
        }
    }

    private void applyProximityBlindness() {
        if (tickCount < nextBlindnessTick) {
            return;
        }
        boolean applied = false;
        AABB area = getBoundingBox().inflate(BLINDNESS_RANGE);
        for (Player player : level().getEntitiesOfClass(
                Player.class,
                area,
                player -> !player.isSpectator()
        )) {
            if (distanceToSqr(player) <= BLINDNESS_RANGE * BLINDNESS_RANGE) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1));
                applied = true;
            }
        }
        if (applied) {
            nextBlindnessTick = tickCount + 80;
            nextCombatTeleportTick = Math.min(
                    nextCombatTeleportTick,
                    tickCount + 4
            );
        }
    }

    @Nullable
    private LivingEntity combatAnchor() {
        LivingEntity target = getTarget();
        LegacyThaumcraftMob cultist = level().getEntitiesOfClass(
                        LegacyThaumcraftMob.class,
                        getBoundingBox().inflate(12.0D),
                        mob -> isCrimsonCultist(mob) && mob.isAlive()
                ).stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        if (cultist != null && cultist.getTarget() != null) {
            return cultist.getTarget();
        }
        if (target != null && cultist != null && getRandom().nextBoolean()) {
            return cultist;
        }
        return target;
    }

    private static boolean isCrimsonCultist(LegacyThaumcraftMob mob) {
        return mob.kind() == LegacyMobKind.CRIMSON_KNIGHT
                || mob.kind() == LegacyMobKind.CRIMSON_CLERIC
                || mob.kind() == LegacyMobKind.CRIMSON_PRAETOR;
    }

    private boolean teleportAround(LivingEntity anchor) {
        Player playerAnchor = anchor instanceof Player player ? player : null;
        boolean blinded = playerAnchor != null
                && playerAnchor.hasEffect(MobEffects.BLINDNESS);
        int behindAttempts = blinded ? 14 : playerAnchor != null ? 10 : 0;
        for (int attempt = 0; attempt < 16; attempt++) {
            double x;
            double z;
            if (attempt < behindAttempts) {
                Vec3 look = playerAnchor.getLookAngle();
                Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
                if (horizontal.lengthSqr() < 0.001D) {
                    horizontal = new Vec3(0.0D, 0.0D, 1.0D);
                } else {
                    horizontal = horizontal.normalize();
                }
                double radius = 1.4D + getRandom().nextDouble() * 0.8D;
                double lateral = (getRandom().nextDouble() - 0.5D) * 2.0D;
                x = anchor.getX() - horizontal.x * radius
                        - horizontal.z * lateral;
                z = anchor.getZ() - horizontal.z * radius
                        + horizontal.x * lateral;
            } else {
                double angle = getRandom().nextDouble() * Mth.TWO_PI;
                double radius = 2.25D + getRandom().nextDouble() * 3.5D;
                x = anchor.getX() + Math.cos(angle) * radius;
                z = anchor.getZ() + Math.sin(angle) * radius;
            }
            if (distanceToSqr(x, getY(), z)
                    > TELEPORT_RANGE * TELEPORT_RANGE) {
                continue;
            }
            BlockPos top = BlockPos.containing(x, anchor.getY() + 4.0D, z);
            for (int down = 0; down <= 8; down++) {
                BlockPos feet = top.below(down);
                BlockPos floor = feet.below();
                if (!level().getBlockState(floor).isFaceSturdy(
                        level(),
                        floor,
                        Direction.UP
                )) {
                    continue;
                }
                double y = feet.getY();
                AABB destination = getBoundingBox().move(
                        x - getX(),
                        y - getY(),
                        z - getZ()
                );
                if (!level().noCollision(this, destination)
                        || level().containsAnyLiquid(destination)) {
                    continue;
                }
                Vec3 origin = position();
                teleportTo(x, y, z);
                getNavigation().stop();
                LivingEntity attackTarget = anchor instanceof Player
                        ? anchor
                        : getTarget();
                if (attackTarget != null && attackTarget.isAlive()) {
                    setTarget(attackTarget);
                    getLookControl().setLookAt(attackTarget, 100.0F, 100.0F);
                    getNavigation().moveTo(attackTarget, 1.2D);
                }
                level().gameEvent(GameEvent.TELEPORT, origin, GameEvent.Context.of(this));
                playSound(SoundEvents.ENDERMAN_TELEPORT, 0.75F, 0.72F);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(
                            ParticleTypes.PORTAL,
                            origin.x,
                            origin.y + getBbHeight() * 0.5D,
                            origin.z,
                            22,
                            0.35D,
                            0.8D,
                            0.35D,
                            0.08D
                    );
                    server.sendParticles(
                            ParticleTypes.PORTAL,
                            getX(),
                            getY() + getBbHeight() * 0.5D,
                            getZ(),
                            22,
                            0.35D,
                            0.8D,
                            0.35D,
                            0.08D
                    );
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && getRandom().nextFloat() < 0.35F) {
            LivingEntity anchor = combatAnchor();
            if (anchor != null) {
                teleportAround(anchor);
                nextCombatTeleportTick = tickCount + 60 + getRandom().nextInt(41);
            }
        }
        return hurt;
    }

    private float progress(int start, int duration, float partialTick) {
        if (start == Integer.MIN_VALUE) {
            return 0.0F;
        }
        float elapsed = tickCount - start + partialTick;
        if (elapsed < 0.0F || elapsed > duration) {
            return 0.0F;
        }
        return Mth.clamp(elapsed / duration, 0.0F, 1.0F);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WITNESS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WITNESS_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public float getVoicePitch() {
        return 0.94F + (getRandom().nextFloat() - getRandom().nextFloat()) * 0.06F;
    }
}
