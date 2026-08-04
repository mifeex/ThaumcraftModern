package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** TC4 blockStoneDevice:8, the Compound Recharge Focus. */
public final class CompoundRechargeFocusBlock extends Block {
    private static final VoxelShape OUTLINE = box(1, 0, 1, 15, 7, 15);

    public CompoundRechargeFocusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return OUTLINE;
    }

    /** The original metadata block kept a full cube collision box. */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.WAND_RECHARGE_PEDESTAL.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
            net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        BlockPos pedestalPos = pos.below();
        BlockState pedestal = level.getBlockState(pedestalPos);
        if (!(pedestal.getBlock() instanceof WandRechargePedestalBlock block)) {
            return InteractionResult.PASS;
        }
        return block.interact(level, pedestalPos, player, hand);
    }
}
