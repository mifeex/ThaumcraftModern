package com.thaumcraftmodern.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddedAuraNodeGenerationTest {
    @Test
    void silverwoodCanContainAtMostTwoNodes() {
        assertEquals(2, SilverwoodNodeGeneration.MAX_NODES);
        assertEquals(10, SilverwoodNodeGeneration.initialChanceBound(7));
        assertEquals(17, SilverwoodNodeGeneration.nextChanceBound(10, 7));
        assertFalse(SilverwoodNodeGeneration.shouldPlace(0, 0, 0));
        assertTrue(SilverwoodNodeGeneration.shouldPlace(1, 0, 0));
        assertTrue(SilverwoodNodeGeneration.shouldPlace(6, 1, 0));
        assertFalse(SilverwoodNodeGeneration.shouldPlace(6, 2, 0));
        assertFalse(SilverwoodNodeGeneration.shouldPlace(6, 1, 1));
        assertTrue(SilverwoodNodeGeneration.shouldPlaceForTree(
                false,
                1,
                0,
                0
        ));
    }

    @Test
    void auraTotemUsesClassicEarlyNodeLevelsAndFallbackHeight() {
        assertEquals(5, AuraTotemGeneration.MAX_NODE_HEIGHT);
        assertFalse(AuraTotemGeneration.isNodeLevel(1, 0));
        assertTrue(AuraTotemGeneration.isNodeLevel(2, 0));
        assertTrue(AuraTotemGeneration.isNodeLevel(4, 0));
        assertFalse(AuraTotemGeneration.isNodeLevel(4, 1));
        assertEquals(40, AuraTotemGeneration.MIN_LEAF_SEARCH_Y);
        assertTrue(AuraTotemGeneration.acceptsBase(
                true, false, false, false, false
        ));
        assertTrue(AuraTotemGeneration.acceptsBase(
                false, false, false, true, false
        ));
        assertFalse(AuraTotemGeneration.acceptsBase(
                false, false, false, false, false
        ));
        assertTrue(AuraTotemGeneration.acceptsReplaceable(true, false));
        assertTrue(AuraTotemGeneration.acceptsReplaceable(false, true));
        assertFalse(AuraTotemGeneration.acceptsReplaceable(false, false));
    }
}
