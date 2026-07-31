package com.thaumcraftmodern.client;

import com.thaumcraftmodern.scan.AspectReward;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Applies the TC4 Goggles rule without removing a node's visible vis amount:
 * unknown aspects keep their amount and color, but use the question-mark
 * texture instead of revealing the aspect icon.
 */
final class ClientNodeAspectMask {
    private ClientNodeAspectMask() {
    }

    static List<DisplayAspect> apply(
            List<AspectReward> aspects,
            Predicate<String> knowsAspect
    ) {
        Objects.requireNonNull(aspects, "aspects");
        Objects.requireNonNull(knowsAspect, "knowsAspect");
        return aspects.stream()
                .map(aspect -> new DisplayAspect(
                        aspect.aspectId(),
                        aspect.amount(),
                        knowsAspect.test(aspect.aspectId())
                ))
                .toList();
    }

    record DisplayAspect(String aspectId, int amount, boolean known) {
        DisplayAspect {
            Objects.requireNonNull(aspectId, "aspectId");
            if (aspectId.isBlank()) {
                throw new IllegalArgumentException("aspectId cannot be blank");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("amount cannot be negative");
            }
        }
    }
}
