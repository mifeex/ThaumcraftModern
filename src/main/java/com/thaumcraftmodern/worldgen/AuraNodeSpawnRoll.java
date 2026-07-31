package com.thaumcraftmodern.worldgen;

import net.minecraft.util.RandomSource;

/**
 * Runtime-independent TC4 wild-node chunk roll.
 */
final class AuraNodeSpawnRoll {
    static final int CLASSIC_NODE_RARITY = 36;

    private AuraNodeSpawnRoll() {
    }

    static boolean roll(RandomSource random) {
        return roll(random, false);
    }

    static boolean roll(RandomSource random, boolean taintedLands) {
        int rarity = taintedLands
                ? TaintedLandsGenerationPolicy.TAINTED_NODE_RARITY
                : CLASSIC_NODE_RARITY;
        return random.nextInt(rarity) == 0;
    }
}
