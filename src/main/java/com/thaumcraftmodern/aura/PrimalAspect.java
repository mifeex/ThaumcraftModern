package com.thaumcraftmodern.aura;

import java.util.List;
import java.util.Locale;

/**
 * The six primal aspects in the stable order used by classic wand and node
 * storage. Keeping this order in one place makes NBT, tooltips and network
 * payloads deterministic.
 */
public enum PrimalAspect {
    AER,
    TERRA,
    IGNIS,
    AQUA,
    ORDO,
    PERDITIO;

    private static final List<PrimalAspect> ORDERED = List.of(values());

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static List<PrimalAspect> ordered() {
        return ORDERED;
    }

    public static PrimalAspect fromId(String id) {
        for (PrimalAspect aspect : values()) {
            if (aspect.id().equals(id)) {
                return aspect;
            }
        }
        throw new IllegalArgumentException("unknown primal aspect: " + id);
    }
}
