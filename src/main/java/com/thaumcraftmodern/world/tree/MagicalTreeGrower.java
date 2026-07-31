package com.thaumcraftmodern.world.tree;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Objects;

/**
 * Connects both classic saplings to the same configured features used by
 * world generation, so bonemeal and natural growth never drift apart.
 */
public final class MagicalTreeGrower extends AbstractTreeGrower {
    private final ResourceKey<ConfiguredFeature<?, ?>> configuredFeature;

    public MagicalTreeGrower(
            ResourceKey<ConfiguredFeature<?, ?>> configuredFeature
    ) {
        this.configuredFeature = Objects.requireNonNull(
                configuredFeature,
                "configuredFeature"
        );
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
            RandomSource random,
            boolean hasFlowers
    ) {
        return configuredFeature;
    }
}
