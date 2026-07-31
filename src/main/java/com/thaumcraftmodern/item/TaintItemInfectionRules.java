package com.thaumcraftmodern.item;

/**
 * Minecraft-independent constants and probability gate copied from TC4's
 * ItemResource inventory tick.
 */
public final class TaintItemInfectionRules {
    public static final int ROLL_BOUND = 4321;
    public static final int EFFECT_DURATION_TICKS = 120;

    private TaintItemInfectionRules() {
    }

    public static boolean shouldInfect(int roll, int stackCount) {
        return roll <= stackCount;
    }
}
