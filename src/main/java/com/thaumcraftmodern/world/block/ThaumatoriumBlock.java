package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.network.NetworkHooks;

public final class ThaumatoriumBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF =
            BlockStateProperties.DOUBLE_BLOCK_HALF;

    public ThaumatoriumBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static BlockPos lowerPosition(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos lower = lowerPosition(state, pos);
        if (!(level.getBlockEntity(lower) instanceof ThaumatoriumBlockEntity machine)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            ItemStack catalyst = machine.removeCatalyst();
            if (!catalyst.isEmpty() && !player.addItem(catalyst)) player.drop(catalyst, false);
            return InteractionResult.CONSUME;
        }
        NetworkHooks.openScreen(serverPlayer, machine, lower);
        return InteractionResult.CONSUME;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThaumatoriumBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel && state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? createTickerHelper(type, ModBlockEntities.THAUMATORIUM.get(),
                        ThaumatoriumBlockEntity::serverTick)
                : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            BlockPos lower = lowerPosition(state, pos);
            if (!level.isClientSide && level.getBlockEntity(lower)
                    instanceof ThaumatoriumBlockEntity machine) {
                ItemStack catalyst = machine.removeCatalyst();
                if (!catalyst.isEmpty()) popResource(level, lower, catalyst);
            }
            BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER
                    ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this)) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING, HALF);
    }
}
