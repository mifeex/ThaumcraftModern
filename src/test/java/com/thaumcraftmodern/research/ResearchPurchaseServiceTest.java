package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchPurchaseServiceTest {
    @AfterEach
    void clearRegistry() {
        ResearchRegistry.replace(List.of());
    }

    @Test
    void purchaseConsumesExactAspectCostAndCompletesResearch() {
        ResearchDefinition definition = purchasable(List.of(
                new AspectCost("aer", 3),
                new AspectCost("terra", 2)
        ));
        ResearchRegistry.replace(List.of(definition));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch(definition.id());

        ResearchPurchaseService.Result result =
                ResearchPurchaseService.purchase(knowledge, definition);

        assertEquals(ResearchPurchaseService.Result.PURCHASED, result);
        assertEquals(2, knowledge.aspectAmount("aer"));
        assertEquals(3, knowledge.aspectAmount("terra"));
        assertTrue(knowledge.hasCompletedResearch(definition.id()));
    }

    @Test
    void insufficientCostDoesNotConsumeAnyAspect() {
        ResearchDefinition definition = purchasable(List.of(
                new AspectCost("aer", 3),
                new AspectCost("terra", 6)
        ));
        ResearchRegistry.replace(List.of(definition));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch(definition.id());

        ResearchPurchaseService.Result result =
                ResearchPurchaseService.purchase(knowledge, definition);

        assertEquals(
                ResearchPurchaseService.Result.INSUFFICIENT_ASPECTS,
                result
        );
        assertEquals(5, knowledge.aspectAmount("aer"));
        assertEquals(5, knowledge.aspectAmount("terra"));
        assertFalse(knowledge.hasCompletedResearch(definition.id()));
    }

    @Test
    void purchaseRechecksResearchAvailability() {
        ResearchDefinition definition = new ResearchDefinition(
                "secondary",
                "basics",
                "minecraft:book",
                "",
                "research.secondary",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of("parent"),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.SECONDARY,
                false,
                List.of(new AspectCost("aer", 1))
        );
        ResearchRegistry.replace(List.of(definition));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch(definition.id());

        assertEquals(
                ResearchPurchaseService.Result.UNAVAILABLE,
                ResearchPurchaseService.purchase(knowledge, definition)
        );
        assertEquals(5, knowledge.aspectAmount("aer"));
    }

    private static ResearchDefinition purchasable(List<AspectCost> cost) {
        return new ResearchDefinition(
                "secondary",
                "basics",
                "minecraft:book",
                "",
                "research.secondary",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.SECONDARY,
                false,
                cost
        );
    }
}
