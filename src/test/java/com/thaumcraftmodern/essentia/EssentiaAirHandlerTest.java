package com.thaumcraftmodern.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaAirHandlerTest {
    private static final BlockPos ORIGIN = new BlockPos(10, 20, 30);

    @Test
    void ordinaryRequesterScansTheClassicFullCube() {
        List<BlockPos> scan = EssentiaAirHandler.buildScan(ORIGIN, null, 2);
        assertEquals(125, scan.size());
        assertEquals(ORIGIN.offset(-2, -2, -2), scan.get(0));
        assertEquals(ORIGIN.offset(2, 2, 2), scan.get(scan.size() - 1));
    }

    @Test
    void mirrorScansOnlyEightBlocksForwardFromItsFace() {
        List<BlockPos> east = EssentiaAirHandler.buildScan(
                ORIGIN,
                Direction.EAST,
                8
        );
        assertEquals(17 * 17 * 8, east.size());
        assertTrue(east.stream().allMatch(position ->
                position.getX() >= ORIGIN.getX()
                        && position.getX() < ORIGIN.getX() + 8));
        assertTrue(east.contains(ORIGIN.offset(7, 8, 8)));
        assertTrue(!east.contains(ORIGIN.offset(8, 0, 0)));
        assertTrue(!east.contains(ORIGIN.offset(-1, 0, 0)));
    }
}
