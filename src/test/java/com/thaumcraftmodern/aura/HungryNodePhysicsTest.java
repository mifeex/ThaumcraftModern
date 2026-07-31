package com.thaumcraftmodern.aura;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HungryNodePhysicsTest {
    @Test
    void distantEntityIsPulledAndGivenTangentialOrbit() {
        Vec3 velocity = HungryNodePhysics.apply(
                Vec3.ZERO,
                new Vec3(10.0D, 0.0D, 0.0D),
                Vec3.ZERO,
                false
        );
        assertTrue(velocity.x < 0.0D, "must pull toward node");
        assertTrue(velocity.z < 0.0D, "must add orbital tangent");
        assertEquals(0.0D, velocity.y, 1.0E-9D);
    }

    @Test
    void closeEntityReceivesUpwardSlingshotPulse() {
        Vec3 ordinary = HungryNodePhysics.apply(
                Vec3.ZERO,
                new Vec3(2.0D, 0.0D, 0.0D),
                Vec3.ZERO,
                false
        );
        Vec3 thrown = HungryNodePhysics.apply(
                Vec3.ZERO,
                new Vec3(2.0D, 0.0D, 0.0D),
                Vec3.ZERO,
                true
        );
        assertTrue(thrown.y > ordinary.y + 0.3D);
        assertTrue(Math.abs(thrown.z) > Math.abs(ordinary.z) + 0.6D);
    }

    @Test
    void entitiesOutsideClassicRangeAreUnaffected() {
        Vec3 initial = new Vec3(0.1D, -0.2D, 0.3D);
        assertEquals(initial, HungryNodePhysics.apply(
                initial,
                new Vec3(15.0D, 0.0D, 0.0D),
                Vec3.ZERO,
                true
        ));
    }
}
