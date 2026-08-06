package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.FluxScrubberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public final class FluxScrubberBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=DirectionProperty.create("facing");
    public FluxScrubberBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.DOWN)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING,context.getClickedFace().getOpposite());
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FluxScrubberBlockEntity entity=new FluxScrubberBlockEntity(pos, state);
        entity.setFacing(state.getValue(FACING));
        return entity;
    }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level,
            BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel ? createTickerHelper(type,
                ModBlockEntities.FLUX_SCRUBBER.get(), FluxScrubberBlockEntity::serverTick) : null;
    }
}
