package com.thaumcraftmodern.entity;

/**
 * TC4 altar-guard and target-selection parameters shared by worldgen and the
 * modern AI.
 */
public final class CrimsonCultBehavior {
    public static final double FOLLOW_RANGE = 32.0D;
    public static final int TARGET_CHECK_INTERVAL_TICKS = 0;
    public static final int UNSEEN_MEMORY_TICKS =
            HostileAiBehavior.UNSEEN_MEMORY_TICKS;
    public static final double ALERT_VERTICAL_RANGE = 10.0D;
    public static final int RITUAL_CHECK_INTERVAL_TICKS = 40;
    public static final double RITUAL_MAX_DISTANCE_SQUARED = 256.0D;
    public static final int CLERIC_HOME_RADIUS = 8;
    public static final int KNIGHT_HOME_RADIUS = 16;
    public static final int RITUALIST_ALERT_CHANCE = 3;

    private CrimsonCultBehavior() {
    }

    static boolean shouldAlertRitualist(
            int legacyChanceRoll
    ) {
        return legacyChanceRoll == 0;
    }
}
