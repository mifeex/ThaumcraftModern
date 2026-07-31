package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CrimsonCultBehaviorTest {
    @Test
    void altarRitualUsesOriginalTc4Limits() {
        assertEquals(32.0D, CrimsonCultBehavior.FOLLOW_RANGE);
        assertEquals(0, CrimsonCultBehavior.TARGET_CHECK_INTERVAL_TICKS);
        assertEquals(60, CrimsonCultBehavior.UNSEEN_MEMORY_TICKS);
        assertEquals(10.0D, CrimsonCultBehavior.ALERT_VERTICAL_RANGE);
        assertEquals(40, CrimsonCultBehavior.RITUAL_CHECK_INTERVAL_TICKS);
        assertEquals(
                256.0D,
                CrimsonCultBehavior.RITUAL_MAX_DISTANCE_SQUARED
        );
        assertEquals(8, CrimsonCultBehavior.CLERIC_HOME_RADIUS);
        assertEquals(16, CrimsonCultBehavior.KNIGHT_HOME_RADIUS);
        assertEquals(3, CrimsonCultBehavior.RITUALIST_ALERT_CHANCE);
    }

    @Test
    void ritualistsAnswerGroupAggroWithOriginalOneInThreeChance() {
        assertFalse(CrimsonCultBehavior.shouldAlertRitualist(
                2
        ));
        assertTrue(CrimsonCultBehavior.shouldAlertRitualist(
                0
        ));
    }
}
