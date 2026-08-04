package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.ArcaneEarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Dedicated modern block for TC4's wooden-device metadata 1. */
public final class ArcaneEarBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public ArcaneEarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(POWERED);
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
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof ArcaneEarBlockEntity ear) {
            ear.changePitchAndPlay();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos position,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, position, state, placer, stack);
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof ArcaneEarBlockEntity ear) {
            ear.updateTone();
        }
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            Block neighbor,
            BlockPos neighborPosition,
            boolean movedByPiston
    ) {
        super.neighborChanged(
                state, level, position, neighbor, neighborPosition, movedByPiston
        );
        if (!level.isClientSide
                && level.getBlockEntity(position)
                instanceof ArcaneEarBlockEntity ear) {
            ear.updateTone();
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            Direction side
    ) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            Direction side
    ) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new ArcaneEarBlockEntity(position, state);
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
                        ModBlockEntities.ARCANE_EAR.get(),
                        ArcaneEarBlockEntity::serverTick
                )
                : null;
    }
}
