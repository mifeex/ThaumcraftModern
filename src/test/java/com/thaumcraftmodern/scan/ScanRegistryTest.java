package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ScanRegistryTest {
    @AfterEach
    void resetRegistry() {
        ScanRegistry.replace(List.of());
    }

    @Test
    void serializedDefinitionsPreserveTargetNamesAndEmbeddedAspectAmounts() {
        ScanDefinition stone = new ScanDefinition(
                ScanTargetType.BLOCK,
                "minecraft:stone",
                "block.minecraft.stone",
                List.of(new AspectReward("terra", 3))
        );
        ScanRegistry.replace(List.of(stone));

        List<ScanDefinition> restored = ScanRegistry.deserialize(ScanRegistry.serialize());

        assertEquals(List.of(stone), restored);
    }

    @Test
    void missingDefinitionDoesNotUseHeuristicsByDefault() {
        ScanRegistry.replace(List.of());

        assertFalse(
                ScanRegistry.find(ScanTargetType.ITEM, "minecraft:feather")
                        .isPresent()
        );
    }

    @Test
    void phenomenonDefinitionKeepsStableNodeScanKeyAcrossSync() {
        ScanDefinition node = new ScanDefinition(
                ScanTargetType.PHENOMENON,
                "thaumcraftmodern:aura_node",
                "block.thaumcraftmodern.aura_node",
                List.of()
        );
        ScanRegistry.replace(List.of(node));

        ScanDefinition restored = ScanRegistry.deserialize(ScanRegistry.serialize())
                .get(0);

        assertEquals(node, restored);
        assertEquals(
                "phenomenon:thaumcraftmodern:aura_node",
                restored.scanKey()
        );
    }

    @Test
    void explicitDefinitionAlwaysWinsInFidelityMode() {
        ScanDefinition feather = new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:feather",
                "item.minecraft.feather",
                List.of(new AspectReward("aer", 2))
        );
        ScanRegistry.replace(List.of(feather));

        assertEquals(
                feather,
                ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:feather",
                                true,
                                (type, targetId) -> {
                                    throw new AssertionError(
                                            "Fallback must not replace an explicit definition"
                                    );
                                }
                        )
                        .orElseThrow()
        );
    }

    @Test
    void automaticFallbackCanBeEnabledExplicitly() {
        ScanRegistry.replace(List.of());
        ScanDefinition inferred = new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:structure_void",
                "",
                List.of(new AspectReward("ordo", 1))
        );

        assertEquals(
                inferred,
                ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:structure_void",
                                true,
                                (type, targetId) -> Optional.of(inferred)
                        )
                        .orElseThrow()
        );
    }

    @Test
    void deepslateInfusedStoneUsesTheOrdinaryOreScanKey() {
        assertEquals(
                "thaumcraftmodern:air_infused_stone",
                ScanRegistry.canonicalBlockId(
                        "thaumcraftmodern:deepslate_air_infused_stone"
                )
        );
        assertEquals(
                "thaumcraftmodern:entropy_infused_stone",
                ScanRegistry.canonicalBlockId(
                        "thaumcraftmodern:deepslate_entropy_infused_stone"
                )
        );
    }
}
