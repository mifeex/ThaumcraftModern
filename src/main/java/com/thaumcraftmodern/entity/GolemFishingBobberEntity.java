package com.thaumcraftmodern.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** TC4-style golem fishing float. The server owns its fisher and lifetime. */
public final class GolemFishingBobberEntity extends Entity {
    public static final byte SPLASH_AMBIENT = 16;
    public static final byte SPLASH_CATCH = 18;
    private static final EntityDataAccessor<Integer> FISHER_ID = SynchedEntityData.defineId(
            GolemFishingBobberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(
            GolemFishingBobberEntity.class, EntityDataSerializers.BLOCK_POS);
    private int age;
    private int removalCountdown = -1;

    public GolemFishingBobberEntity(EntityType<? extends GolemFishingBobberEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public void castFrom(ClassicGolemEntity fisher, BlockPos target) {
        entityData.set(FISHER_ID, fisher.getId());
        entityData.set(TARGET, target.immutable());
        setPos(fisher.getX(), fisher.getY() + fisher.getBbHeight() * .55D, fisher.getZ());
        Vec3 destination = floatPosition(target);
        Vec3 cast = destination.subtract(position());
        double distance = Math.max(.001D, cast.length());
        setDeltaMovement(cast.x * .1D, cast.y * .1D + Math.sqrt(distance) * .08D, cast.z * .1D);
    }

    public ClassicGolemEntity fisher() {
        Entity entity = level().getEntity(entityData.get(FISHER_ID));
        return entity instanceof ClassicGolemEntity golem ? golem : null;
    }

    @Override protected void defineSynchedData() {
        entityData.define(FISHER_ID, -1);
        entityData.define(TARGET, BlockPos.ZERO);
    }

    @Override public void tick() {
        super.tick();
        age++;
        if (removalCountdown >= 0 && --removalCountdown <= 0) {
            discard();
            return;
        }
        ClassicGolemEntity fisher = fisher();
        if (!level().isClientSide && (fisher == null || !fisher.isAlive()
                || fisher.core() != GolemCoreType.FISHING || age > 4000)) {
            discard();
            return;
        }
        Vec3 destination = floatPosition(entityData.get(TARGET));
        boolean settled = age > 24 || position().distanceToSqr(destination) < .45D || isInWater();
        if (settled) {
            setPos(destination.x, destination.y + Math.sin((tickCount + getId()) * .12D) * .025D, destination.z);
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide && random.nextFloat() < .02F) {
                level().broadcastEntityEvent(this, SPLASH_AMBIENT);
            }
            return;
        }
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(.92D).add(0D, -.025D, 0D));
    }

    public void catchSplash() {
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, SPLASH_CATCH);
            removalCountdown = 3;
        }
    }

    @Override public void handleEntityEvent(byte event) {
        if (event == SPLASH_AMBIENT || event == SPLASH_CATCH) {
            int count = event == SPLASH_CATCH ? 18 : 4;
            for (int index = 0; index < count; index++) {
                level().addParticle(index % 2 == 0 ? ParticleTypes.SPLASH : ParticleTypes.BUBBLE,
                        getX() + (random.nextDouble() - .5D) * .5D, getY(),
                        getZ() + (random.nextDouble() - .5D) * .5D,
                        (random.nextDouble() - .5D) * .08D, event == SPLASH_CATCH ? .12D : .03D,
                        (random.nextDouble() - .5D) * .08D);
            }
            return;
        }
        super.handleEntityEvent(event);
    }

    private static Vec3 floatPosition(BlockPos target) {
        return new Vec3(target.getX() + .5D, target.getY() + .88D, target.getZ() + .5D);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(FISHER_ID, tag.getInt("Fisher"));
        entityData.set(TARGET, BlockPos.of(tag.getLong("Target")));
        age = tag.getInt("Age");
        removalCountdown = tag.getInt("RemovalCountdown");
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fisher", entityData.get(FISHER_ID));
        tag.putLong("Target", entityData.get(TARGET).asLong());
        tag.putInt("Age", age);
        tag.putInt("RemovalCountdown", removalCountdown);
    }

    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
