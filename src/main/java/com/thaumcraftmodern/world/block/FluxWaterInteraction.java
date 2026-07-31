package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;

/**
 * Shared player-requested water washing for finite Flux Goo and Flux Gas.
 */
public final class FluxWaterInteraction {
    /**
     * Levels 0..4 are at most five of the eight finite-fluid quanta.
     * Levels 5..7 resist flowing water and require a source.
     */
    public static final int MAX_FLOWING_WATER_LEVEL = 4;

    private FluxWaterInteraction() {
    }

    public static boolean mayWash(
            int fluxLevel,
            boolean sourceWater,
            boolean flowingWater
    ) {
        return sourceWater
                || flowingWater
                && fluxLevel <= MAX_FLOWING_WATER_LEVEL;
    }

    static boolean washFromNeighbour(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            IntegerProperty levelProperty
    ) {
        boolean flowingWater = false;
        for (Direction direction : new Direction[]{
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        }) {
            BlockPos waterPosition = position.relative(direction);
            FluidState fluid = level.getFluidState(waterPosition);
            if (!fluid.is(FluidTags.WATER)) {
                continue;
            }
            if (fluid.isSource()) {
                removeFlux(level, position, waterPosition, fluid);
                return true;
            }
            flowingWater = true;
        }
        if (!mayWash(
                state.getValue(levelProperty),
                false,
                flowingWater
        )) {
            return false;
        }
        level.removeBlock(position, false);
        return true;
    }

    static boolean mayReplaceDirectly(
            int fluxLevel,
            FluidState water
    ) {
        return water.is(FluidTags.WATER)
                && mayWash(fluxLevel, water.isSource(), !water.isSource());
    }

    private static void removeFlux(
            ServerLevel level,
            BlockPos position,
            BlockPos waterPosition,
            FluidState water
    ) {
        level.removeBlock(position, false);
        level.scheduleTick(
                waterPosition,
                water.getType(),
                water.getType().getTickDelay(level)
        );
    }
}
