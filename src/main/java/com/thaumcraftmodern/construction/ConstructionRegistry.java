package com.thaumcraftmodern.construction;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class ConstructionRegistry {
    private static volatile Map<ConstructionDefinition.Handler, ConstructionDefinition>
            current = Map.of();

    private ConstructionRegistry() {
    }

    public static Optional<ConstructionDefinition> find(
            ConstructionDefinition.Handler handler
    ) {
        return Optional.ofNullable(current.get(handler));
    }

    public static Collection<ConstructionDefinition> all() {
        return current.values();
    }

    public static void replace(Collection<ConstructionDefinition> definitions) {
        EnumMap<ConstructionDefinition.Handler, ConstructionDefinition> next =
                new EnumMap<>(ConstructionDefinition.Handler.class);
        for (ConstructionDefinition definition : definitions) {
            ConstructionDefinition previous =
                    next.put(definition.handler(), definition);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate construction handler "
                                + definition.handler()
                                + " for " + previous.id()
                                + " and " + definition.id()
                );
            }
        }
        current = Map.copyOf(next);
    }
}
