package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BrainJarBlockEntity extends BlockEntity {
    public static final int MAX_XP = 2000;
    private int xp;
    private float rotation,previousRotation;
    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRAIN_JAR.get(), pos, state);
    }
    public static void serverTick(net.minecraft.world.level.Level raw, BlockPos pos,
            BlockState state, BrainJarBlockEntity jar) {
        if (!(raw instanceof ServerLevel level) || jar.xp >= MAX_XP) return;
        ExperienceOrb closest = level.getEntitiesOfClass(ExperienceOrb.class,
                new AABB(pos).inflate(8)).stream().min((a,b) -> Double.compare(
                        a.distanceToSqr(pos.getCenter()), b.distanceToSqr(pos.getCenter()))).orElse(null);
        if (closest == null) return;
        var delta = pos.getCenter().subtract(closest.position()).scale(1.0 / 25.0);
        double distance = delta.length();
        if (distance > 0 && distance < 1) {
            double strength = (1 - distance) * (1 - distance);
            closest.setDeltaMovement(closest.getDeltaMovement().add(delta.x / distance * strength * .3,
                    delta.y / distance * strength * .5, delta.z / distance * strength * .3));
        }
        if (closest.getBoundingBox().intersects(new AABB(pos).inflate(.1))) {
            jar.xp = Math.min(MAX_XP, jar.xp + closest.value);
            closest.discard();
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, .1F, 1F);
            jar.setChanged();
        }
    }
    public int storedExperience() { return xp; }
    public static void clientTick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,BrainJarBlockEntity jar){
        jar.previousRotation=jar.rotation;
        var target=level.getNearestPlayer(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,6,false);
        if(target!=null){double dx=target.getX()-(pos.getX()+.5),dz=target.getZ()-(pos.getZ()+.5);float wanted=(float)Math.atan2(dz,dx);float delta=wanted-jar.rotation;while(delta>Math.PI)delta-=(float)(Math.PI*2);while(delta<-Math.PI)delta+=(float)(Math.PI*2);jar.rotation+=delta*.04f;}else jar.rotation+=.01f;
    }
    public float rotation(float partial){return net.minecraft.util.Mth.lerp(partial,previousRotation,rotation);}
    public float bob(float partial){float ticks=level==null?partial:level.getGameTime()+partial;return net.minecraft.util.Mth.sin(ticks/14f)*.03f+.03f;}
    public int releaseExperience() { int out = xp; xp = 0; setChanged(); return out; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("XP", xp); }
    @Override public void load(CompoundTag tag) { super.load(tag); xp = Math.min(MAX_XP, tag.getInt("XP")); }
}
