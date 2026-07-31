package com.thaumcraftmodern.scan;

import java.util.Locale;
import java.util.Objects;

public record AspectReward(String aspectId, int amount) {
    public AspectReward {
        Objects.requireNonNull(aspectId, "aspectId");
        if (aspectId.isBlank()
                || !aspectId.equals(aspectId.trim())
                || !aspectId.equals(aspectId.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("aspectId must be non-blank, trimmed and lowercase");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("aspect reward amount must be positive");
        }
    }
}
