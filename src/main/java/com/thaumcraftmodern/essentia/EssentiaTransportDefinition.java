package com.thaumcraftmodern.essentia;

import net.minecraft.resources.ResourceLocation;

/** Data-driven routing flags for an essentia-capable block. */
public record EssentiaTransportDefinition(
        ResourceLocation block,
        boolean canReturnEssentia
) {
}
