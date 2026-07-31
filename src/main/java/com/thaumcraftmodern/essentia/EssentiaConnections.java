package com.thaumcraftmodern.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public final class EssentiaConnections {
    private EssentiaConnections() {
    }

    public static Optional<EssentiaTransport> neighbour(
            Level level,
            BlockPos position,
            Direction side
    ) {
        BlockEntity entity = level.getBlockEntity(position.relative(side));
        if (!(entity instanceof EssentiaTransport transport)
                || !transport.isConnectable(side.getOpposite())) {
            return Optional.empty();
        }
        return Optional.of(transport);
    }

    public static boolean connected(
            Level level,
            BlockPos position,
            Direction side,
            EssentiaTransport local
    ) {
        return local.isConnectable(side)
                && neighbour(level, position, side).isPresent();
    }
}
