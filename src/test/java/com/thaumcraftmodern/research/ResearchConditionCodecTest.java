package com.thaumcraftmodern.research;

import com.google.gson.JsonParser;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.scan.ScanDefinition;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanTargetType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchConditionCodecTest {
    @Test
    void jsonSupportsNestedClassicTriggerAndWarpGate() {
        ResearchCondition condition = ResearchConditionCodec.fromJson(
                JsonParser.parseString("""
                        {
                          "type": "all_of",
                          "conditions": [
                            {
                              "type": "research_completed",
                              "id": "first_discovery"
                            },
                            {
                              "type": "any_of",
                              "conditions": [
                                {
                                  "type": "scan",
                                  "id": "entity:minecraft:enderman"
                                },
                                {
                                  "type": "scan",
                                  "id": "item:minecraft:ender_pearl"
                                }
                              ]
                            },
                            {
                              "type": "warp",
                              "minimum": 5
                            }
                          ]
                        }
                        """),
                "test"
        );
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.completeResearch("first_discovery");
        knowledge.recordScan("item:minecraft:ender_pearl");
        knowledge.addWarp(WarpType.TEMPORARY, 20);

        assertFalse(condition.test(knowledge));

        knowledge.addWarp(WarpType.PERMANENT, 2);
        knowledge.addWarp(WarpType.NORMAL, 3);
        assertTrue(condition.test(knowledge));
    }

    @Test
    void unknownConditionTypeFailsWithUsefulError() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ResearchConditionCodec.fromJson(
                        JsonParser.parseString("""
                                {"type": "scna", "id": "item:minecraft:book"}
                                """),
                        "research typo.reveal_when"
                )
        );
    }

    @Test
    void scannedAspectRequiresAnActualScanRatherThanAspectKnowledgeAlone() {
        ScanRegistry.replace(List.of(new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:coal",
                "",
                List.of(new AspectReward("potentia", 2))
        )));
        try {
            ResearchCondition condition = ResearchConditionCodec.fromJson(
                    JsonParser.parseString("""
                            {"type": "scan_aspect", "id": "potentia"}
                            """),
                    "test"
            );
            PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
            knowledge.learnAspect("potentia");

            assertFalse(condition.test(knowledge));

            knowledge.recordScan("item:minecraft:coal");
            assertTrue(condition.test(knowledge));
            assertTrue(
                    ResearchConditionCodec.fromNbt(
                            ResearchConditionCodec.toNbt(condition)
                    ).test(knowledge)
            );
        } finally {
            ScanRegistry.replace(List.of());
        }
    }
}
