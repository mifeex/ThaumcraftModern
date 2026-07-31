package com.thaumcraftmodern.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

class PlayerThaumKnowledgeTest {
    @Test
    void automaticResearchIsCompletedOnceAndPersisted() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        assertTrue(knowledge.applyAutomaticResearchUnlocks(List.of("basics", "alchemy")));
        assertFalse(knowledge.applyAutomaticResearchUnlocks(List.of("basics", "alchemy")));
        assertTrue(knowledge.hasCompletedResearch("basics"));
        assertTrue(knowledge.hasCompletedResearch("alchemy"));

        PlayerThaumKnowledge restored = PlayerThaumKnowledge.deserialize(knowledge.serialize());
        assertTrue(restored.hasCompletedResearch("basics"));
        assertTrue(restored.hasCompletedResearch("alchemy"));
    }

    @Test
    void nbtRoundTripPreservesAllKnowledgeAndVersion() {
        PlayerThaumKnowledge original = new PlayerThaumKnowledge();
        original.learnAspect("lux");
        original.learnAspect("potentia");
        original.recordScan("block:minecraft:bookshelf");
        original.recordScan("entity:minecraft:cow");
        original.revealResearch("research_mastery");
        original.completeResearch("first_discovery");
        original.recordResearchCriterion("crafted:minecraft:bookshelf");
        original.addWarp(WarpType.PERMANENT, 2);
        original.addWarp(WarpType.NORMAL, 3);
        original.addWarp(WarpType.TEMPORARY, 4);

        CompoundTag serialized = original.serialize();
        PlayerThaumKnowledge restored = PlayerThaumKnowledge.deserialize(serialized);

        assertEquals(PlayerThaumKnowledge.SERIAL_VERSION, serialized.getInt("version"));
        assertEquals(original.knownAspects(), restored.knownAspects());
        assertEquals(original.aspectAmounts(), restored.aspectAmounts());
        assertEquals(original.scans(), restored.scans());
        assertEquals(original.revealedResearch(), restored.revealedResearch());
        assertEquals(original.completedResearch(), restored.completedResearch());
        assertEquals(original.researchCriteria(), restored.researchCriteria());
        assertEquals(2, restored.warp(WarpType.PERMANENT));
        assertEquals(3, restored.warp(WarpType.NORMAL));
        assertEquals(4, restored.warp(WarpType.TEMPORARY));
        assertEquals(5, restored.nonTemporaryWarp());
        assertEquals(9, restored.totalWarp());
        assertEquals(9, restored.warpCounter());
    }

    @Test
    void newKnowledgeStartsWithExactlySixClassicPrimals() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        assertEquals(PlayerThaumKnowledge.startingPrimalAspects(), knowledge.knownAspects());
        assertEquals(6, knowledge.knownAspects().size());
        assertTrue(knowledge.knowsAspect("aer"));
        assertEquals(PlayerThaumKnowledge.STARTING_PRIMAL_AMOUNT, knowledge.aspectAmount("aer"));
        assertTrue(knowledge.knowsAspect("perditio"));
        assertFalse(knowledge.knowsAspect("lux"));
    }

    @Test
    void copyAndGettersDoNotExposeMutableState() {
        PlayerThaumKnowledge original = new PlayerThaumKnowledge();
        original.learnAspect("lux");
        PlayerThaumKnowledge copy = original.copy();

        copy.learnAspect("potentia");

        assertFalse(original.knowsAspect("potentia"));
        assertTrue(copy.knowsAspect("potentia"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.knownAspects().add("potentia"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.aspectAmounts().put("aer", 99));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.scans().add("block:minecraft:stone"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.completedResearch().add("first_discovery"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.revealedResearch().add("first_discovery"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> original.researchCriteria().add("crafted:minecraft:book"));
    }

    @Test
    void unknownSerializationVersionIsRejected() {
        CompoundTag tag = new PlayerThaumKnowledge().serialize();
        tag.putInt("version", 99);

        assertThrows(
                IllegalArgumentException.class,
                () -> PlayerThaumKnowledge.deserialize(tag));
    }

    @Test
    void researchCompletionTransitionsExactlyOnceAndCopiesToRespawnState() {
        PlayerThaumKnowledge original = new PlayerThaumKnowledge();

        assertFalse(original.hasCompletedResearch("first_discovery"));
        assertTrue(original.completeResearch("first_discovery"));
        assertFalse(original.completeResearch("first_discovery"));

        PlayerThaumKnowledge respawnState = new PlayerThaumKnowledge();
        respawnState.copyFrom(original);
        assertTrue(respawnState.hasCompletedResearch("first_discovery"));
    }

    @Test
    void learningAspectReportsOnlyTheFirstDiscovery() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        assertTrue(knowledge.learnAspect("lux"));
        assertFalse(knowledge.learnAspect("lux"));
        assertFalse(knowledge.learnAspect("aer"));
    }

    @Test
    void aspectPointsAreAddedConsumedAndMigratedFromVersionOne() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        assertTrue(knowledge.addAspectPoints("lux", 2));
        assertFalse(knowledge.addAspectPoints("lux", 3));
        assertEquals(5, knowledge.aspectAmount("lux"));
        assertTrue(knowledge.tryConsumeAspects("aer", "ignis"));
        assertEquals(PlayerThaumKnowledge.STARTING_PRIMAL_AMOUNT - 1, knowledge.aspectAmount("aer"));
        knowledge.addAspectPoints("single_use", 1);
        assertTrue(knowledge.tryConsumeAspect("single_use"));
        assertEquals(0, knowledge.aspectAmount("single_use"));
        assertFalse(knowledge.tryConsumeAspect("single_use"));

        CompoundTag legacy = knowledge.serialize();
        legacy.putInt("version", 1);
        legacy.remove("aspect_amounts");
        PlayerThaumKnowledge migrated = PlayerThaumKnowledge.deserialize(legacy);
        assertEquals(PlayerThaumKnowledge.STARTING_PRIMAL_AMOUNT, migrated.aspectAmount("aer"));
        assertEquals(0, migrated.aspectAmount("lux"));
    }

    @Test
    void versionTwoResearchMigratesCompletedEntriesToRevealedState() {
        PlayerThaumKnowledge original = new PlayerThaumKnowledge();
        original.completeResearch("legacy_completed");
        CompoundTag versionTwo = original.serialize();
        versionTwo.putInt("version", 2);
        versionTwo.remove("revealed_research");
        versionTwo.remove("research_criteria");
        versionTwo.remove("warp");

        PlayerThaumKnowledge migrated = PlayerThaumKnowledge.deserialize(versionTwo);

        assertTrue(migrated.hasCompletedResearch("legacy_completed"));
        assertTrue(migrated.hasRevealedResearch("legacy_completed"));
        assertEquals(0, migrated.totalWarp());
    }

    @Test
    void versionThreeWarpMigratesEventCounterFromTotalWarp() {
        PlayerThaumKnowledge original = new PlayerThaumKnowledge();
        original.addWarp(WarpType.PERMANENT, 2);
        original.addWarp(WarpType.NORMAL, 3);
        original.addWarp(WarpType.TEMPORARY, 4);
        CompoundTag versionThree = original.serialize();
        versionThree.putInt("version", 3);
        versionThree.getCompound("warp").remove("counter");

        PlayerThaumKnowledge migrated = PlayerThaumKnowledge.deserialize(versionThree);

        assertEquals(9, migrated.totalWarp());
        assertEquals(9, migrated.warpCounter());
    }

    @Test
    void gainingWarpRefreshesCounterButDecayDoesNot() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.addWarp(WarpType.PERMANENT, 5);
        assertEquals(5, knowledge.warpCounter());
        knowledge.setWarpCounter(1);

        knowledge.setWarp(WarpType.TEMPORARY, 0);

        assertEquals(1, knowledge.warpCounter());
        knowledge.addWarp(WarpType.TEMPORARY, 3);
        assertEquals(8, knowledge.warpCounter());
    }

}
