package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.ArcaneLampBlockEntity;
import com.thaumcraftmodern.world.block.entity.FertilityLampBlockEntity;
import com.thaumcraftmodern.world.block.entity.GrowthLampBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Common attachment and ticking contract for the three classic TC4 lamps. */
public final class ArcaneLampBlock extends BaseEntityBlock {
    public enum Kind { ARCANE, GROWTH, FERTILITY }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = box(4, 2, 4, 12, 14, 12);
    private final Kind kind;

    public ArcaneLampBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(LIT, kind == Kind.ARCANE));
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(
                FACING, context.getClickedFace().getOpposite());
        return state.canSurvive(context.getLevel(), context.getClickedPos())
                ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction supportDirection = state.getValue(FACING);
        BlockPos support = pos.relative(supportDirection);
        return level.getBlockState(support).isFaceSturdy(
                level, support, supportDirection.getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction changedSide,
            BlockState neighbour, net.minecraft.world.level.LevelAccessor level,
            BlockPos pos, BlockPos neighbourPos) {
        return changedSide == state.getValue(FACING) && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, changedSide, neighbour, level, pos, neighbourPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean movedByPiston) {
        if (kind == Kind.ARCANE && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ArcaneLampBlockEntity lamp) {
            lamp.removeLights();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case ARCANE -> new ArcaneLampBlockEntity(pos, state);
            case GROWTH -> new GrowthLampBlockEntity(pos, state);
            case FERTILITY -> new FertilityLampBlockEntity(pos, state);
        };
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel)) return null;
        return switch (kind) {
            case ARCANE -> createTickerHelper(type, ModBlockEntities.ARCANE_LAMP.get(),
                    ArcaneLampBlockEntity::serverTick);
            case GROWTH -> createTickerHelper(type, ModBlockEntities.GROWTH_LAMP.get(),
                    GrowthLampBlockEntity::serverTick);
            case FERTILITY -> createTickerHelper(type, ModBlockEntities.FERTILITY_LAMP.get(),
                    FertilityLampBlockEntity::serverTick);
        };
    }

    @Override protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
