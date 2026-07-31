package com.thaumcraftmodern.worldgen;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TC4 aura-totem vertical layout and its one-in-four early cap roll.
 */
final class AuraTotemGeneration {
    static final int MAX_NODE_HEIGHT = 5;
    static final int MIN_LEAF_SEARCH_Y = 40;

    private AuraTotemGeneration() {
    }

    static boolean isNodeLevel(int height, int oneInFourRoll) {
        return height > 1 && oneInFourRoll == 0;
    }

    static boolean isSurfaceCover(BlockState state) {
        return state.is(Blocks.SNOW)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.FERN);
    }

    static boolean isReplaceable(BlockState state) {
        return acceptsReplaceable(state.isAir(), isSurfaceCover(state));
    }

    static boolean isValidBase(BlockState state) {
        return acceptsBase(
                state.is(Blocks.GRASS_BLOCK),
                state.is(Blocks.SAND) || state.is(Blocks.RED_SAND),
                state.is(BlockTags.DIRT),
                state.is(BlockTags.BASE_STONE_OVERWORLD),
                state.is(Blocks.NETHERRACK)
        );
    }

    static boolean acceptsReplaceable(boolean air, boolean surfaceCover) {
        return air || surfaceCover;
    }

    static boolean acceptsBase(
            boolean grass,
            boolean sand,
            boolean dirt,
            boolean stone,
            boolean netherrack
    ) {
        return grass || sand || dirt || stone || netherrack;
    }
}
