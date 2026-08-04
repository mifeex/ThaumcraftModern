package com.thaumcraftmodern.wand;

import java.util.Objects;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Data-driven wand rod properties. Capacity is measured in whole vis per
 * primal aspect, matching the public TC4 wand API.
 */
public record WandRodDefinition(
        String id,
        int capacityVis,
        String translationKey,
        int craftCostVis,
        String researchId,
        List<String> rechargeAspects,
        int rechargeIntervalTicks,
        int rechargeCentivis,
        boolean staff,
        boolean runes
) {
    private static final Pattern STABLE_ID = Pattern.compile("[a-z0-9_.-]+");

    public WandRodDefinition {
        id = Objects.requireNonNull(id, "id");
        translationKey = Objects.requireNonNull(translationKey, "translationKey");
        researchId = Objects.requireNonNull(researchId, "researchId");
        rechargeAspects = List.copyOf(Objects.requireNonNull(
                rechargeAspects,
                "rechargeAspects"
        ));
        if (!STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("invalid wand rod id: " + id);
        }
        if (capacityVis <= 0) {
            throw new IllegalArgumentException(
                    "wand rod capacity must be positive: " + id
            );
        }
        if (translationKey.isBlank()) {
            throw new IllegalArgumentException(
                    "wand rod translation key cannot be blank: " + id
            );
        }
        if (craftCostVis <= 0) {
            throw new IllegalArgumentException(
                    "wand rod craft cost must be positive: " + id
            );
        }
        if (researchId.isBlank()) {
            throw new IllegalArgumentException(
                    "wand rod research id cannot be blank: " + id
            );
        }
        if (rechargeAspects.stream().anyMatch(
                aspect -> aspect == null || aspect.isBlank()
        )) {
            throw new IllegalArgumentException(
                    "wand rod recharge aspects cannot be blank: " + id
            );
        }
        if (rechargeAspects.isEmpty()) {
            if (rechargeIntervalTicks != 0 || rechargeCentivis != 0) {
                throw new IllegalArgumentException(
                        "non-recharging wand rod cannot define recharge timing: "
                                + id
                );
            }
        } else if (rechargeIntervalTicks <= 0 || rechargeCentivis <= 0) {
            throw new IllegalArgumentException(
                    "recharging wand rod requires positive interval and amount: "
                            + id
            );
        }
        if (runes && !staff) {
            throw new IllegalArgumentException(
                    "wand runes are only valid for staff rods: " + id
            );
        }
    }

    public WandRodDefinition(
            String id,
            int capacityVis,
            String translationKey
    ) {
        this(id, capacityVis, translationKey, 1, "rod_" + id,
                List.of(), 0, 0, false, false);
    }
}
