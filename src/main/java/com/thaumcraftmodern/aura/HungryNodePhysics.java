package com.thaumcraftmodern.aura;

import net.minecraft.world.phys.Vec3;

/**
 * Pure velocity integration for the Hungry node's pull, orbit and slingshot.
 */
public final class HungryNodePhysics {
    public static final double RANGE = 15.0D;

    private HungryNodePhysics() {
    }

    public static Vec3 apply(
            Vec3 currentVelocity,
            Vec3 entityPosition,
            Vec3 nodeCenter,
            boolean throwPulse
    ) {
        Vec3 delta = nodeCenter.subtract(entityPosition);
        double distance = delta.length();
        if (distance <= 0.0001D || distance >= RANGE) {
            return currentVelocity;
        }
        double normalizedDistance = distance / RANGE;
        double pull = 1.0D - normalizedDistance;
        pull *= pull;
        Vec3 radial = delta.normalize();
        Vec3 tangent = new Vec3(-radial.z, 0.0D, radial.x);
        double orbit = pull * 0.055D;
        Vec3 velocity = currentVelocity.add(
                radial.x * pull * 0.15D + tangent.x * orbit,
                radial.y * pull * 0.25D,
                radial.z * pull * 0.15D + tangent.z * orbit
        );
        if (throwPulse && distance < 2.5D) {
            velocity = velocity.add(
                    tangent.x * 0.65D - radial.x * 0.20D,
                    0.35D,
                    tangent.z * 0.65D - radial.z * 0.20D
            );
        }
        return velocity;
    }
}
