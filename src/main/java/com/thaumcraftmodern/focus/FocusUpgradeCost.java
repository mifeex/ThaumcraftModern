package com.thaumcraftmodern.focus;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.PrimalAspect;
import java.util.EnumMap;
import java.util.Map;

/** Reduces TC4 compound upgrade aspects to the six vis-network primals. */
public final class FocusUpgradeCost {
    private FocusUpgradeCost() {}

    public static EnumMap<PrimalAspect, Integer> primalCost(
            FocusUpgradeType upgrade, int rank) {
        EnumMap<PrimalAspect, Integer> result = new EnumMap<>(PrimalAspect.class);
        int multiplier = 200 << Math.max(0, rank - 1);
        upgrade.aspectCost().forEach((aspect, amount) ->
                reduce(aspect, amount * multiplier, result, 0));
        return result;
    }

    private static void reduce(String aspect, int amount,
                               Map<PrimalAspect, Integer> result, int depth) {
        if (depth > 32) throw new IllegalStateException("cyclic aspect: " + aspect);
        try {
            PrimalAspect primal = PrimalAspect.fromId(aspect);
            result.merge(primal, amount, Integer::sum);
            return;
        } catch (IllegalArgumentException ignored) {
            // Compound aspect: recursively split it exactly as TC4 does.
        }
        AspectDefinition definition = AspectRegistryRuntime.find(aspect)
                .orElseThrow(() -> new IllegalStateException("unknown aspect: " + aspect));
        if (!definition.isCompound())
            throw new IllegalStateException("non-primal aspect without components: " + aspect);
        for (String component : definition.components())
            reduce(component, amount, result, depth + 1);
    }
}
