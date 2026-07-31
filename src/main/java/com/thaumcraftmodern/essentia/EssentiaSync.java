package com.thaumcraftmodern.essentia;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EssentiaSync {
    private EssentiaSync() {
    }

    public static void changed(BlockEntity entity) {
        entity.setChanged();
        if (entity.getLevel() != null && !entity.getLevel().isClientSide) {
            entity.getLevel().sendBlockUpdated(
                    entity.getBlockPos(),
                    entity.getBlockState(),
                    entity.getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }
}
