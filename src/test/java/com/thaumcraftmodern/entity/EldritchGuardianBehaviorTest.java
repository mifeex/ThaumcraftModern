package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;
import net.minecraft.world.entity.MobSpawnType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EldritchGuardianBehaviorTest {
    @Test
    void altarSpawnerUsesOriginalTc4TimingAndLimits() {
        assertEquals(80, EldritchGuardianBehavior.ALTAR_INITIAL_DELAY_TICKS);
        assertEquals(40, EldritchGuardianBehavior.ALTAR_SPAWN_INTERVAL_TICKS);
        assertEquals(32.0D, EldritchGuardianBehavior.GUARDIAN_SEARCH_HORIZONTAL);
        assertEquals(16.0D, EldritchGuardianBehavior.GUARDIAN_SEARCH_VERTICAL);
        assertEquals(16, EldritchGuardianBehavior.HOME_RADIUS);
        assertEquals(4, EldritchGuardianBehavior.SPAWN_MIN_HORIZONTAL);
        assertEquals(10, EldritchGuardianBehavior.SPAWN_MAX_HORIZONTAL);
        assertEquals(3, EldritchGuardianBehavior.SPAWN_MAX_VERTICAL);
    }

    @Test
    void obeliskBuffUsesOriginalTc4RangeAndTiming() {
        assertEquals(6.0D, EldritchGuardianBehavior.OBELISK_EFFECT_RADIUS);
        assertEquals(
                20,
                EldritchGuardianBehavior.OBELISK_EFFECT_CHECK_INTERVAL_TICKS
        );
        assertEquals(
                40,
                EldritchGuardianBehavior.OBELISK_EFFECT_DURATION_TICKS
        );
        assertEquals(0, EldritchGuardianBehavior.OBELISK_EFFECT_AMPLIFIER);
        assertEquals(1.0F, EldritchGuardianBehavior.OBELISK_HEAL_AMOUNT);
        assertTrue(EldritchGuardianBehavior.shouldRefreshObeliskEffects(
                40L,
                false
        ));
        assertFalse(EldritchGuardianBehavior.shouldRefreshObeliskEffects(
                41L,
                false
        ));
        assertFalse(EldritchGuardianBehavior.shouldRefreshObeliskEffects(
                40L,
                true
        ));
    }

    @Test
    void worldSpawnsRequireNightAndExactSurfaceHeight() {
        assertTrue(EldritchGuardianBehavior.usesSurfaceNightRules(
                MobSpawnType.NATURAL
        ));
        assertTrue(EldritchGuardianBehavior.usesSurfaceNightRules(
                MobSpawnType.STRUCTURE
        ));
        assertFalse(EldritchGuardianBehavior.usesSurfaceNightRules(
                MobSpawnType.COMMAND
        ));
        assertTrue(EldritchGuardianBehavior.isSurfaceNightSpawn(
                true,
                false,
                72,
                72
        ));
        assertFalse(EldritchGuardianBehavior.isSurfaceNightSpawn(
                true,
                true,
                72,
                72
        ));
        assertFalse(EldritchGuardianBehavior.isSurfaceNightSpawn(
                true,
                false,
                71,
                72
        ));
        assertTrue(EldritchGuardianBehavior.isSurfaceNightSpawn(
                false,
                true,
                40,
                40
        ));
    }

    @Test
    void combatMatchesOriginalGuardianAndOrbParameters() {
        assertEquals(8.0D, EldritchGuardianBehavior.RANGED_MIN_DISTANCE);
        assertEquals(24.0F, EldritchGuardianBehavior.RANGED_MAX_DISTANCE);
        assertEquals(20, EldritchGuardianBehavior.RANGED_MIN_COOLDOWN_TICKS);
        assertEquals(40, EldritchGuardianBehavior.RANGED_MAX_COOLDOWN_TICKS);
        assertEquals(0.9F, EldritchGuardianBehavior.ORB_ATTACK_CHANCE);
        assertEquals(100, EldritchGuardianBehavior.ORB_LIFETIME_TICKS);
        assertEquals(2.0D, EldritchGuardianBehavior.ORB_EFFECT_RADIUS);
        assertEquals(0.666F, EldritchGuardianBehavior.ORB_DAMAGE_MULTIPLIER);
        assertEquals(160, EldritchGuardianBehavior.ORB_WITHER_TICKS);
        assertEquals(400, EldritchGuardianBehavior.SCREECH_BLINDNESS_TICKS);
    }

    @Test
    void altarSwitchesToGuardianOnlyAfterCultistsAreGone() {
        assertFalse(EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                79,
                false,
                false
        ));
        assertFalse(EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                119,
                true,
                false
        ));
        assertTrue(EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                119,
                false,
                false
        ));
        assertFalse(EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                119,
                false,
                true
        ));
        assertTrue(EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                159,
                false,
                false
        ));
    }
}
