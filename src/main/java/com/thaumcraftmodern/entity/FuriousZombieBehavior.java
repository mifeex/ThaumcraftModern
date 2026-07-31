package com.thaumcraftmodern.entity;

import net.minecraft.util.Mth;

/**
 * Exact anger curve from TC4's EntityGiantBrainyZombie.
 */
public final class FuriousZombieBehavior {
    public static final float INITIAL_ANGER = 1.0F;
    public static final float MAX_ANGER = 2.0F;
    public static final float ANGER_PER_HIT = 0.1F;
    public static final float ANGER_DECAY_PER_TICK = 0.002F;
    public static final double BASE_ATTACK_DAMAGE = 7.0D;
    public static final double BONUS_DAMAGE_AT_MAX_ANGER = 5.0D;

    private FuriousZombieBehavior() {
    }

    public static float afterHit(float anger) {
        return Mth.clamp(
                anger + ANGER_PER_HIT,
                INITIAL_ANGER,
                MAX_ANGER
        );
    }

    public static float afterTick(float anger) {
        return Math.max(
                INITIAL_ANGER,
                anger - ANGER_DECAY_PER_TICK
        );
    }

    public static double attackDamage(float anger) {
        return BASE_ATTACK_DAMAGE
                + (anger - INITIAL_ANGER)
                * BONUS_DAMAGE_AT_MAX_ANGER;
    }
}
