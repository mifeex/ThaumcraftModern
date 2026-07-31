package com.thaumcraftmodern.entity;

import net.minecraft.world.entity.MobSpawnType;

/**
 * Combat and altar-spawner constants verified against TC4 4.2.3.5.
 */
public final class EldritchGuardianBehavior {
    public static final int ALTAR_INITIAL_DELAY_TICKS = 80;
    public static final int ALTAR_SPAWN_INTERVAL_TICKS = 40;
    public static final double CULTIST_SEARCH_HORIZONTAL = 24.0D;
    public static final double CULTIST_SEARCH_VERTICAL = 16.0D;
    public static final double GUARDIAN_SEARCH_HORIZONTAL = 32.0D;
    public static final double GUARDIAN_SEARCH_VERTICAL = 16.0D;
    public static final int HOME_RADIUS = 16;
    public static final int SPAWN_MIN_HORIZONTAL = 4;
    public static final int SPAWN_MAX_HORIZONTAL = 10;
    public static final int SPAWN_MAX_VERTICAL = 3;
    public static final double OBELISK_EFFECT_RADIUS = 6.0D;
    public static final int OBELISK_EFFECT_CHECK_INTERVAL_TICKS = 20;
    public static final int OBELISK_EFFECT_DURATION_TICKS = 40;
    public static final int OBELISK_EFFECT_AMPLIFIER = 0;
    public static final float OBELISK_HEAL_AMOUNT = 1.0F;

    public static final double RANGED_MIN_DISTANCE = 8.0D;
    public static final float RANGED_MAX_DISTANCE = 24.0F;
    public static final int RANGED_MIN_COOLDOWN_TICKS = 20;
    public static final int RANGED_MAX_COOLDOWN_TICKS = 40;
    public static final float ORB_ATTACK_CHANCE = 0.9F;

    public static final int ORB_LIFETIME_TICKS = 100;
    public static final double ORB_EFFECT_RADIUS = 2.0D;
    public static final float ORB_DAMAGE_MULTIPLIER = 0.666F;
    public static final int ORB_WITHER_TICKS = 160;
    public static final int SCREECH_BLINDNESS_TICKS = 400;
    public static final int SCREECH_MIN_TEMPORARY_WARP = 1;
    public static final int SCREECH_MAX_TEMPORARY_WARP = 3;

    private EldritchGuardianBehavior() {
    }

    public static boolean shouldAttemptAltarSpawn(
            int counterBeforeIncrement,
            boolean hasLivingCultists,
            boolean hasNearbyGuardian
    ) {
        return isAltarSpawnBoundary(counterBeforeIncrement)
                && !hasLivingCultists
                && !hasNearbyGuardian;
    }

    public static boolean isAltarSpawnBoundary(int counterBeforeIncrement) {
        int counterAfterIncrement = counterBeforeIncrement + 1;
        return counterBeforeIncrement >= ALTAR_INITIAL_DELAY_TICKS
                && counterAfterIncrement
                        % ALTAR_SPAWN_INTERVAL_TICKS == 0;
    }

    public static boolean usesSurfaceNightRules(MobSpawnType reason) {
        return reason == MobSpawnType.NATURAL
                || reason == MobSpawnType.CHUNK_GENERATION
                || reason == MobSpawnType.STRUCTURE;
    }

    public static boolean isSurfaceNightSpawn(
            boolean hasSkyLight,
            boolean isDay,
            int spawnY,
            int surfaceY
    ) {
        return (!hasSkyLight || !isDay) && spawnY == surfaceY;
    }

    public static boolean shouldRefreshObeliskEffects(
            long gameTime,
            boolean hasResistance
    ) {
        return gameTime % OBELISK_EFFECT_CHECK_INTERVAL_TICKS == 0
                && !hasResistance;
    }
}
