package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PureNodeBiomeSpreadRulesTest {
    @Test
    void usesOriginalTriangularFifteenBlockWindow() {
        assertEquals(-7, PureNodeBiomeSpreadRules.biomeOffset(0, 7));
        assertEquals(0, PureNodeBiomeSpreadRules.biomeOffset(4, 4));
        assertEquals(7, PureNodeBiomeSpreadRules.biomeOffset(7, 0));
    }

    @Test
    void silverwoodAndTaintConditionsMatchTc4() {
        assertTrue(PureNodeBiomeSpreadRules.mayPaint(50, true, false));
        assertTrue(PureNodeBiomeSpreadRules.mayPaint(50, false, true));
        assertFalse(PureNodeBiomeSpreadRules.mayPaint(49, true, true));
        assertFalse(PureNodeBiomeSpreadRules.mayPaint(50, false, false));
    }
}
