package com.thaumcraftmodern.knowledge;

import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeSyncTest {
    @AfterEach
    void clearResearchRegistry() {
        ResearchRegistry.replace(List.of());
    }

    @Test
    void registryReloadAddsAutomaticResearchToExistingKnowledge() {
        PlayerThaumKnowledge existingKnowledge = new PlayerThaumKnowledge();
        ResearchRegistry.replace(List.of(definition("manual", false)));

        assertFalse(KnowledgeSync.applyAutomaticResearchUnlocks(existingKnowledge));
        assertFalse(existingKnowledge.hasCompletedResearch("automatic"));

        ResearchRegistry.replace(List.of(
                definition("manual", false),
                definition("automatic", true)
        ));

        assertTrue(KnowledgeSync.applyAutomaticResearchUnlocks(existingKnowledge));
        assertTrue(existingKnowledge.hasCompletedResearch("automatic"));
        assertFalse(existingKnowledge.hasCompletedResearch("manual"));
        assertFalse(KnowledgeSync.applyAutomaticResearchUnlocks(existingKnowledge));
    }

    @Test
    void inactiveAutomaticResearchRemainsVisibleButIsNotCompleted() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        ResearchRegistry.replace(List.of(definition("preserved_only", true, true)));

        assertFalse(KnowledgeSync.applyAutomaticResearchUnlocks(knowledge));
        assertFalse(knowledge.hasCompletedResearch("preserved_only"));
        assertTrue(ResearchRegistry.find("preserved_only").isPresent());
    }

    private static ResearchDefinition definition(String id, boolean autoUnlock) {
        return definition(id, autoUnlock, false);
    }

    private static ResearchDefinition definition(
            String id,
            boolean autoUnlock,
            boolean inactive
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                false,
                autoUnlock,
                inactive,
                "",
                List.of(),
                0,
                0,
                List.of()
        );
    }
}
