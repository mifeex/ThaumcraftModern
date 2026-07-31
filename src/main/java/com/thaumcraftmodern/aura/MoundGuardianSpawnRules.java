package com.thaumcraftmodern.aura;

/**
 * Pure contract for TC4 dark-node Furious Zombie attempts.
 */
public final class MoundGuardianSpawnRules {
    public static final int INTERVAL_TICKS = 50;
    public static final int BIOME_OFFSET_BOUND = 12;
    public static final double PLAYER_RANGE = 24.0D;
    public static final double HORIZONTAL_CAP_RANGE = 10.0D;
    public static final double VERTICAL_CAP_RANGE = 6.0D;
    public static final int MAX_EXISTING_GUARDIANS = 3;
    public static final double SPAWN_SPREAD = 5.0D;

    private MoundGuardianSpawnRules() {
    }

    public static int biomeOffset(int firstRoll, int secondRoll) {
        if (firstRoll < 0 || firstRoll >= BIOME_OFFSET_BOUND
                || secondRoll < 0 || secondRoll >= BIOME_OFFSET_BOUND) {
            throw new IllegalArgumentException(
                    "dark-node biome rolls must be in [0, 12)"
            );
        }
        return firstRoll - secondRoll;
    }

    public static boolean mayAttempt(
            boolean enabled,
            int ticks,
            boolean randomGate,
            boolean playerNearby,
            int existingGuardians
    ) {
        return enabled
                && ticks % INTERVAL_TICKS == 0
                && randomGate
                && playerNearby
                && existingGuardians <= MAX_EXISTING_GUARDIANS;
    }
}
