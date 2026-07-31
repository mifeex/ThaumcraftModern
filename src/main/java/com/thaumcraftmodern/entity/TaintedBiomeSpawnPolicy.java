package com.thaumcraftmodern.entity;

/**
 * Shared biome and placement gate for natural taint-creature spawning.
 */
final class TaintedBiomeSpawnPolicy {
    private TaintedBiomeSpawnPolicy() {
    }

    static boolean allows(
            LegacyMobKind kind,
            boolean taintedBiome,
            boolean hostileSpawnRules,
            boolean sturdyGround
    ) {
        return kind.tainted()
                && taintedBiome
                && hostileSpawnRules
                && (kind.flying() || sturdyGround);
    }
}
