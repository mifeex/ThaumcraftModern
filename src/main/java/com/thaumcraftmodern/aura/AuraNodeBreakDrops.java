package com.thaumcraftmodern.aura;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lossless port of TC4 BlockAiry.harvestBlock's aura-node essence loop.
 */
public final class AuraNodeBreakDrops {
    public static final int MINIMUM_ASPECT_AMOUNT = 5;
    public static final int ESSENCE_ASPECT_AMOUNT = 2;

    private AuraNodeBreakDrops() {
    }

    public static List<PrimalAspect> aspectsForDrops(AuraNodeState state) {
        Objects.requireNonNull(state, "state");
        AuraNodeState.Snapshot snapshot = state.snapshot();
        List<PrimalAspect> drops = new ArrayList<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int amount = snapshot.current().get(aspect);
            if (amount < MINIMUM_ASPECT_AMOUNT) {
                continue;
            }
            int count = amount / 10 + 1;
            for (int index = 0; index < count; index++) {
                drops.add(aspect);
            }
        }
        return List.copyOf(drops);
    }

    public static List<String> aspectIdsForDrops(AuraNodeState state) {
        Objects.requireNonNull(state, "state");
        AuraNodeState.Snapshot snapshot = state.snapshot();
        List<String> drops = new ArrayList<>();
        snapshot.aspectsCurrent().forEach((aspect, amount) -> {
            if (amount >= MINIMUM_ASPECT_AMOUNT) {
                int count = amount / 10 + 1;
                for (int index = 0; index < count; index++) {
                    drops.add(aspect);
                }
            }
        });
        return List.copyOf(drops);
    }
}
