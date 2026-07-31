package com.thaumcraftmodern.aspect;

import java.util.Locale;
import java.util.Objects;

/**
 * One exact aspect requirement shown by Thaumonomicon recipe and action
 * pages. The id is intentionally not limited to primal aspects.
 */
public record AspectCost(String aspectId, int amount) {
    public AspectCost {
        Objects.requireNonNull(aspectId, "aspectId");
        if (aspectId.isBlank()
                || !aspectId.equals(aspectId.trim())
                || !aspectId.equals(aspectId.toLowerCase(Locale.ROOT))
                || aspectId.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "aspectId must be non-blank, trimmed and lowercase"
            );
        }
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "aspect cost must be positive: " + aspectId
            );
        }
    }
}
