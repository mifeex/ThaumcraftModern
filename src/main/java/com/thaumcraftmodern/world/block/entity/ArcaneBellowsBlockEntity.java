package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render state for TC4's continuously pumping Arcane Bellows. */
public final class ArcaneBellowsBlockEntity extends BlockEntity {
    public ArcaneBellowsBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ARCANE_BELLOWS.get(), position, state);
    }

    /** Exact fallback used by the original item renderer: sin(ticks / 8) * .3 + .7. */
    public float inflation(float partialTick) {
        float ticks = level == null ? partialTick : level.getGameTime() + partialTick;
        return net.minecraft.util.Mth.sin(ticks / 8.0F) * 0.3F + 0.7F;
    }
}
