package com.thaumcraftmodern.entity;

/**
 * Combat constants preserved from TC4's EntityEldritchGolem and
 * EntityThaumcraftBoss. Keeping the numeric contract here makes the modern
 * goal implementation auditable without scattering legacy magic numbers.
 */
public final class EldritchConstructBehavior {
    public static final int SPAWN_RECOVERY_TICKS = 100;
    public static final float SPAWN_HEAL_PER_TICK = 2.0F;
    public static final int MELEE_COOLDOWN_TICKS = 10;
    public static final float MELEE_DAMAGE_MULTIPLIER = 0.75F;
    public static final double MELEE_VERTICAL_LIFT = 0.2D;
    public static final float HEADLESS_KNOCKBACK = 1.5F;
    public static final double HEADLESS_KNOCKBACK_Y = 0.1D;

    public static final double RANGED_MOVE_SPEED = 3.0D;
    public static final int RANGED_INTERVAL_TICKS = 5;
    public static final float RANGED_RANGE = 24.0F;
    public static final int BEAM_MAX_CHARGE = 150;
    public static final int BEAM_SHOT_COST_MIN = 15;
    public static final int BEAM_SHOT_COST_VARIANCE = 5;
    public static final float ORB_VELOCITY = 0.66F;
    public static final float ORB_INACCURACY = 5.0F;
    public static final int ORB_LIFETIME_TICKS = 160;
    public static final float ORB_DAMAGE_MULTIPLIER = 0.6F;
    public static final double ORB_ACCELERATION = 0.2D;
    public static final double ORB_MAX_AXIS_SPEED = 0.25D;

    public static final float MAX_DAMAGE_PER_HIT = 35.0F;
    public static final int ENRAGE_TICKS = 200;
    public static final int PASSIVE_HEAL_INTERVAL_TICKS = 30;
    public static final float PASSIVE_HEAL = 1.0F;

    private EldritchConstructBehavior() {
    }

    public static boolean breaksHead(float incomingDamage, float health) {
        return incomingDamage > health;
    }

    public static float cappedDamage(float incomingDamage) {
        return Math.min(incomingDamage, MAX_DAMAGE_PER_HIT);
    }
}
