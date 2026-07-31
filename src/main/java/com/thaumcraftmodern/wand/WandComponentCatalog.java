package com.thaumcraftmodern.wand;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable rod/cap lookup built atomically by the resource reload listener.
 */
public final class WandComponentCatalog {
    private final Map<String, WandRodDefinition> rodsById;
    private final Map<String, WandCapDefinition> capsById;
    private final List<WandRodDefinition> rods;
    private final List<WandCapDefinition> caps;

    public WandComponentCatalog(
            Collection<WandRodDefinition> rods,
            Collection<WandCapDefinition> caps
    ) {
        Objects.requireNonNull(rods, "rods");
        Objects.requireNonNull(caps, "caps");
        this.rodsById = immutableIndex(rods, WandRodDefinition::id, "rod");
        this.capsById = immutableIndex(caps, WandCapDefinition::id, "cap");
        if (rodsById.isEmpty()) {
            throw new IllegalArgumentException("wand component catalog has no rods");
        }
        if (capsById.isEmpty()) {
            throw new IllegalArgumentException("wand component catalog has no caps");
        }
        this.rods = List.copyOf(rodsById.values());
        this.caps = List.copyOf(capsById.values());
    }

    public Optional<WandRodDefinition> rod(String id) {
        return Optional.ofNullable(id == null ? null : rodsById.get(id));
    }

    public Optional<WandCapDefinition> cap(String id) {
        return Optional.ofNullable(id == null ? null : capsById.get(id));
    }

    public List<WandRodDefinition> rods() {
        return rods;
    }

    public List<WandCapDefinition> caps() {
        return caps;
    }

    private static <T> Map<String, T> immutableIndex(
            Collection<T> values,
            java.util.function.Function<T, String> id,
            String kind
    ) {
        LinkedHashMap<String, T> indexed = new LinkedHashMap<>();
        values.stream()
                .sorted(java.util.Comparator.comparing(id))
                .forEach(value -> {
                    Objects.requireNonNull(value, kind + " definitions cannot contain null");
                    String key = id.apply(value);
                    if (indexed.putIfAbsent(key, value) != null) {
                        throw new IllegalArgumentException(
                                "duplicate wand " + kind + " id: " + key
                        );
                    }
                });
        return Collections.unmodifiableMap(indexed);
    }
}
