package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public final class ArcaneWorkbenchBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 8, 0, 16, 16, 16),
            box(0, 0, 0, 16, 4, 16),
            box(1, 4, 1, 5, 8, 5),
            box(11, 4, 1, 15, 8, 5),
            box(1, 4, 11, 5, 8, 15),
            box(11, 4, 11, 15, 8, 15)
    );

    public ArcaneWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(position) instanceof ArcaneWorkbenchBlockEntity workbench) {
                NetworkHooks.openScreen(serverPlayer, workbench, position);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position) instanceof ArcaneWorkbenchBlockEntity workbench) {
            workbench.dropContents();
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ArcaneWorkbenchBlockEntity(position, state);
    }
}
