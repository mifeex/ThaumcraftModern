package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Resolves the exact TC4 research-note ribbon colour from its primary tag. */
public final class ResearchColorResolver {
    public static final int UNKNOWN_COLOR = 0x999999;

    private ResearchColorResolver() {
    }

    public static int color(String researchId) {
        return ResearchRegistry.find(researchId)
                .map(ResearchDefinition::researchCost)
                .map(costs -> color(costs, AspectRegistryRuntime::find))
                .orElse(UNKNOWN_COLOR);
    }

    static int color(
            List<AspectCost> researchCost,
            Function<String, Optional<AspectDefinition>> aspects
    ) {
        if (researchCost == null || researchCost.isEmpty()) {
            return UNKNOWN_COLOR;
        }
        return aspects.apply(researchCost.get(0).aspectId())
                .map(AspectDefinition::color)
                .orElse(UNKNOWN_COLOR);
    }
}
