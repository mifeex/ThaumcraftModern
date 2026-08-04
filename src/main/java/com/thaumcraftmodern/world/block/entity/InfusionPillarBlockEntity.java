package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Lower, rendered half of the classic two-block infusion pillar. */
public final class InfusionPillarBlockEntity extends BlockEntity {
    public InfusionPillarBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.INFUSION_PILLAR.get(), position, state);
    }
}
