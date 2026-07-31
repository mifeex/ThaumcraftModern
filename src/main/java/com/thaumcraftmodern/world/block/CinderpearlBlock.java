package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cinderpearls use TC4's desert rule and may root directly in sand.
 */
public final class CinderpearlBlock extends FlowerBlock {
    public CinderpearlBlock(
            MobEffect effect,
            int duration,
            BlockBehaviour.Properties properties
    ) {
        super(effect, duration, properties);
    }

    @Override
    protected boolean mayPlaceOn(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return state.is(BlockTags.SAND)
                || super.mayPlaceOn(state, level, position);
    }
}
