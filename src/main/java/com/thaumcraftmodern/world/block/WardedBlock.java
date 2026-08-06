package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.WardedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Unbreakable owner-restorable shell used by the Warding focus. */
public final class WardedBlock extends BaseEntityBlock {
    public WardedBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardedBlockEntity(pos, state);
    }
}
