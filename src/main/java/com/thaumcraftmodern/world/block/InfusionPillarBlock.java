package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import com.thaumcraftmodern.world.block.entity.InfusionPillarBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public final class InfusionPillarBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty CAP = BooleanProperty.create("cap");

    public InfusionPillarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CAP, false));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING, CAP);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        // TC4 metadata 4 is only the invisible upper multiblock placeholder.
        return state.getValue(CAP) ? null
                : new InfusionPillarBlockEntity(position, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server) {
            CraftingStructureDisassembly.partRemoved(server, pos, state);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
