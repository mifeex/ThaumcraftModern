package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.DeconstructionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** TC4 table metadata 14, split into a dedicated modern block. */
public final class DeconstructionTableBlock extends BaseEntityBlock {
    private static final VoxelShape SUPPORT_SHAPE = Shapes.or(
            box(0, 8, 0, 16, 16, 16),
            box(0, 0, 0, 16, 4, 16),
            box(1, 4, 1, 5, 8, 5),
            box(11, 4, 1, 15, 8, 5),
            box(1, 4, 11, 5, 8, 15),
            box(11, 4, 11, 15, 8, 15)
    );

    public DeconstructionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getBlockSupportShape(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        // TC4 selected and collided with the full block, but only its upper
        // face counted as solid support.
        return SUPPORT_SHAPE;
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
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(position)
                instanceof DeconstructionTableBlockEntity table) {
            NetworkHooks.openScreen(serverPlayer, table, position);
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
                && level.getBlockEntity(position)
                instanceof DeconstructionTableBlockEntity table) {
            table.dropContents();
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new DeconstructionTableBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level instanceof ServerLevel
                ? createTickerHelper(
                        type,
                        ModBlockEntities.DECONSTRUCTION_TABLE.get(),
                        DeconstructionTableBlockEntity::serverTick
                )
                : null;
    }
}
