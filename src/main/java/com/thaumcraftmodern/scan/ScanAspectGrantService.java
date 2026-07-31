package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Applies server-owned scan rewards. The first scanned occurrence of an
 * aspect grants the configured target amount plus the discovery bonus.
 */
final class ScanAspectGrantService {
    static final int FIRST_DISCOVERY_BONUS = 3;

    private ScanAspectGrantService() {
    }

    static List<Grant> apply(
            PlayerThaumKnowledge knowledge,
            List<AspectReward> rewards
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        Objects.requireNonNull(rewards, "rewards");

        Map<String, Integer> combined = new LinkedHashMap<>();
        for (AspectReward reward : rewards) {
            Objects.requireNonNull(reward, "reward");
            combined.merge(reward.aspectId(), reward.amount(), Math::addExact);
        }

        List<Grant> grants = new ArrayList<>(combined.size());
        combined.forEach((aspectId, targetAmount) -> {
            boolean newlyDiscovered = !knowledge.knowsAspect(aspectId);
            int grantedAmount = Math.addExact(
                    targetAmount,
                    newlyDiscovered ? FIRST_DISCOVERY_BONUS : 0
            );
            boolean learned = knowledge.addAspectPoints(
                    aspectId,
                    grantedAmount
            );
            if (learned != newlyDiscovered) {
                throw new IllegalStateException(
                        "aspect discovery state changed while granting "
                                + aspectId
                );
            }
            grants.add(new Grant(
                    aspectId,
                    grantedAmount,
                    knowledge.aspectAmount(aspectId),
                    newlyDiscovered
            ));
        });
        return List.copyOf(grants);
    }

    record Grant(
            String aspectId,
            int amount,
            int total,
            boolean newlyDiscovered
    ) {
    }
}
