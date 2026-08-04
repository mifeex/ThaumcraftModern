package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class NodeStabilizerBlockEntity extends BlockEntity {
    private int count;

    public NodeStabilizerBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.NODE_STABILIZER.get(), position, state);
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            NodeStabilizerBlockEntity stabilizer
    ) {
        boolean hasNode = level.getBlockState(position.above())
                .is(ModBlocks.AURA_NODE.get())
                || level.getBlockState(position.above())
                .is(ModBlocks.ENERGIZED_AURA_NODE.get());
        if (hasNode) {
            stabilizer.count = Math.min(37, stabilizer.count + 1);
        } else {
            stabilizer.count = Math.max(0, stabilizer.count - 1);
        }
    }

    public int count() {
        return count;
    }

    public boolean advanced() {
        return getBlockState().is(ModBlocks.ADVANCED_NODE_STABILIZER.get());
    }
}
