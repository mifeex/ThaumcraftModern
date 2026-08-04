package com.thaumcraftmodern.deconstruction;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.minecraft.util.RandomSource;

/** Pure TC4 deconstruction rules, isolated for deterministic tests. */
public final class DeconstructionTableLogic {
    public static final int BREAK_TICKS = 40;
    public static final int DISCOVERY_ROLL_BOUND = 80;

    private DeconstructionTableLogic() {
    }

    public static Map<String, Integer> reduceToPrimals(
            Map<String, Integer> aspects,
            AspectCatalog catalog
    ) {
        LinkedHashMap<String, Integer> primals = new LinkedHashMap<>();
        aspects.forEach((aspect, amount) -> reduce(
                aspect,
                amount,
                catalog,
                primals
        ));
        return Collections.unmodifiableMap(new LinkedHashMap<>(primals));
    }

    public static Optional<String> rollDiscovery(
            Map<String, Integer> aspects,
            AspectCatalog catalog,
            RandomSource random
    ) {
        return rollDiscovery(aspects, catalog, random::nextInt);
    }

    static Optional<String> rollDiscovery(
            Map<String, Integer> aspects,
            AspectCatalog catalog,
            IntUnaryOperator nextInt
    ) {
        Map<String, Integer> primals = reduceToPrimals(aspects, catalog);
        int total = primals.values().stream().mapToInt(Integer::intValue).sum();
        if (primals.isEmpty()
                || nextInt.applyAsInt(DISCOVERY_ROLL_BOUND) >= total) {
            return Optional.empty();
        }
        List<String> choices = new ArrayList<>(primals.keySet());
        return Optional.of(choices.get(nextInt.applyAsInt(choices.size())));
    }

    private static void reduce(
            String aspectId,
            int amount,
            AspectCatalog catalog,
            Map<String, Integer> primals
    ) {
        if (amount <= 0) {
            return;
        }
        AspectDefinition definition = catalog.lookup(aspectId).orElse(null);
        if (definition == null) {
            return;
        }
        if (definition.isPrimal()) {
            primals.merge(aspectId, amount, Math::addExact);
            return;
        }
        reduce(definition.components().get(0), amount, catalog, primals);
        reduce(definition.components().get(1), amount, catalog, primals);
    }
}
