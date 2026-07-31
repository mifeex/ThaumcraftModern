package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure TC4 node-bullying transaction, separated for deterministic tests.
 */
public final class ClassicNodeDischarge {
    private ClassicNodeDischarge() {
    }

    public static Optional<Result> tryTransfer(
            AuraNodeState.Snapshot predator,
            AuraNodeState.Snapshot victim,
            RandomSource random,
            boolean shiny
    ) {
        int predatorAverage = averagePool(predator);
        int victimAverage = averagePool(victim);
        List<String> victimAspects = victim.aspectsCurrent().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
        if (victimAverage >= predatorAverage || victimAspects.isEmpty()) {
            return Optional.empty();
        }

        String aspect = victimAspects.get(random.nextInt(victimAspects.size()));
        Map<String, Integer> victimCurrent =
                new LinkedHashMap<>(victim.aspectsCurrent());
        Map<String, Integer> victimMaximum =
                new LinkedHashMap<>(victim.aspectsMaximum());
        victimCurrent.put(aspect, victimCurrent.get(aspect) - 1);

        Map<String, Integer> predatorCurrent =
                new LinkedHashMap<>(predator.aspectsCurrent());
        Map<String, Integer> predatorMaximum =
                new LinkedHashMap<>(predator.aspectsMaximum());
        predatorCurrent.putIfAbsent(aspect, 0);
        predatorMaximum.putIfAbsent(aspect, 0);
        if (predatorCurrent.get(aspect) < predatorMaximum.get(aspect)) {
            predatorCurrent.put(
                    aspect,
                    predatorCurrent.get(aspect) + 1
            );
        } else {
            int base = predatorMaximum.get(aspect);
            int bound = 1 + (int) (base / (shiny ? 1.5D : 1.0D));
            if (random.nextInt(Math.max(1, bound)) == 0) {
                predatorMaximum.put(aspect, base + 1);
                if (random.nextInt(3) == 0) {
                    victimMaximum.put(
                            aspect,
                            Math.max(
                                    victimCurrent.get(aspect),
                                    victimMaximum.get(aspect) - 1
                            )
                    );
                }
            }
        }
        return Optional.of(new Result(
                aspect,
                predatorCurrent,
                predatorMaximum,
                victimCurrent,
                victimMaximum
        ));
    }

    static int averagePool(AuraNodeState.Snapshot snapshot) {
        int current = snapshot.aspectsCurrent().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int maximum = snapshot.aspectsMaximum().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return (current + maximum) / 2;
    }

    public record Result(
            String aspect,
            Map<String, Integer> predatorCurrent,
            Map<String, Integer> predatorMaximum,
            Map<String, Integer> victimCurrent,
            Map<String, Integer> victimMaximum
    ) {
        public Result {
            predatorCurrent = Map.copyOf(predatorCurrent);
            predatorMaximum = Map.copyOf(predatorMaximum);
            victimCurrent = Map.copyOf(victimCurrent);
            victimMaximum = Map.copyOf(victimMaximum);
        }
    }
}
