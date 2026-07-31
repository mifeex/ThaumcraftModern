package com.thaumcraftmodern.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Safe ordinary-game acquisition hook for placing a newly created node.
 * Worldgen, a research reward, or a controlled in-world interaction may call
 * this method; debug commands are not required by the API.
 */
public final class AuraNodeAcquisitionService {
    private AuraNodeAcquisitionService() {
    }

    public static Result place(
            ServerLevel level,
            BlockPos position,
            BlockState nodeBlock,
            AuraNodeState state
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(nodeBlock, "nodeBlock");
        Objects.requireNonNull(state, "state");

        if (!level.hasChunkAt(position)) {
            return Result.CHUNK_NOT_LOADED;
        }
        if (!level.getWorldBorder().isWithinBounds(position)) {
            return Result.OUTSIDE_WORLD_BORDER;
        }
        BlockState previous = level.getBlockState(position);
        if (!previous.isAir()) {
            return Result.TARGET_NOT_EMPTY;
        }
        if (!level.setBlock(position, nodeBlock, Block.UPDATE_ALL)) {
            return Result.BLOCK_PLACEMENT_FAILED;
        }

        BlockEntity created = level.getBlockEntity(position);
        if (!(created instanceof AuraNodeBlockEntity node)
                || !node.initializeOnce(state)) {
            level.setBlock(position, previous, Block.UPDATE_ALL);
            return Result.BLOCK_ENTITY_INITIALIZATION_FAILED;
        }
        return Result.PLACED;
    }

    public enum Result {
        PLACED,
        CHUNK_NOT_LOADED,
        OUTSIDE_WORLD_BORDER,
        TARGET_NOT_EMPTY,
        BLOCK_PLACEMENT_FAILED,
        BLOCK_ENTITY_INITIALIZATION_FAILED
    }
}
