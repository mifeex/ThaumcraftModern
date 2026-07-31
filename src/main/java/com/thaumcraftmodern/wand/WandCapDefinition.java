package com.thaumcraftmodern.wand;

import java.util.Objects;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Data-driven wand cap properties. The multiplier is applied to the base
 * centivis cost with the same positive-value truncation used by TC4.
 */
public record WandCapDefinition(
        String id,
        float costModifier,
        String translationKey,
        List<String> specialAspects,
        float specialCostModifier
) {
    private static final Pattern STABLE_ID = Pattern.compile("[a-z0-9_.-]+");

    public WandCapDefinition {
        id = Objects.requireNonNull(id, "id");
        translationKey = Objects.requireNonNull(translationKey, "translationKey");
        specialAspects = List.copyOf(Objects.requireNonNull(
                specialAspects,
                "specialAspects"
        ));
        if (!STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("invalid wand cap id: " + id);
        }
        if (!Float.isFinite(costModifier) || costModifier <= 0.0F) {
            throw new IllegalArgumentException(
                    "wand cap cost modifier must be finite and positive: " + id
            );
        }
        if (translationKey.isBlank()) {
            throw new IllegalArgumentException(
                    "wand cap translation key cannot be blank: " + id
            );
        }
        if (specialAspects.stream().anyMatch(
                aspect -> aspect == null || aspect.isBlank()
        )) {
            throw new IllegalArgumentException(
                    "wand cap special aspects cannot be blank: " + id
            );
        }
        if (specialAspects.isEmpty()) {
            specialCostModifier = costModifier;
        } else if (!Float.isFinite(specialCostModifier)
                || specialCostModifier <= 0.0F) {
            throw new IllegalArgumentException(
                    "wand cap special modifier must be finite and positive: "
                            + id
            );
        }
    }

    public WandCapDefinition(
            String id,
            float costModifier,
            String translationKey
    ) {
        this(id, costModifier, translationKey, List.of(), costModifier);
    }

    public int adjustCentivis(int baseCentivis) {
        return adjustCentivis(baseCentivis, 0, "");
    }

    /**
     * Applies the classic TC4 order of operations: subtract the player's
     * percentage discount from the cap multiplier, clamp the resulting
     * multiplier to {@code 0.1}, multiply centivis, then truncate.
     */
    public int adjustCentivis(int baseCentivis, int visDiscountPercent) {
        return adjustCentivis(baseCentivis, visDiscountPercent, "");
    }

    public int adjustCentivis(
            int baseCentivis,
            int visDiscountPercent,
            String aspectId
    ) {
        if (baseCentivis < 0) {
            throw new IllegalArgumentException("base centivis cannot be negative");
        }
        float effectiveModifier = Math.max(
                modifierFor(aspectId) - visDiscountPercent / 100.0F,
                0.1F
        );
        float adjusted = baseCentivis * effectiveModifier;
        if (!Float.isFinite(adjusted) || adjusted > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("adjusted centivis exceeds integer range");
        }
        return (int) adjusted;
    }

    public float modifierFor(String aspectId) {
        return aspectId != null && specialAspects.contains(aspectId)
                ? specialCostModifier
                : costModifier;
    }
}
