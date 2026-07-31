package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PechBehaviorTest {
    @Test
    void twentyWayHeldItemRollMatchesTc4Distribution() {
        Map<PechBehavior.HeldItemRoll, Integer> counts =
                new EnumMap<>(PechBehavior.HeldItemRoll.class);
        for (int roll = 0; roll < 20; roll++) {
            counts.merge(PechBehavior.heldItemRoll(roll), 1, Integer::sum);
        }

        assertEquals(6, counts.get(PechBehavior.HeldItemRoll.EMPTY));
        assertEquals(2, counts.get(PechBehavior.HeldItemRoll.WAND));
        assertEquals(5, counts.get(PechBehavior.HeldItemRoll.BOW));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.STONE_SWORD));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.STONE_AXE));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.IRON_SWORD));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.IRON_AXE));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.FISHING_ROD));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.STONE_PICKAXE));
        assertEquals(1, counts.get(PechBehavior.HeldItemRoll.IRON_PICKAXE));
    }

    @Test
    void heldItemDeterminesClassicPechType() {
        assertEquals(
                PechBehavior.MAGE,
                PechBehavior.typeFor(PechBehavior.HeldItemRoll.WAND)
        );
        assertEquals(
                PechBehavior.STALKER,
                PechBehavior.typeFor(PechBehavior.HeldItemRoll.BOW)
        );
        assertEquals(
                PechBehavior.FORAGER,
                PechBehavior.typeFor(PechBehavior.HeldItemRoll.EMPTY)
        );
        assertEquals(
                PechBehavior.FORAGER,
                PechBehavior.typeFor(PechBehavior.HeldItemRoll.IRON_PICKAXE)
        );
    }

    @Test
    void tameChanceUsesStrictTenSidedComparison() {
        assertFalse(PechBehavior.tames(0, 0));
        assertTrue(PechBehavior.tames(1, 0));
        assertFalse(PechBehavior.tames(1, 1));
        assertTrue(PechBehavior.tames(5, 4));
        assertFalse(PechBehavior.tames(5, 5));
    }
}
