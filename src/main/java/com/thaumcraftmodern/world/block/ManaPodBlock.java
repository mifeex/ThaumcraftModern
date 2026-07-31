package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The TC4 pod hangs below wood in a magical biome and ripens through eight
 * growth states. Modern placement keeps the hanging support rule; biome
 * selection belongs to the world-generation feature.
 */
public final class ManaPodBlock extends CropBlock {
    public ManaPodBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos position
    ) {
        BlockState support = level.getBlockState(position.above());
        Block block = support.getBlock();
        return support.is(BlockTags.LOGS)
                || block instanceof LeavesBlock
                || block == ModBlocks.GREATWOOD_LOG.get()
                || block == ModBlocks.SILVERWOOD_LOG.get();
    }

    @Override
    protected boolean mayPlaceOn(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return true;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.MANA_BEAN.get();
    }

    /**
     * TC4 growth is deliberately independent of crop light and farmland:
     * each pod advances one stage on one out of thirty random ticks.
     */
    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        if (!state.canSurvive(level, position)) {
            level.destroyBlock(position, true);
            return;
        }
        int age = getAge(state);
        if (age < getMaxAge() && random.nextInt(30) == 0) {
            level.setBlock(position, getStateForAge(age + 1), 2);
        }
    }
}
