package com.thaumcraftmodern.essentia.tube;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aura.PrimalAspect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Pure rules for wand-releasing one clogged point of tube essentia. */
public final class TubeEssentiaReleaseRules {
    public static final int FLUX_THRESHOLD = 8;

    public enum Complexity {
        PRIMAL(1, 125),
        PRIMAL_COMPOUND(2, 250),
        MIXED_COMPOUND(3, 500),
        COMPLEX_COMPOUND(4, 750);

        private final int risk;
        private final int visCentivis;

        Complexity(int risk, int visCentivis) {
            this.risk = risk;
            this.visCentivis = visCentivis;
        }

        public int risk() {
            return risk;
        }

        public int visCentivis() {
            return visCentivis;
        }
    }

    public record Release(Complexity complexity, int accumulatedRisk,
            boolean createsFlux) {
        public Release {
            complexity = Objects.requireNonNull(complexity, "complexity");
            accumulatedRisk = Math.max(0, accumulatedRisk);
        }
    }

    private TubeEssentiaReleaseRules() {
    }

    public static Complexity complexity(AspectCatalog catalog, String aspectId) {
        Objects.requireNonNull(catalog, "catalog");
        AspectDefinition aspect = catalog.lookup(aspectId).orElseThrow(() ->
                new IllegalArgumentException("unknown aspect: " + aspectId));
        if (aspect.isPrimal()) return Complexity.PRIMAL;

        int primalComponents = 0;
        for (String componentId : aspect.components()) {
            AspectDefinition component = catalog.lookup(componentId).orElseThrow(() ->
                    new IllegalArgumentException(
                            "unknown component " + componentId + " of " + aspectId));
            if (component.isPrimal()) primalComponents++;
        }
        return switch (primalComponents) {
            case 2 -> Complexity.PRIMAL_COMPOUND;
            case 1 -> Complexity.MIXED_COMPOUND;
            default -> Complexity.COMPLEX_COMPOUND;
        };
    }

    public static Release accumulate(int previousRisk, Complexity complexity) {
        int total = Math.max(0, previousRisk)
                + Objects.requireNonNull(complexity, "complexity").risk();
        return new Release(complexity, total, total > FLUX_THRESHOLD);
    }

    public static Map<String, Integer> baseVisCostCentivis(
            Complexity complexity
    ) {
        int cost = Objects.requireNonNull(complexity, "complexity").visCentivis();
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            values.put(aspect.id(), cost);
        }
        return Map.copyOf(values);
    }
}
