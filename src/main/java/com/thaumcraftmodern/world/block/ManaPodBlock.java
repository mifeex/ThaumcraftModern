package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The TC4 pod hangs below wood in a magical biome and ripens through eight
 * growth states. Modern placement keeps the hanging support rule; biome
 * selection belongs to the world-generation feature.
 */
public final class ManaPodBlock extends CropBlock implements EntityBlock {
    private static final VoxelShape[] SHAPES = {
            Block.box(4.0D, 12.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 10.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 8.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 6.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 5.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 4.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 3.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 2.0D, 4.0D, 12.0D, 16.0D, 12.0D)
    };

    public ManaPodBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ManaPodBlockEntity(position, state);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPES[getAge(state)];
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
        ManaPodBlockEntity pod;
        if (level.getBlockEntity(position)
                instanceof ManaPodBlockEntity existing) {
            pod = existing;
        } else {
            // Worlds created before mana pods gained aspect NBT contain no
            // block-entity entry. Recreate it lazily so the classic renderer
            // and growth cycle recover without replacing every old pod.
            pod = new ManaPodBlockEntity(position, state);
            level.setBlockEntity(pod);
            level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
        }
        if (random.nextInt(30) == 0) {
            pod.checkGrowth(level);
        }
    }
}
