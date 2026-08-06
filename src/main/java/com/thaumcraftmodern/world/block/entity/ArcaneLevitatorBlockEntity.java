package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ArcaneLevitatorBlockEntity extends BlockEntity {
    public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ARCANE_LEVITATOR.get(),pos,state); }
    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLevitatorBlockEntity ignored) {
        if (gettingPower(level, pos)) return;
        int range = rangeAbove(level, pos);
        if (range <= 0) return;
        AABB column=new AABB(pos.getX(),pos.getY()+1,pos.getZ(),pos.getX()+1,pos.getY()+1+range,pos.getZ()+1);
        for(Entity target:level.getEntities((Entity)null,column,
                e -> e instanceof ItemEntity || e instanceof LivingEntity)) {
            var motion=target.getDeltaMovement();
            if(target.isShiftKeyDown()) {
                if(motion.y<0) target.setDeltaMovement(motion.multiply(1,.9,1));
            } else if(motion.y<.35) {
                target.setDeltaMovement(motion.add(0,.1,0));
            }
            target.resetFallDistance();
        }
    }

    public static int rangeAbove(Level level, BlockPos pos) {
        int max=10, below=1;
        while(level.getBlockState(pos.below(below)).is(ModBlocks.ARCANE_LEVITATOR.get())
                && !gettingPower(level, pos.below(below))) { max+=10; below++; }
        int range=0;
        while(range<max && !level.getBlockState(pos.above(1+range)).canOcclude()) range++;
        return range;
    }

    public static boolean gettingPower(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }
}
