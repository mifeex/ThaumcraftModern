package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Direct modern port of Forge 1.12 BlockFluidFinite's eight-quanta cellular
 * automaton. Positive-density goo flows down; negative-density gas uses the
 * same rules with the vertical direction reversed.
 */
final class FiniteFluxFlow {
    static final int QUANTA_PER_BLOCK = 8;

    private FiniteFluxFlow() {
    }

    static void tick(
            ServerLevel level,
            BlockPos position,
            Block block,
            IntegerProperty amountProperty,
            Direction verticalDirection,
            int density,
            int tickDelay,
            RandomSource random
    ) {
        BlockState state = level.getBlockState(position);
        if (!state.is(block)) {
            return;
        }
        int amountRemaining = state.getValue(amountProperty) + 1;
        int previousAmount = amountRemaining;
        amountRemaining = flowVertically(
                level,
                position,
                state,
                block,
                amountProperty,
                verticalDirection,
                density,
                tickDelay,
                amountRemaining
        );
        if (amountRemaining < 1) {
            return;
        }
        if (amountRemaining != previousAmount) {
            if (amountRemaining == 1) {
                setAmount(
                        level,
                        position,
                        state,
                        amountProperty,
                        amountRemaining,
                        tickDelay
                );
                return;
            }
        } else if (amountRemaining == 1) {
            return;
        }

        int lowerThan = amountRemaining - 1;
        int total = amountRemaining;
        int count = 1;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = position.relative(direction);
            int adjacentAmount = amountBelow(
                    level,
                    adjacent,
                    block,
                    amountProperty,
                    lowerThan
            );
            if (adjacentAmount >= 0) {
                count++;
                total += adjacentAmount;
            }
        }
        if (count == 1) {
            if (amountRemaining != previousAmount) {
                setAmount(
                        level,
                        position,
                        state,
                        amountProperty,
                        amountRemaining,
                        tickDelay
                );
            }
            return;
        }

        int each = total / count;
        int remainder = total % count;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = position.relative(direction);
            int adjacentAmount = amountBelow(
                    level,
                    adjacent,
                    block,
                    amountProperty,
                    lowerThan
            );
            if (adjacentAmount < 0) {
                continue;
            }
            int newAmount = each;
            if (remainder == count
                    || remainder > 1
                    && random.nextInt(count - remainder) != 0) {
                newAmount++;
                remainder--;
            }
            if (newAmount != adjacentAmount) {
                setAmount(
                        level,
                        adjacent,
                        block.defaultBlockState(),
                        amountProperty,
                        newAmount,
                        tickDelay
                );
            }
            count--;
        }
        if (remainder > 0) {
            each++;
        }
        setAmount(
                level,
                position,
                state,
                amountProperty,
                each,
                tickDelay
        );
    }

    private static int flowVertically(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            Block block,
            IntegerProperty amountProperty,
            Direction verticalDirection,
            int density,
            int tickDelay,
            int inputAmount
    ) {
        BlockPos target = position.relative(verticalDirection);
        if (level.isOutsideBuildHeight(target)) {
            level.removeBlock(position, false);
            return 0;
        }
        int targetAmount = amountBelow(
                level,
                target,
                block,
                amountProperty,
                QUANTA_PER_BLOCK
        );
        if (targetAmount >= 0) {
            int combined = targetAmount + inputAmount;
            if (combined > QUANTA_PER_BLOCK) {
                setAmount(
                        level,
                        target,
                        state,
                        amountProperty,
                        QUANTA_PER_BLOCK,
                        tickDelay
                );
                return combined - QUANTA_PER_BLOCK;
            }
            if (combined > 0) {
                setAmount(
                        level,
                        target,
                        state,
                        amountProperty,
                        combined,
                        tickDelay
                );
                level.removeBlock(position, false);
                return 0;
            }
            return inputAmount;
        }

        BlockState targetState = level.getBlockState(target);
        int targetDensity = density(targetState);
        boolean swap = verticalDirection == Direction.UP
                ? targetDensity > density
                : targetDensity < density;
        if (swap && isFlux(targetState)) {
            level.setBlock(
                    target,
                    state.setValue(amountProperty, inputAmount - 1),
                    3
            );
            level.setBlock(position, targetState, 3);
            schedule(level, target, block, tickDelay);
            level.scheduleTick(
                    position,
                    targetState.getBlock(),
                    targetState.is(com.thaumcraftmodern.registry.ModBlocks
                            .FLUX_GAS.get()) ? FluxGasBlock.FLOW_TICK_DELAY
                            : FluxGooBlock.FLOW_TICK_DELAY
            );
            return 0;
        }
        return inputAmount;
    }

    private static int amountBelow(
            ServerLevel level,
            BlockPos position,
            Block block,
            IntegerProperty amountProperty,
            int belowThis
    ) {
        BlockState state = level.getBlockState(position);
        int amount;
        if (state.is(block)) {
            amount = state.getValue(amountProperty) + 1;
        } else if (canDisplace(state)) {
            amount = 0;
        } else {
            return -1;
        }
        return amount >= belowThis ? -1 : amount;
    }

    private static boolean canDisplace(BlockState state) {
        return state.isAir()
                || state.canBeReplaced() && !isFlux(state)
                && state.getFluidState().isEmpty();
    }

    private static boolean isFlux(BlockState state) {
        return state.is(com.thaumcraftmodern.registry.ModBlocks.FLUX_GOO.get())
                || state.is(com.thaumcraftmodern.registry.ModBlocks
                        .FLUX_GAS.get());
    }

    private static int density(BlockState state) {
        if (state.is(com.thaumcraftmodern.registry.ModBlocks.FLUX_GOO.get())) {
            return FluxGooBlock.DENSITY;
        }
        if (state.is(com.thaumcraftmodern.registry.ModBlocks.FLUX_GAS.get())) {
            return FluxGasBlock.DENSITY;
        }
        return Integer.MAX_VALUE;
    }

    private static void setAmount(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            IntegerProperty amountProperty,
            int amount,
            int tickDelay
    ) {
        if (amount <= 0) {
            level.removeBlock(position, false);
            return;
        }
        BlockState placed = state.setValue(amountProperty, amount - 1);
        level.setBlock(position, placed, 3);
        schedule(level, position, placed.getBlock(), tickDelay);
    }

    static void schedule(
            ServerLevel level,
            BlockPos position,
            Block block,
            int tickDelay
    ) {
        level.scheduleTick(position, block, tickDelay);
    }
}
