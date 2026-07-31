package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EldritchConstructBehaviorTest {
    @Test
    void combatValuesMatchOriginalEldritchGolem() {
        assertEquals(100, EldritchConstructBehavior.SPAWN_RECOVERY_TICKS);
        assertEquals(2.0F, EldritchConstructBehavior.SPAWN_HEAL_PER_TICK);
        assertEquals(10, EldritchConstructBehavior.MELEE_COOLDOWN_TICKS);
        assertEquals(0.75F, EldritchConstructBehavior.MELEE_DAMAGE_MULTIPLIER);
        assertEquals(1.5F, EldritchConstructBehavior.HEADLESS_KNOCKBACK);
        assertEquals(3.0D, EldritchConstructBehavior.RANGED_MOVE_SPEED);
        assertEquals(5, EldritchConstructBehavior.RANGED_INTERVAL_TICKS);
        assertEquals(24.0F, EldritchConstructBehavior.RANGED_RANGE);
        assertEquals(150, EldritchConstructBehavior.BEAM_MAX_CHARGE);
        assertEquals(15, EldritchConstructBehavior.BEAM_SHOT_COST_MIN);
        assertEquals(5, EldritchConstructBehavior.BEAM_SHOT_COST_VARIANCE);
        assertEquals(0.66F, EldritchConstructBehavior.ORB_VELOCITY);
        assertEquals(5.0F, EldritchConstructBehavior.ORB_INACCURACY);
        assertEquals(160, EldritchConstructBehavior.ORB_LIFETIME_TICKS);
        assertEquals(0.6F, EldritchConstructBehavior.ORB_DAMAGE_MULTIPLIER);
    }

    @Test
    void firstPhaseRequiresStrictlyOverLethalDamage() {
        assertFalse(EldritchConstructBehavior.breaksHead(10.0F, 10.0F));
        assertTrue(EldritchConstructBehavior.breaksHead(10.01F, 10.0F));
    }

    @Test
    void bossDamageCapMatchesOriginalBaseClass() {
        assertEquals(34.0F, EldritchConstructBehavior.cappedDamage(34.0F));
        assertEquals(35.0F, EldritchConstructBehavior.cappedDamage(80.0F));
        assertEquals(200, EldritchConstructBehavior.ENRAGE_TICKS);
        assertEquals(
                30,
                EldritchConstructBehavior.PASSIVE_HEAL_INTERVAL_TICKS
        );
        assertEquals(1.0F, EldritchConstructBehavior.PASSIVE_HEAL);
    }
}
