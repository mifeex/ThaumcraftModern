package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.essentia.tube.TubeWandTargetResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EssentiaTubeWandTargetTest {
    private static final BlockPos POS = new BlockPos(10, 20, 30);

    @Test
    void sideWallOfEveryArmSelectsTheArmRatherThanTheHitFace() {
        assertArm(Direction.WEST,  .10, .50, .50, Direction.UP);
        assertArm(Direction.EAST,  .90, .50, .50, Direction.DOWN);
        assertArm(Direction.DOWN,  .50, .10, .50, Direction.NORTH);
        assertArm(Direction.UP,    .50, .90, .50, Direction.SOUTH);
        assertArm(Direction.NORTH, .50, .50, .10, Direction.EAST);
        assertArm(Direction.SOUTH, .50, .50, .90, Direction.WEST);
    }

    @Test
    void centralNodeIsOriginalTc4RotationSubHitSix() {
        for (Direction side : Direction.values()) {
            BlockHitResult hit = hit(.50, .50, .50, side);
            assertTrue(hitsCore(hit));
        }
    }

    @Test
    void armsRemainSideToggleTargetsOutsideTheCore() {
        assertFalse(hitsCore(hit(.50, .50, .10, Direction.EAST)));
        assertFalse(hitsCore(hit(.90, .50, .50, Direction.UP)));
    }

    private static void assertArm(Direction expected, double x, double y,
            double z, Direction misleadingFace) {
        assertEquals(expected, resolve(hit(x, y, z, misleadingFace)));
    }

    private static BlockHitResult hit(double x, double y, double z,
            Direction face) {
        return new BlockHitResult(new Vec3(POS.getX() + x, POS.getY() + y,
                POS.getZ() + z), face, POS, false);
    }

    private static Direction resolve(BlockHitResult hit) {
        return TubeWandTargetResolver.resolve(
                hit.getLocation().x-POS.getX(),
                hit.getLocation().y-POS.getY(),
                hit.getLocation().z-POS.getZ(), hit.getDirection());
    }

    private static boolean hitsCore(BlockHitResult hit) {
        return TubeWandTargetResolver.hitsCore(
                hit.getLocation().x-POS.getX(),
                hit.getLocation().y-POS.getY(),
                hit.getLocation().z-POS.getZ());
    }
}
