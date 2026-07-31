package com.thaumcraftmodern.world.block;

import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * A non-growing cave-vine head used to visually test tainted glow berries.
 * The default state keeps berries present so the placed test block immediately
 * shows its emissive texture.
 */
public final class TaintedGlowBerryVineBlock extends CaveVinesBlock {
    public TaintedGlowBerryVineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CaveVines.BERRIES, true));
    }
}
