package com.thaumcraftmodern.essentia;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Reload-safe lookup used by transport endpoints, independent of tube types. */
public final class EssentiaTransportRegistry {
    private static volatile Map<ResourceLocation, EssentiaTransportDefinition> definitions = Map.of();

    private EssentiaTransportRegistry() {
    }

    public static synchronized void replace(
            Collection<EssentiaTransportDefinition> replacements) {
        Map<ResourceLocation, EssentiaTransportDefinition> next =
                new ConcurrentHashMap<>();
        for (EssentiaTransportDefinition definition : replacements) {
            if (next.put(definition.block(), definition) != null) {
                throw new IllegalArgumentException(
                        "Duplicate essentia transport block " + definition.block());
            }
        }
        definitions = Map.copyOf(next);
    }

    public static boolean canReturnEssentia(ResourceLocation block) {
        EssentiaTransportDefinition definition = definitions.get(block);
        return definition == null || definition.canReturnEssentia();
    }
}
