package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchProgressServiceTest {
    @AfterEach
    void clearRegistry() {
        ResearchRegistry.replace(List.of());
    }

    @Test
    void concealedChildAppearsOnlyAfterParentResearchIsCompleted() {
        ResearchDefinition parent = definition(
                "parent",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchDefinition child = definition(
                "child",
                true,
                List.of("parent"),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchRegistry.replace(List.of(parent, child));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        ResearchProgressService.reconcile(knowledge);
        assertTrue(knowledge.hasRevealedResearch("parent"));
        assertFalse(knowledge.hasRevealedResearch("child"));

        knowledge.completeResearch("parent");
        ResearchProgressService.reconcile(knowledge);

        assertTrue(knowledge.hasRevealedResearch("child"));
        assertTrue(ResearchProgressService.isAvailable(child, knowledge));
    }

    @Test
    void staleRevealDoesNotExposeConcealedResearchBeforeItsParents() {
        ResearchDefinition parent = definition(
                "parent",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchDefinition child = definition(
                "child",
                true,
                List.of("parent"),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchRegistry.replace(List.of(parent, child));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.revealResearch("child");

        assertFalse(ResearchProgressService.isVisible(child, knowledge));

        knowledge.completeResearch("parent");
        assertTrue(ResearchProgressService.isVisible(child, knowledge));
    }

    @Test
    void scanTriggerRevealsResearchAndRevealNeverRollsBack() {
        ResearchDefinition triggered = definition(
                "pearl_secret",
                false,
                List.of(),
                List.of(),
                new ResearchCondition.ScanCompleted("item:minecraft:ender_pearl"),
                false
        );
        ResearchRegistry.replace(List.of(triggered));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        ResearchProgressService.reconcile(knowledge);
        assertFalse(knowledge.hasRevealedResearch("pearl_secret"));

        knowledge.recordScan("item:minecraft:ender_pearl");
        ResearchProgressService.reconcile(knowledge);

        assertTrue(knowledge.hasRevealedResearch("pearl_secret"));
        assertTrue(ResearchProgressService.isVisible(triggered, knowledge));
    }

    @Test
    void hiddenParentBlocksAvailabilityWithoutBecomingVisibleConnection() {
        ResearchDefinition gated = definition(
                "gated",
                false,
                List.of(),
                List.of("secret_parent"),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchRegistry.replace(List.of(gated));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        ResearchProgressService.reconcile(knowledge);

        assertTrue(knowledge.hasRevealedResearch("gated"));
        assertFalse(ResearchProgressService.isAvailable(gated, knowledge));

        knowledge.completeResearch("secret_parent");
        assertTrue(ResearchProgressService.isAvailable(gated, knowledge));
    }

    @Test
    void classicWarpGateIgnoresTemporaryWarpUnlessTotalIsRequested() {
        ResearchCondition nonTemporary = new ResearchCondition.WarpAtLeast(
                ResearchCondition.WarpMeasure.NON_TEMPORARY,
                5
        );
        ResearchCondition total = new ResearchCondition.WarpAtLeast(
                ResearchCondition.WarpMeasure.TOTAL,
                5
        );
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.addWarp(WarpType.TEMPORARY, 5);

        assertFalse(nonTemporary.test(knowledge));
        assertTrue(total.test(knowledge));

        knowledge.addWarp(WarpType.NORMAL, 3);
        knowledge.addWarp(WarpType.PERMANENT, 2);
        assertTrue(nonTemporary.test(knowledge));
    }

    @Test
    void autoUnlockChainAdvancesAcrossMultipleResearchPasses() {
        ResearchDefinition first = definition(
                "first",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                true
        );
        ResearchDefinition second = definition(
                "second",
                true,
                List.of("first"),
                List.of(),
                ResearchCondition.ALWAYS,
                true
        );
        ResearchRegistry.replace(List.of(second, first));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        ResearchProgressService.Update update =
                ResearchProgressService.reconcile(knowledge);

        assertTrue(update.changed());
        assertTrue(knowledge.hasCompletedResearch("first"));
        assertTrue(knowledge.hasCompletedResearch("second"));
    }

    @Test
    void onlyAvailableIncompleteManualResearchCanCreateNotes() {
        ResearchDefinition manual = definition(
                "manual",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchDefinition automatic = definition(
                "automatic",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                true
        );
        ResearchRegistry.replace(List.of(manual, automatic));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        ResearchProgressService.reconcile(knowledge);

        assertTrue(ResearchProgressService.canCreateNotes(manual, knowledge));
        assertFalse(ResearchProgressService.canCreateNotes(automatic, knowledge));

        knowledge.completeResearch("manual");
        assertFalse(ResearchProgressService.canCreateNotes(manual, knowledge));
    }

    @Test
    void inactiveGameplayContentRemainsVisibleButCannotBeResearched() {
        ResearchDefinition inactive = new ResearchDefinition(
                "future_content",
                "basics",
                "minecraft:book",
                "research.future_content",
                "",
                false,
                false,
                true,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of()
        );
        ResearchRegistry.replace(List.of(inactive));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        ResearchProgressService.reconcile(knowledge);

        assertTrue(ResearchProgressService.isVisible(inactive, knowledge));
        assertFalse(ResearchProgressService.isAvailable(inactive, knowledge));
        assertFalse(ResearchProgressService.canCreateNotes(inactive, knowledge));
    }

    @Test
    void categoryNeedsAnyVisibleResearchIncludingInactiveContent() {
        ResearchDefinition inactive = new ResearchDefinition(
                "future_content",
                "future",
                "minecraft:book",
                "research.future_content",
                "",
                false,
                false,
                true,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of()
        );
        ResearchDefinition concealed = new ResearchDefinition(
                "concealed_content",
                "hidden",
                "minecraft:book",
                "research.concealed_content",
                "",
                true,
                false,
                false,
                "",
                List.of("parent"),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of()
        );
        ResearchDefinition available = definition(
                "available",
                false,
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                false
        );
        ResearchDefinition virtual = new ResearchDefinition(
                "virtual_root",
                "virtual_only",
                "minecraft:book",
                "",
                "research.virtual_root",
                "",
                false,
                true,
                false,
                true,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of()
        );
        ResearchRegistry.replace(List.of(inactive, concealed, available, virtual));
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        ResearchProgressService.reconcile(knowledge);

        assertTrue(ResearchProgressService.hasVisibleResearch(
                "future",
                knowledge
        ));
        assertFalse(ResearchProgressService.hasVisibleResearch(
                "hidden",
                knowledge
        ));
        assertTrue(ResearchProgressService.hasVisibleResearch(
                "basics",
                knowledge
        ));
        assertFalse(ResearchProgressService.hasVisibleResearch(
                "virtual_only",
                knowledge
        ));
        assertTrue(knowledge.hasCompletedResearch("virtual_root"));

        knowledge.completeResearch("parent");
        ResearchProgressService.reconcile(knowledge);
        assertTrue(ResearchProgressService.hasVisibleResearch(
                "hidden",
                knowledge
        ));
    }

    private static ResearchDefinition definition(
            String id,
            boolean concealed,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            boolean autoUnlock
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                concealed,
                autoUnlock,
                false,
                "",
                parents,
                hiddenParents,
                revealWhen,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of()
        );
    }
}
