package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public final class ResearchTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ResearchTablePart> PART =
            EnumProperty.create("part", ResearchTablePart.class);
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 12, 0, 16, 16, 16),
            box(1, 0, 1, 4, 12, 4),
            box(12, 0, 1, 15, 12, 4),
            box(1, 0, 12, 4, 12, 15),
            box(12, 0, 12, 15, 12, 15)
    );

    public ResearchTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
                .setValue(PART, ResearchTablePart.MAIN));
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
        BlockPos mainPosition = mainPosition(state, position);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(mainPosition);
            if (blockEntity instanceof ResearchTableBlockEntity table) {
                NetworkHooks.openScreen(serverPlayer, table, mainPosition);
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
        if (!state.is(newState.getBlock())) {
            BlockPos mainPosition = mainPosition(state, position);
            BlockEntity blockEntity = level.getBlockEntity(mainPosition);
            if (state.getValue(PART) == ResearchTablePart.MAIN
                    && blockEntity instanceof ResearchTableBlockEntity table) {
                table.dropContents();
            }
            BlockPos counterpart = counterpartPosition(state, position);
            BlockState counterpartState = level.getBlockState(counterpart);
            if (counterpartState.is(this)
                    && counterpartState.getValue(FACING) == state.getValue(FACING)
                    && counterpartState.getValue(PART) != state.getValue(PART)) {
                level.removeBlock(counterpart, false);
            }
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return state.getValue(PART) == ResearchTablePart.MAIN
                ? new ResearchTableBlockEntity(position, state)
                : null;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING, PART);
    }

    public static BlockPos mainPosition(BlockState state, BlockPos position) {
        return state.getValue(PART) == ResearchTablePart.MAIN
                ? position
                : position.relative(state.getValue(FACING).getOpposite());
    }

    private static BlockPos counterpartPosition(
            BlockState state,
            BlockPos position
    ) {
        return state.getValue(PART) == ResearchTablePart.MAIN
                ? position.relative(state.getValue(FACING))
                : position.relative(state.getValue(FACING).getOpposite());
    }
}
