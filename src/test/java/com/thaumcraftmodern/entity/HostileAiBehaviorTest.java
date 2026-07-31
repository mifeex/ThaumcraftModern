package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HostileAiBehaviorTest {
    @Test
    void hostileMobsUseOriginalPerKindPursuitParameters() {
        assertEquals(32.0D, HostileAiBehavior.FOLLOW_RANGE);
        assertEquals(40.0D, HostileAiBehavior.ELDRITCH_FOLLOW_RANGE);
        assertEquals(
                12.0D,
                HostileAiBehavior.followRange(LegacyMobKind.MIND_SPIDER)
        );
        assertEquals(
                32.0D,
                HostileAiBehavior.followRange(LegacyMobKind.CRIMSON_KNIGHT)
        );
        assertEquals(
                32.0D,
                HostileAiBehavior.followRange(LegacyMobKind.CRIMSON_CLERIC)
        );
        assertEquals(
                32.0D,
                HostileAiBehavior.followRange(LegacyMobKind.CRIMSON_PRAETOR)
        );
        assertEquals(
                40.0D,
                HostileAiBehavior.followRange(
                        LegacyMobKind.ELDRITCH_GUARDIAN
                )
        );
        assertEquals(
                48.0D,
                HostileAiBehavior.followRange(LegacyMobKind.ELDRITCH_WARDEN)
        );
        assertEquals(0, HostileAiBehavior.TARGET_CHECK_INTERVAL_TICKS);
        assertEquals(60, HostileAiBehavior.UNSEEN_MEMORY_TICKS);
        assertTrue(HostileAiBehavior.MELEE_PURSUIT_SPEED > 1.0D);
    }
}
