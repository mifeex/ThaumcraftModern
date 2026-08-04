package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Metadata replacement for classic multiblock parts. Each device owns its
 * property range, so old TC4 metadata values stay visible in modern block
 * states and can select the matching model.
 */
public final class ClassicPartBlock extends Block {
    public static final IntegerProperty PART =
            IntegerProperty.create("part", 0, 10);

    public ClassicPartBlock(Properties properties, int maximumPart) {
        super(properties);
        if (maximumPart < 1 || maximumPart > 10) {
            throw new IllegalArgumentException(
                    "maximumPart must be between 1 and 10"
            );
        }
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    public IntegerProperty part() {
        return PART;
    }

    public BlockState stateForPart(int value) {
        return defaultBlockState().setValue(PART, value);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server) {
            CraftingStructureDisassembly.partRemoved(server, pos, state);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(PART);
    }
}
