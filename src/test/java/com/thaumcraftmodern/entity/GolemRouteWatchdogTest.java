package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class GolemRouteWatchdogTest {
    @Test
    void rebuildsAfterThreeFinishedOrFailedNavigationChecks() {
        GolemRouteWatchdog watchdog = new GolemRouteWatchdog();
        assertTrue(watchdog.shouldRebuild(12L, 0, 0D, 0D, 0D, 25D, true));
        assertFalse(watchdog.shouldRebuild(12L, 10, 0D, 0D, 0D, 25D, true));
        assertFalse(watchdog.shouldRebuild(12L, 20, 0D, 0D, 0D, 25D, true));
        assertTrue(watchdog.shouldRebuild(12L, 30, 0D, 0D, 0D, 25D, true));
    }

    @Test
    void realProgressResetsFailureSequence() {
        GolemRouteWatchdog watchdog = new GolemRouteWatchdog();
        assertTrue(watchdog.shouldRebuild(12L, 0, 0D, 0D, 0D, 25D, false));
        assertFalse(watchdog.shouldRebuild(12L, 10, 0D, 0D, 0D, 25D, false));
        assertFalse(watchdog.shouldRebuild(12L, 20, 1D, 0D, 0D, 16D, false));
        assertFalse(watchdog.shouldRebuild(12L, 30, 1D, 0D, 0D, 16D, false));
        assertFalse(watchdog.shouldRebuild(12L, 40, 1D, 0D, 0D, 16D, false));
        assertTrue(watchdog.shouldRebuild(12L, 50, 1D, 0D, 0D, 16D, false));
    }

    @Test
    void changingTargetImmediatelyBuildsAPath() {
        GolemRouteWatchdog watchdog = new GolemRouteWatchdog();
        assertTrue(watchdog.shouldRebuild(1L, 0, 0D, 0D, 0D, 9D, false));
        assertTrue(watchdog.shouldRebuild(2L, 1, 0D, 0D, 0D, 9D, false));
    }

    @Test
    void releasesTargetAfterFourFailedRouteRebuilds() {
        GolemRouteWatchdog watchdog = new GolemRouteWatchdog();
        assertTrue(watchdog.shouldRebuild(12L, 0, 0D, 0D, 0D, 25D, true));
        for (int rebuild = 1; rebuild <= GolemRouteWatchdog.REBUILDS_BEFORE_TARGET_RELEASE; rebuild++) {
            int base = rebuild * 30;
            watchdog.shouldRebuild(12L, base - 20, 0D, 0D, 0D, 25D, true);
            watchdog.shouldRebuild(12L, base - 10, 0D, 0D, 0D, 25D, true);
            watchdog.shouldRebuild(12L, base, 0D, 0D, 0D, 25D, true);
        }
        assertTrue(watchdog.shouldReleaseTarget());
    }
}
