package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Solid TC4 taint states share the same slow ecology tick.
 */
public final class SpreadingTaintBlock extends Block {
    public SpreadingTaintBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        TaintEcology.randomTick(level, position, state, random);
    }
}
