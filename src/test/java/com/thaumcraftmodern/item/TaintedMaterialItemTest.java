package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaintedMaterialItemTest {
    @Test
    void preservesClassicInventoryInfectionGate() {
        assertEquals(4321, TaintItemInfectionRules.ROLL_BOUND);
        assertEquals(120, TaintItemInfectionRules.EFFECT_DURATION_TICKS);
        assertTrue(TaintItemInfectionRules.shouldInfect(0, 1));
        assertTrue(TaintItemInfectionRules.shouldInfect(16, 16));
        assertFalse(TaintItemInfectionRules.shouldInfect(17, 16));
    }
}
