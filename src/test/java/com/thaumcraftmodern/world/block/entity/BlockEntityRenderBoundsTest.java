package com.thaumcraftmodern.world.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlockEntityRenderBoundsTest {
    @Test
    void obeliskBoundsContainTheCompleteAnimatedModel() {
        BlockPos position = new BlockPos(10, 20, 30);
        AABB bounds = EldritchAltarPartBlockEntity.renderBoundingBox(
                position,
                1
        );

        assertTrue(bounds.minX < position.getX());
        assertTrue(bounds.maxX > position.getX() + 1.0D);
        assertTrue(bounds.maxY >= position.getY() + 4.2D);
        assertTrue(bounds.minZ < position.getZ());
        assertTrue(bounds.maxZ > position.getZ() + 1.0D);
    }

    @Test
    void thaumatoriumBoundsContainMachineAndFrontDisplayForEveryFacing() {
        BlockPos position = new BlockPos(10, 20, 30);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            AABB bounds = ThaumatoriumBlockEntity.renderBoundingBox(
                    position,
                    facing
            );
            assertTrue(bounds.minX <= position.getX());
            assertTrue(bounds.maxX >= position.getX() + 1.0D);
            assertTrue(bounds.minY <= position.getY());
            assertTrue(bounds.maxY >= position.getY() + 2.0D);
            assertTrue(bounds.minZ <= position.getZ());
            assertTrue(bounds.maxZ >= position.getZ() + 1.0D);

            double displayX = position.getX() + 0.5D
                    + facing.getStepX() / 1.99D;
            double displayZ = position.getZ() + 0.5D
                    + facing.getStepZ() / 1.99D;
            assertTrue(bounds.minX < displayX);
            assertTrue(bounds.maxX > displayX);
            assertTrue(bounds.minZ < displayZ);
            assertTrue(bounds.maxZ > displayZ);
        }
    }
}
