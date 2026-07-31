package com.thaumcraftmodern.item;

import com.thaumcraftmodern.world.block.FluxGooBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * TC4's BlockFluxGooItem placed metadata zero: one quantum, rendered as a
 * thin surface layer. Full level-seven blocks are produced by world effects.
 */
public final class FluxGooBlockItem extends BlockItem {
    public FluxGooBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null
                ? null
                : state.setValue(FluxGooBlock.LEVEL, 0);
    }
}
