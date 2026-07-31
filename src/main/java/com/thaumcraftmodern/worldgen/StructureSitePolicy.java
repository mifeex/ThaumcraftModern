package com.thaumcraftmodern.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Shared terrain acceptance rules for Thaumcraft world structures.
 */
final class StructureSitePolicy {
    private static final int DEFAULT_SUPPORT_DEPTH = 2;

    private StructureSitePolicy() {
    }

    static boolean hasDrySupportedFloor(
            WorldGenLevel level,
            BlockPos origin,
            int width,
            int depth,
            Rotation rotation
    ) {
        return drySupportedFloorFailure(
                level,
                origin,
                width,
                depth,
                rotation
        ) == null;
    }

    static String drySupportedFloorFailure(
            WorldGenLevel level,
            BlockPos origin,
            int width,
            int depth,
            Rotation rotation
    ) {
        for (int localX = 0; localX < width; localX++) {
            for (int localZ = 0; localZ < depth; localZ++) {
                BlockPos floor = rotated(origin, localX, 0, localZ, rotation);
                BlockPos support = floor.below();
                FluidState floorFluid = level.getFluidState(floor);
                FluidState supportFluid = level.getFluidState(support);
                BlockState supportState = level.getBlockState(support);
                if (!floorFluid.isEmpty()
                        || !supportFluid.isEmpty()
                        || supportState.isAir()
                        || !supportState.isFaceSturdy(
                                level,
                                support,
                                Direction.UP
                        )) {
                    return "invalid floor/support at " + floor
                            + " floor=" + level.getBlockState(floor)
                            + " support=" + supportState;
                }
                for (int supportDepth = 1;
                        supportDepth < DEFAULT_SUPPORT_DEPTH;
                        supportDepth++) {
                    BlockPos deeper = support.below(supportDepth);
                    BlockState deeperState = level.getBlockState(deeper);
                    if (!level.getFluidState(deeper).isEmpty()
                            || deeperState.isAir()
                            || deeperState.getCollisionShape(level, deeper)
                                    .isEmpty()) {
                        return "missing deep support at " + deeper
                                + " state=" + deeperState;
                    }
                }
            }
        }
        return null;
    }

    static boolean hasDryReplaceableClearance(
            WorldGenLevel level,
            BlockPos origin,
            int width,
            int depth,
            int height,
            Rotation rotation
    ) {
        return dryReplaceableClearanceFailure(
                level,
                origin,
                width,
                depth,
                height,
                rotation
        ) == null;
    }

    static String dryReplaceableClearanceFailure(
            WorldGenLevel level,
            BlockPos origin,
            int width,
            int depth,
            int height,
            Rotation rotation
    ) {
        for (int localX = 0; localX < width; localX++) {
            for (int localZ = 0; localZ < depth; localZ++) {
                for (int localY = 0; localY < height; localY++) {
                    BlockPos position = rotated(
                            origin,
                            localX,
                            localY,
                            localZ,
                            rotation
                    );
                    BlockState state = level.getBlockState(position);
                    if (!level.getFluidState(position).isEmpty()
                            || (!state.isAir()
                                    && !state.canBeReplaced()
                                    && !state.is(Blocks.STRUCTURE_VOID))) {
                        return "blocked clearance at " + position
                                + " state=" + state;
                    }
                }
            }
        }
        return null;
    }

    static boolean hasDryDeepSupport(
            WorldGenLevel level,
            BlockPos surface,
            int depth
    ) {
        if (!level.getFluidState(surface.above()).isEmpty()) {
            return false;
        }
        for (int offset = 0; offset < depth; offset++) {
            BlockPos support = surface.below(offset);
            BlockState state = level.getBlockState(support);
            if (!level.getFluidState(support).isEmpty()
                    || state.isAir()
                    || state.getCollisionShape(level, support).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    static BlockPos rotated(
            BlockPos origin,
            int localX,
            int localY,
            int localZ,
            Rotation rotation
    ) {
        return switch (rotation) {
            case NONE -> origin.offset(localX, localY, localZ);
            case CLOCKWISE_90 -> origin.offset(-localZ, localY, localX);
            case CLOCKWISE_180 -> origin.offset(-localX, localY, -localZ);
            case COUNTERCLOCKWISE_90 ->
                    origin.offset(localZ, localY, -localX);
        };
    }
}
