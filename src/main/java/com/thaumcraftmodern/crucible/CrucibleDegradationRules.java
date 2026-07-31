package com.thaumcraftmodern.crucible;

import com.thaumcraftmodern.aspect.AspectDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * Runtime-independent port of TC4 {@code TileCrucible}'s periodic aspect
 * degradation.
 */
public final class CrucibleDegradationRules {
    public static final int WATER_COST_MB = 2;
    public static final long INTERVAL_TICKS = 100L;

    private CrucibleDegradationRules() {
    }

    /**
     * Removes one random aspect. TC4 retries the random selection once when
     * the first choice is primal, then either returns one direct component of
     * a compound aspect or requests a spill for a final primal choice.
     */
    public static Result degradeOne(
            EssentiaStore essentia,
            Function<String, Optional<AspectDefinition>> definitions,
            IntUnaryOperator nextIndex,
            BooleanSupplier chooseFirstComponent
    ) {
        if (essentia.isEmpty()) {
            return Result.EMPTY;
        }
        List<String> aspects = new ArrayList<>(essentia.view().keySet());
        String selected = aspects.get(nextIndex.applyAsInt(aspects.size()));
        if (definitions.apply(selected)
                .map(AspectDefinition::isPrimal)
                .orElse(true)) {
            selected = aspects.get(nextIndex.applyAsInt(aspects.size()));
        }

        essentia.remove(selected, 1);
        Optional<AspectDefinition> definition = definitions.apply(selected);
        if (definition.isPresent() && definition.get().isCompound()) {
            List<String> components = definition.get().components();
            String component = components.get(
                    chooseFirstComponent.getAsBoolean() ? 0 : 1
            );
            essentia.add(component, 1);
            return new Result(true, false, selected, component);
        }
        return new Result(true, true, selected, null);
    }

    public record Result(
            boolean changed,
            boolean spill,
            String removedAspect,
            String addedComponent
    ) {
        private static final Result EMPTY =
                new Result(false, false, null, null);
    }
}
