package com.thaumcraftmodern.world.block;

import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Visual-only test vine for the tainted cave set. It intentionally has no
 * spreading or exposure behavior; those rules belong to the later ecology
 * integration rather than an asset-preview block.
 */
public final class TaintedCaveVineBlock extends MultifaceBlock {
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public TaintedCaveVineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return spreader;
    }
}
