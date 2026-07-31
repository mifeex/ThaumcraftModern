package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchCompletionServiceTest {
    @AfterEach
    void clearRegistry() {
        ResearchRegistry.replace(List.of());
    }

    @Test
    void rejectsUnknownResearchId() {
        assertEquals(
                ResearchCompletionService.Result.UNKNOWN_RESEARCH,
                complete(new PlayerThaumKnowledge(), "missing")
        );
    }

    @Test
    void rejectsInactiveResearch() {
        ResearchRegistry.replace(List.of(definition(
                "inactive",
                true,
                List.of(),
                ResearchCondition.ALWAYS
        )));
        PlayerThaumKnowledge knowledge = ready("inactive");

        assertEquals(
                ResearchCompletionService.Result.INACTIVE_RESEARCH,
                complete(knowledge, "inactive")
        );
        assertFalse(knowledge.hasCompletedResearch("inactive"));
    }

    @Test
    void rejectsUnfinishedParent() {
        ResearchRegistry.replace(List.of(
                definition("parent", false, List.of(), ResearchCondition.ALWAYS),
                definition("child", false, List.of("parent"), ResearchCondition.ALWAYS)
        ));
        PlayerThaumKnowledge knowledge = ready("child");

        assertEquals(
                ResearchCompletionService.Result.PARENTS_INCOMPLETE,
                complete(knowledge, "child")
        );
        assertFalse(knowledge.hasCompletedResearch("child"));
    }

    @Test
    void rejectsUnfinishedHiddenParent() {
        ResearchRegistry.replace(List.of(
                definition("hidden_parent", false, List.of(), ResearchCondition.ALWAYS),
                definition(
                        "child",
                        false,
                        List.of(),
                        List.of("hidden_parent"),
                        ResearchCondition.ALWAYS,
                        ResearchCondition.ALWAYS
                )
        ));
        PlayerThaumKnowledge knowledge = ready("child");

        assertEquals(
                ResearchCompletionService.Result.PARENTS_INCOMPLETE,
                complete(knowledge, "child")
        );
    }

    @Test
    void rejectsUnmetWarpOrScanCriterion() {
        ResearchCondition condition = new ResearchCondition.AllOf(List.of(
                new ResearchCondition.ScanCompleted("item:minecraft:ender_pearl"),
                new ResearchCondition.WarpAtLeast(
                        ResearchCondition.WarpMeasure.NON_TEMPORARY,
                        3
                )
        ));
        ResearchRegistry.replace(List.of(definition(
                "conditional",
                false,
                List.of(),
                condition
        )));
        PlayerThaumKnowledge knowledge = ready("conditional");

        assertEquals(
                ResearchCompletionService.Result.CONDITIONS_UNMET,
                complete(knowledge, "conditional")
        );
    }

    @Test
    void rejectsAlreadyCompletedResearch() {
        ResearchRegistry.replace(List.of(definition(
                "known",
                false,
                List.of(),
                ResearchCondition.ALWAYS
        )));
        PlayerThaumKnowledge knowledge = ready("known");
        knowledge.completeResearch("known");

        assertEquals(
                ResearchCompletionService.Result.ALREADY_COMPLETED,
                complete(knowledge, "known")
        );
    }

    @Test
    void rejectsDiscoveryWithoutServerClaim() {
        ResearchRegistry.replace(List.of(definition(
                "manual",
                false,
                List.of(),
                ResearchCondition.ALWAYS
        )));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch("manual");

        assertEquals(
                ResearchCompletionService.Result.INVALID_DISCOVERY,
                complete(knowledge, "manual")
        );
    }

    @Test
    void rejectsResearchThatWasNotRevealed() {
        ResearchRegistry.replace(List.of(definition(
                "manual",
                false,
                List.of(),
                ResearchCondition.ALWAYS
        )));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        ResearchCompletionService.markDiscoveryReady(knowledge, "manual");

        assertEquals(
                ResearchCompletionService.Result.NOT_REVEALED,
                complete(knowledge, "manual")
        );
    }

    @Test
    void rejectsResearchWhoseRevealConditionIsNoLongerSatisfied() {
        ResearchRegistry.replace(List.of(definition(
                "conditional",
                false,
                List.of(),
                List.of(),
                new ResearchCondition.ScanCompleted(
                        "item:minecraft:ender_pearl"
                ),
                ResearchCondition.ALWAYS
        )));
        PlayerThaumKnowledge knowledge = ready("conditional");

        assertEquals(
                ResearchCompletionService.Result.CONDITIONS_UNMET,
                complete(knowledge, "conditional")
        );
    }

    @Test
    void rejectsAutoUnlockResearchAsAManualDiscovery() {
        ResearchRegistry.replace(List.of(automaticDefinition(
                "automatic",
                List.of()
        )));
        PlayerThaumKnowledge knowledge = ready("automatic");

        assertEquals(
                ResearchCompletionService.Result.TRANSITION_NOT_ALLOWED,
                complete(knowledge, "automatic")
        );
    }

    @Test
    void completesValidTransitionAndReconcilesChildren() {
        ResearchRegistry.replace(List.of(
                definition("manual", false, List.of(), ResearchCondition.ALWAYS),
                automaticDefinition("child", List.of("manual"))
        ));
        PlayerThaumKnowledge knowledge = ready("manual");

        assertEquals(
                ResearchCompletionService.Result.COMPLETED,
                complete(knowledge, "manual")
        );
        assertTrue(knowledge.hasCompletedResearch("manual"));
        assertTrue(knowledge.hasCompletedResearch("child"));
    }

    @Test
    void completionAppliesClassicPermanentAndNormalWarpSplitOnce() {
        ResearchDefinition warped = new ResearchDefinition(
                "warped",
                "basics",
                "minecraft:book",
                "",
                "research.warped",
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
                3,
                ResearchDefinition.NodeFrame.PRIMARY,
                false
        );
        ResearchRegistry.replace(List.of(warped));
        PlayerThaumKnowledge knowledge = ready("warped");

        assertEquals(
                ResearchCompletionService.Result.COMPLETED,
                complete(knowledge, "warped")
        );
        assertEquals(2, knowledge.warp(WarpType.PERMANENT));
        assertEquals(1, knowledge.warp(WarpType.NORMAL));
        assertEquals(3, knowledge.warpCounter());
        assertEquals(
                ResearchCompletionService.Result.ALREADY_COMPLETED,
                complete(knowledge, "warped")
        );
        assertEquals(3, knowledge.nonTemporaryWarp());
    }

    private static PlayerThaumKnowledge ready(String researchId) {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch(researchId);
        ResearchCompletionService.markDiscoveryReady(knowledge, researchId);
        return knowledge;
    }

    private static ResearchCompletionService.Result complete(
            PlayerThaumKnowledge knowledge,
            String researchId
    ) {
        return ResearchCompletionService.completeValidatedDiscovery(
                knowledge,
                researchId
        );
    }

    private static ResearchDefinition definition(
            String id,
            boolean inactive,
            List<String> parents,
            ResearchCondition unlockCondition
    ) {
        return definition(
                id,
                inactive,
                parents,
                List.of(),
                ResearchCondition.ALWAYS,
                unlockCondition
        );
    }

    private static ResearchDefinition definition(
            String id,
            boolean inactive,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealCondition,
            ResearchCondition unlockCondition
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                !parents.isEmpty(),
                false,
                inactive,
                "",
                parents,
                hiddenParents,
                revealCondition,
                unlockCondition,
                0,
                0,
                List.of()
        );
    }

    private static ResearchDefinition automaticDefinition(
            String id,
            List<String> parents
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                true,
                true,
                false,
                "",
                parents,
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                1,
                0,
                List.of()
        );
    }
}
