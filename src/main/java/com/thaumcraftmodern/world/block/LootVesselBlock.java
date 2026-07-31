package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 break-open loot urn/crate with its three original rarity metadata tiers.
 */
public final class LootVesselBlock extends Block {
    public static final IntegerProperty TYPE =
            IntegerProperty.create("type", 0, 2);
    private static final VoxelShape URN_SHAPE =
            box(2.0D, 1.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    private static final VoxelShape CRATE_SHAPE =
            box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    private final boolean urn;

    public LootVesselBlock(Properties properties, boolean urn) {
        super(properties);
        this.urn = urn;
        registerDefaultState(stateDefinition.any().setValue(TYPE, 0));
    }

    public BlockState stateForTier(int tier) {
        return defaultBlockState().setValue(TYPE, Mth.clamp(tier, 0, 2));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return urn ? URN_SHAPE : CRATE_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(TYPE);
    }
}
