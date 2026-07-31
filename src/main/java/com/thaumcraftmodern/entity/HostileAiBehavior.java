package com.thaumcraftmodern.entity;

/**
 * Shared pursuit tuning for hostile Thaumcraft entities.
 */
public final class HostileAiBehavior {
    public static final double FOLLOW_RANGE = 32.0D;
    public static final double ELDRITCH_FOLLOW_RANGE = 40.0D;
    public static final double MIND_SPIDER_FOLLOW_RANGE = 12.0D;
    public static final double ELDRITCH_WARDEN_FOLLOW_RANGE = 48.0D;
    public static final int TARGET_CHECK_INTERVAL_TICKS = 0;
    public static final int UNSEEN_MEMORY_TICKS = 60;
    public static final double MELEE_PURSUIT_SPEED = 1.15D;
    public static final double IDLE_RETURN_SPEED = 0.8D;

    private HostileAiBehavior() {
    }

    /**
     * Exact TC4 follow-range overrides. Mobs absent from this switch inherit
     * the original generic 32-block hostile range.
     */
    public static double followRange(LegacyMobKind kind) {
        return switch (kind) {
            case MIND_SPIDER -> MIND_SPIDER_FOLLOW_RANGE;
            case ELDRITCH_GUARDIAN, ELDRITCH_CONSTRUCT, GIANT_TAINTACLE ->
                    ELDRITCH_FOLLOW_RANGE;
            case ELDRITCH_WARDEN -> ELDRITCH_WARDEN_FOLLOW_RANGE;
            default -> FOLLOW_RANGE;
        };
    }
}
