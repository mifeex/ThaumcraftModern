package com.thaumcraftmodern.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.research.HexResearchPuzzle.EraseResult;
import com.thaumcraftmodern.research.HexResearchPuzzle.PlacementResult;
import com.thaumcraftmodern.testing.AspectFixtures;

class HexResearchPuzzleTest {
    @Test
    void exactClassicChainCompletesDeterministically() {
        PlayerThaumKnowledge knowledge = solvedKnowledge();
        HexResearchPuzzle puzzle = new HexResearchPuzzle(AspectFixtures.firstDiscoveryCatalog());

        assertEquals("aer", puzzle.aspectAt(-2).orElseThrow());
        assertEquals("ordo", puzzle.aspectAt(2).orElseThrow());
        assertFalse(puzzle.isComplete());

        assertEquals(PlacementResult.PLACED, puzzle.place(-1, "lux", knowledge));
        assertEquals(PlacementResult.PLACED, puzzle.place(0, "ignis", knowledge));
        assertEquals(
                PlacementResult.PLACED_AND_COMPLETED,
                puzzle.place(1, "potentia", knowledge));

        assertTrue(puzzle.isComplete());
        assertEquals(HexResearchPuzzle.solution(), puzzle.placements());
        assertEquals(EraseResult.ALREADY_COMPLETE, puzzle.erase(0));
    }

    @Test
    void classicChainCanBeBuiltFromCompoundTowardPrimalInEitherDirection() {
        PlayerThaumKnowledge knowledge = solvedKnowledge();
        HexResearchPuzzle puzzle = new HexResearchPuzzle(AspectFixtures.firstDiscoveryCatalog());

        assertEquals(PlacementResult.PLACED, puzzle.place(1, "potentia", knowledge));
        assertEquals(PlacementResult.PLACED, puzzle.place(0, "ignis", knowledge));
        assertEquals(
                PlacementResult.PLACED_AND_COMPLETED,
                puzzle.place(-1, "lux", knowledge)
        );
        assertTrue(puzzle.isComplete());
    }

    @Test
    void placementRequiresRegisteredKnownAspectButAllowsTemporaryDisconnectedCells() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        HexResearchPuzzle puzzle = new HexResearchPuzzle(AspectFixtures.firstDiscoveryCatalog());

        assertEquals(
                PlacementResult.ASPECT_NOT_REGISTERED,
                puzzle.place(-1, "missing", knowledge));
        assertEquals(
                PlacementResult.ASPECT_NOT_KNOWN,
                puzzle.place(-1, "lux", knowledge));

        knowledge.learnAspect("lux");
        assertEquals(
                PlacementResult.ASPECT_DEPLETED,
                puzzle.validatePlacement(-1, "lux", knowledge)
        );
        knowledge.addAspectPoints("lux", 2);
        assertEquals(PlacementResult.PLACED, puzzle.validatePlacement(-1, "lux", knowledge));
        assertTrue(puzzle.aspectAt(-1).isEmpty());
        assertEquals(PlacementResult.PLACED, puzzle.place(-1, "lux", knowledge));
        assertEquals(PlacementResult.PLACED, puzzle.place(1, "lux", knowledge));
        assertEquals(0, knowledge.aspectAmount("lux"));
        assertTrue(puzzle.hasRelatedNeighbor(-1));
        assertFalse(puzzle.hasRelatedNeighbor(1));
        assertFalse(puzzle.isComplete());
    }

    @Test
    void eraseValidationProtectsAnchorsAndReportsEmptyCells() {
        PlayerThaumKnowledge knowledge = solvedKnowledge();
        HexResearchPuzzle puzzle = new HexResearchPuzzle(AspectFixtures.firstDiscoveryCatalog());

        assertEquals(EraseResult.ANCHOR_LOCKED, puzzle.erase(-2));
        assertEquals(EraseResult.CELL_EMPTY, puzzle.erase(0));
        assertEquals(EraseResult.CELL_OUT_OF_BOUNDS, puzzle.erase(3));

        assertEquals(PlacementResult.PLACED, puzzle.place(-1, "lux", knowledge));
        assertEquals(EraseResult.ERASED, puzzle.erase(-1));
        assertTrue(puzzle.aspectAt(-1).isEmpty());
    }

    @Test
    void restoringSavedPlacementNeverConsumesLiveAspectPoints() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.addAspectPoints("lux", 3);
        HexResearchPuzzle puzzle = new HexResearchPuzzle(AspectFixtures.firstDiscoveryCatalog());

        assertTrue(puzzle.restorePlacement(-1, "lux"));
        assertEquals("lux", puzzle.aspectAt(-1).orElseThrow());
        assertEquals(3, knowledge.aspectAmount("lux"));
    }

    @Test
    void masteryPlacementStillRequiresAPointButCanWaiveItsConsumption() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        HexResearchPuzzle puzzle = new HexResearchPuzzle(
                AspectFixtures.firstDiscoveryCatalog()
        );

        knowledge.learnAspect("lux");
        assertEquals(
                PlacementResult.ASPECT_DEPLETED,
                puzzle.place(-1, "lux", knowledge, false)
        );

        knowledge.addAspectPoints("lux", 1);
        assertEquals(
                PlacementResult.PLACED,
                puzzle.place(-1, "lux", knowledge, false)
        );
        assertEquals(1, knowledge.aspectAmount("lux"));
    }

    private static PlayerThaumKnowledge solvedKnowledge() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.addAspectPoints("lux", 1);
        knowledge.addAspectPoints("potentia", 1);
        return knowledge;
    }
}
