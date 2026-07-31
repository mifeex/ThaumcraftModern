package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicAuraNodeGenerationTest {
    private static final List<String> PRIMALS = PrimalAspect.ordered().stream()
            .map(PrimalAspect::id)
            .toList();
    private static final List<String> COMPOUNDS = List.of(
            "auram",
            "fames",
            "lucrum",
            "mortuus",
            "victus",
            "herba",
            "tenebrae",
            "vitium"
    );
    private static final ClassicAuraNodeGeneration.Environment PLAINS =
            new ClassicAuraNodeGeneration.Environment(
                    100,
                    "aer",
                    false,
                    false,
                    false,
                    false,
                    0,
                    0,
                    0,
                    0
            );

    @Test
    void generatedNodesHaveVariableAspectSetsAndValidPools() {
        RandomSource random = RandomSource.create(4235L);
        java.util.Set<java.util.Set<String>> aspectSets =
                new java.util.HashSet<>();
        java.util.Set<Integer> totals = new java.util.HashSet<>();
        for (int index = 0; index < 200; index++) {
            AuraNodeState.Snapshot node = generated(random, index).snapshot();
            aspectSets.add(node.aspectsCurrent().keySet());
            totals.add(node.aspectsCurrent().values().stream()
                    .mapToInt(Integer::intValue)
                    .sum());
            assertEquals(
                    node.aspectsCurrent().keySet(),
                    node.aspectsMaximum().keySet()
            );
            assertTrue(node.aspectsCurrent().values().stream()
                    .allMatch(amount -> amount > 0));
        }
        assertTrue(aspectSets.size() > 8);
        assertTrue(totals.size() > 20);
    }

    @Test
    void originalSpecialAndHungryRarityRemainRare() {
        RandomSource random = RandomSource.create(0x544334L);
        int special = 0;
        int hungry = 0;
        java.util.EnumSet<AuraNodeModifier> modifiers =
                java.util.EnumSet.noneOf(AuraNodeModifier.class);
        for (int index = 0; index < 18_000; index++) {
            AuraNodeState.Snapshot node = generated(random, index).snapshot();
            if (node.type() != AuraNodeType.NORMAL) {
                special++;
            }
            if (node.type() == AuraNodeType.HUNGRY) {
                hungry++;
                assertTrue(node.aspectsCurrent().containsKey("fames"));
            }
            modifiers.add(node.modifier());
        }
        assertTrue(special > 850 && special < 1_150);
        assertTrue(hungry > 65 && hungry < 145);
        assertTrue(modifiers.containsAll(java.util.EnumSet.allOf(
                AuraNodeModifier.class
        )));
    }

    @Test
    void modifierDistributionMatchesClassicOneInNineRoll() {
        RandomSource random = RandomSource.create(0x50414C454E4F4445L);
        Map<AuraNodeModifier, Integer> counts =
                new EnumMap<>(AuraNodeModifier.class);
        for (AuraNodeModifier modifier : AuraNodeModifier.values()) {
            counts.put(modifier, 0);
        }
        for (int index = 0; index < 90_000; index++) {
            AuraNodeModifier modifier = generated(random, index)
                    .snapshot()
                    .modifier();
            counts.put(modifier, counts.get(modifier) + 1);
        }

        assertTrue(counts.get(AuraNodeModifier.NORMAL) > 79_000);
        assertTrue(counts.get(AuraNodeModifier.NORMAL) < 81_000);
        assertRareModifier(counts, AuraNodeModifier.BRIGHT);
        assertRareModifier(counts, AuraNodeModifier.PALE);
        assertRareModifier(counts, AuraNodeModifier.FADING);
    }

    @Test
    void biomeAffinitiesAreSelectedInsteadOfForcingOneAspect() {
        RandomSource random = RandomSource.create(0x42494F4D45534CL);
        ClassicAuraNodeGeneration.Environment nether =
                new ClassicAuraNodeGeneration.Environment(
                        List.of(
                                new ClassicAuraNodeGeneration.BiomeAffinity(
                                        120,
                                        "ignis"
                                ),
                                new ClassicAuraNodeGeneration.BiomeAffinity(
                                        100,
                                        "ignis"
                                ),
                                new ClassicAuraNodeGeneration.BiomeAffinity(
                                        80,
                                        "perditio"
                                )
                        ),
                        false,
                        false,
                        false,
                        false,
                        0,
                        0,
                        0,
                        0
                );
        int ignis = 0;
        int perditio = 0;
        for (int index = 0; index < 6_000; index++) {
            AuraNodeState.Snapshot node = ClassicAuraNodeGeneration.generate(
                    new UUID(1L, index),
                    random,
                    nether,
                    PRIMALS,
                    COMPOUNDS
            ).snapshot();
            if (node.aspectsCurrent().containsKey("ignis")) {
                ignis++;
            }
            if (node.aspectsCurrent().containsKey("perditio")) {
                perditio++;
            }
        }
        assertTrue(ignis > perditio);
        assertTrue(ignis > 3_800);
        assertTrue(perditio > 1_800);
    }

    @Test
    void taintedBiomeProducesTaintedNodesAndVitium() {
        RandomSource random = RandomSource.create(77L);
        ClassicAuraNodeGeneration.Environment tainted =
                new ClassicAuraNodeGeneration.Environment(
                        80,
                        "vitium",
                        true,
                        false,
                        false,
                        false,
                        0,
                        0,
                        0,
                        0
                );
        int taintedCount = 0;
        for (int index = 0; index < 500; index++) {
            AuraNodeState.Snapshot node = ClassicAuraNodeGeneration.generate(
                    new UUID(0L, index),
                    random,
                    tainted,
                    PRIMALS,
                    COMPOUNDS
            ).snapshot();
            if (node.type() == AuraNodeType.TAINTED) {
                taintedCount++;
                assertTrue(node.aspectsCurrent().containsKey("vitium"));
            }
        }
        assertEquals(500, taintedCount);
    }

    @Test
    void eerieStructureNodesAreDarkButKeepRandomPools() {
        RandomSource random = RandomSource.create(0x455249454E4F4445L);
        ClassicAuraNodeGeneration.Environment eerie =
                new ClassicAuraNodeGeneration.Environment(
                        100,
                        "aer",
                        false,
                        false,
                        true,
                        false,
                        0,
                        0,
                        0,
                        0
                );
        java.util.Set<Map<String, Integer>> pools = new java.util.HashSet<>();
        for (int index = 0; index < 500; index++) {
            AuraNodeState.Snapshot node = ClassicAuraNodeGeneration.generate(
                    new UUID(2L, index),
                    random,
                    eerie,
                    PRIMALS,
                    COMPOUNDS
            ).snapshot();
            assertEquals(AuraNodeType.DARK, node.type());
            assertFalse(node.aspectsCurrent().values().stream()
                    .allMatch(amount -> amount == 100));
            pools.add(node.aspectsCurrent());
        }
        assertTrue(pools.size() > 450);
    }

    @Test
    void codecVersionTwoPreservesCompoundAspects() {
        AuraNodeState original = AuraNodeState.withAspects(
                UUID.fromString("2d2dfa68-1c7f-447e-8549-d08310bbb384"),
                AuraNodeType.HUNGRY,
                AuraNodeModifier.BRIGHT,
                Map.of("aer", 14, "fames", 37, "lucrum", 9),
                Map.of("aer", 20, "fames", 40, "lucrum", 11),
                4L
        );
        AuraNodeState restored = AuraNodeCodec.decode(
                AuraNodeCodec.encode(original)
        );
        assertEquals(original.snapshot(), restored.snapshot());
        assertEquals(37, restored.current("fames"));
        assertFalse(restored.snapshot().aspectsCurrent().containsKey("terra"));
    }

    private static AuraNodeState generated(RandomSource random, int index) {
        return ClassicAuraNodeGeneration.generate(
                new UUID(0L, index),
                random,
                PLAINS,
                PRIMALS,
                COMPOUNDS
        );
    }

    private static void assertRareModifier(
            Map<AuraNodeModifier, Integer> counts,
            AuraNodeModifier modifier
    ) {
        int count = counts.get(modifier);
        assertTrue(
                count > 2_600 && count < 3_400,
                modifier + " count was " + count
        );
    }
}
