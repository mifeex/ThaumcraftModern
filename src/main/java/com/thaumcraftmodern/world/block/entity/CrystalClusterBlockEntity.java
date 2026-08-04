package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalClusterBlockEntity extends BlockEntity {
    public CrystalClusterBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CLUSTER.get(), position, state);
    }
}
