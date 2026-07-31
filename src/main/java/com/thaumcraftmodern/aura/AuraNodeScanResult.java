package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.scan.AspectReward;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable server-derived node parameters exposed to the existing
 * Thaumometer completion/result path.
 *
 * <p>Every aspect actually present in a scanned node grants its full current
 * vis at ten or below. Above ten it grants ten percent, rounded to the nearest
 * whole point with positive half values rounded up.</p>
 */
public record AuraNodeScanResult(
        UUID nodeId,
        AuraNodeType type,
        AuraNodeModifier modifier,
        Map<PrimalAspect, Integer> current,
        Map<PrimalAspect, Integer> maximum,
        Map<String, Integer> aspectsCurrent,
        Map<String, Integer> aspectsMaximum,
        long revision,
        List<AspectReward> rewards
) {
    public AuraNodeScanResult {
        nodeId = Objects.requireNonNull(nodeId, "nodeId");
        type = Objects.requireNonNull(type, "type");
        modifier = Objects.requireNonNull(modifier, "modifier");
        current = PrimalVis.exact(current, "current");
        maximum = PrimalVis.exact(maximum, "maximum");
        aspectsCurrent = Map.copyOf(
                Objects.requireNonNull(aspectsCurrent, "aspectsCurrent")
        );
        aspectsMaximum = Map.copyOf(
                Objects.requireNonNull(aspectsMaximum, "aspectsMaximum")
        );
        if (!aspectsCurrent.keySet().equals(aspectsMaximum.keySet())) {
            throw new IllegalArgumentException(
                    "node aspect current and maximum keys must match"
            );
        }
        for (String aspect : aspectsCurrent.keySet()) {
            int aspectCurrent = aspectsCurrent.get(aspect);
            int aspectMaximum = aspectsMaximum.get(aspect);
            if (aspectCurrent < 0
                    || aspectMaximum < 0
                    || aspectCurrent > aspectMaximum) {
                throw new IllegalArgumentException(
                        "invalid node aspect pool " + aspect
                );
            }
        }
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            if (current.get(aspect) > maximum.get(aspect)) {
                throw new IllegalArgumentException(
                        aspect.id() + " current vis exceeds maximum"
                );
            }
        }
        if (revision < 0L) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
        rewards = List.copyOf(Objects.requireNonNull(rewards, "rewards"));
    }

    public AuraNodeScanResult(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            Map<PrimalAspect, Integer> current,
            Map<PrimalAspect, Integer> maximum,
            long revision,
            List<AspectReward> rewards
    ) {
        this(
                nodeId,
                type,
                modifier,
                current,
                maximum,
                primalIds(current),
                primalIds(maximum),
                revision,
                rewards
        );
    }

    public static AuraNodeScanResult from(AuraNodeState.Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<AspectReward> rewards = snapshot.aspectsCurrent().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new AspectReward(
                        entry.getKey(),
                        researchReward(entry.getValue())
                ))
                .toList();
        return new AuraNodeScanResult(
                snapshot.nodeId(),
                snapshot.type(),
                snapshot.modifier(),
                snapshot.current(),
                snapshot.maximum(),
                snapshot.aspectsCurrent(),
                snapshot.aspectsMaximum(),
                snapshot.revision(),
                rewards
        );
    }

    private static Map<String, Integer> primalIds(
            Map<PrimalAspect, Integer> values
    ) {
        java.util.LinkedHashMap<String, Integer> result =
                new java.util.LinkedHashMap<>();
        PrimalAspect.ordered().forEach(
                aspect -> result.put(aspect.id(), values.get(aspect))
        );
        return result;
    }

    static int researchReward(int currentVis) {
        if (currentVis <= 0) {
            throw new IllegalArgumentException("currentVis must be positive");
        }
        if (currentVis <= 10) {
            return currentVis;
        }
        return (int) ((currentVis + 5L) / 10L);
    }
}
