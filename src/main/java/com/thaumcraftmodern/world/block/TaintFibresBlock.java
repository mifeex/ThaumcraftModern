package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * TC4 fibrous taint (metadata 0) clings to every available solid face instead
 * of rendering as a crossed plant.
 */
public final class TaintFibresBlock extends MultifaceBlock {
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public TaintFibresBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return spreader;
    }

    @Override
    public void randomTick(
            net.minecraft.world.level.block.state.BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        TaintEcology.randomTick(level, position, state, random);
    }

    @Override
    public void entityInside(
            net.minecraft.world.level.block.state.BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        TaintExposure.touch(level, entity);
    }
}
