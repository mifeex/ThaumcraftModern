package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuraNodeBreakDropsTest {
    @Test
    void reproducesClassicThresholdAndInclusiveTenPointLoop() {
        EnumMap<PrimalAspect, Integer> current = values(0);
        current.put(PrimalAspect.AER, 4);
        current.put(PrimalAspect.TERRA, 5);
        current.put(PrimalAspect.IGNIS, 9);
        current.put(PrimalAspect.AQUA, 10);
        current.put(PrimalAspect.ORDO, 100);

        List<PrimalAspect> drops = AuraNodeBreakDrops.aspectsForDrops(node(
                current,
                values(120)
        ));

        assertEquals(0, count(drops, PrimalAspect.AER));
        assertEquals(1, count(drops, PrimalAspect.TERRA));
        assertEquals(1, count(drops, PrimalAspect.IGNIS));
        assertEquals(2, count(drops, PrimalAspect.AQUA));
        assertEquals(11, count(drops, PrimalAspect.ORDO));
        assertEquals(0, count(drops, PrimalAspect.PERDITIO));
    }

    @Test
    void usesCurrentNodeAspectsRatherThanMaximumPool() {
        assertEquals(
                List.of(),
                AuraNodeBreakDrops.aspectsForDrops(node(values(0), values(100)))
        );
    }

    @Test
    void essenceAmountAndAspectPaletteMatchClassicValues() {
        assertEquals(2, AuraNodeBreakDrops.ESSENCE_ASPECT_AMOUNT);
        assertEquals(0xFF5A01, PrimalAspectColors.color(PrimalAspect.IGNIS));
    }

    private static long count(
            List<PrimalAspect> drops,
            PrimalAspect aspect
    ) {
        return drops.stream().filter(aspect::equals).count();
    }

    private static AuraNodeState node(
            EnumMap<PrimalAspect, Integer> current,
            EnumMap<PrimalAspect, Integer> maximum
    ) {
        return new AuraNodeState(
                UUID.fromString("2f3a48e9-640f-443c-9498-06f42c8f1258"),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                current,
                maximum,
                0L
        );
    }

    private static EnumMap<PrimalAspect, Integer> values(int amount) {
        EnumMap<PrimalAspect, Integer> result =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect, amount);
        }
        return result;
    }
}
