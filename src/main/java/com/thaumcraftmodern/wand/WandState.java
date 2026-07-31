package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalVis;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable wand composition and six-primal vis state. Stored amounts are
 * centivis: 100 units equal one whole vis, as in TC4.
 */
public record WandState(
        int version,
        String rodId,
        String capId,
        Map<PrimalAspect, Integer> visCentivis
) {
    public WandState {
        if (version <= 0) {
            throw new IllegalArgumentException("wand state version must be positive");
        }
        rodId = requireId(rodId, "rodId");
        capId = requireId(capId, "capId");
        visCentivis = PrimalVis.exact(visCentivis, "wand vis");
    }

    public static WandState empty(int version, String rodId, String capId) {
        return new WandState(version, rodId, capId, PrimalVis.uniform(0));
    }

    public int visCentivis(PrimalAspect aspect) {
        return visCentivis.get(Objects.requireNonNull(aspect, "aspect"));
    }

    public WandState withVisCentivis(Map<PrimalAspect, Integer> values) {
        return new WandState(version, rodId, capId, values);
    }

    public WandState withVisCentivis(PrimalAspect aspect, int amount) {
        EnumMap<PrimalAspect, Integer> next =
                new EnumMap<>(PrimalVis.exact(visCentivis, "wand vis"));
        next.put(Objects.requireNonNull(aspect, "aspect"), amount);
        return withVisCentivis(next);
    }

    private static String requireId(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}
