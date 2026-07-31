package com.thaumcraftmodern.aspect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable aspect lookup and direct-composition index.
 */
public final class AspectCatalog {
    private final Map<String, AspectDefinition> definitionsById;
    private final Map<ComponentPair, AspectDefinition> compositions;
    private final List<AspectDefinition> definitions;
    private final Set<String> ids;

    public AspectCatalog(Collection<AspectDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");

        LinkedHashMap<String, AspectDefinition> byId = new LinkedHashMap<>();
        for (AspectDefinition definition : definitions) {
            Objects.requireNonNull(definition, "definitions cannot contain null");
            AspectDefinition previous = byId.putIfAbsent(definition.id(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate aspect id: " + definition.id());
            }
        }
        if (byId.isEmpty()) {
            throw new IllegalArgumentException("an aspect catalog cannot be empty");
        }

        LinkedHashMap<ComponentPair, AspectDefinition> byComposition = new LinkedHashMap<>();
        for (AspectDefinition definition : byId.values()) {
            if (!definition.isCompound()) {
                continue;
            }

            String first = definition.components().get(0);
            String second = definition.components().get(1);
            if (!byId.containsKey(first) || !byId.containsKey(second)) {
                throw new IllegalArgumentException(
                        "aspect " + definition.id() + " references an unknown component");
            }

            ComponentPair pair = ComponentPair.of(first, second);
            AspectDefinition previous = byComposition.putIfAbsent(pair, definition);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate composition " + first + " + " + second
                                + " for " + previous.id() + " and " + definition.id());
            }
        }

        this.definitionsById = Collections.unmodifiableMap(byId);
        this.compositions = Collections.unmodifiableMap(byComposition);
        this.definitions = List.copyOf(byId.values());
        this.ids = Collections.unmodifiableSet(byId.keySet());
    }

    public Optional<AspectDefinition> lookup(String id) {
        return Optional.ofNullable(id == null ? null : definitionsById.get(id));
    }

    public Optional<AspectDefinition> compositionResult(String firstId, String secondId) {
        if (firstId == null || secondId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(compositions.get(ComponentPair.of(firstId, secondId)));
    }

    /**
     * Returns true only when either aspect directly declares the other as one
     * of its two components. Components that merely share a compound are not
     * related, and the relationship is not transitive.
     */
    public boolean related(String firstId, String secondId) {
        if (firstId == null || secondId == null || firstId.equals(secondId)) {
            return false;
        }

        AspectDefinition first = definitionsById.get(firstId);
        AspectDefinition second = definitionsById.get(secondId);
        if (first == null || second == null) {
            return false;
        }
        return first.hasDirectComponent(secondId) || second.hasDirectComponent(firstId);
    }

    public List<AspectDefinition> definitions() {
        return definitions;
    }

    public Set<String> ids() {
        return ids;
    }

    private record ComponentPair(String first, String second) {
        private static ComponentPair of(String first, String second) {
            Objects.requireNonNull(first, "first");
            Objects.requireNonNull(second, "second");
            return first.compareTo(second) <= 0
                    ? new ComponentPair(first, second)
                    : new ComponentPair(second, first);
        }
    }
}
