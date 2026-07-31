package com.thaumcraftmodern.aura;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Validation and copy helpers for exact six-primal vis maps.
 */
public final class PrimalVis {
    private PrimalVis() {
    }

    public static Map<PrimalAspect, Integer> uniform(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("vis amount cannot be negative");
        }
        EnumMap<PrimalAspect, Integer> values = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            values.put(aspect, amount);
        }
        return immutable(values);
    }

    public static Map<PrimalAspect, Integer> exact(
            Map<PrimalAspect, Integer> values,
            String field
    ) {
        Objects.requireNonNull(values, field);
        EnumMap<PrimalAspect, Integer> copy = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            Integer amount = values.get(aspect);
            if (amount == null) {
                throw new IllegalArgumentException(field + " is missing " + aspect.id());
            }
            if (amount < 0) {
                throw new IllegalArgumentException(
                        field + " contains negative " + aspect.id() + ": " + amount
                );
            }
            copy.put(aspect, amount);
        }
        if (copy.size() != values.size()) {
            throw new IllegalArgumentException(field + " contains non-primal entries");
        }
        return immutable(copy);
    }

    public static EnumMap<PrimalAspect, Integer> mutableCopy(
            Map<PrimalAspect, Integer> values
    ) {
        return new EnumMap<>(exact(values, "vis"));
    }

    private static Map<PrimalAspect, Integer> immutable(
            EnumMap<PrimalAspect, Integer> values
    ) {
        return Collections.unmodifiableMap(new EnumMap<>(values));
    }
}
