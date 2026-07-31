package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.essentia.tube.TubeFacingRules;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TubeFacingRulesTest {
    @Test
    void advancesToFirstSideWithoutAdjacentTransportInTc4Order() {
        EnumSet<Direction> occupied = EnumSet.of(
                Direction.SOUTH, Direction.WEST);

        assertEquals(Direction.EAST, TubeFacingRules.nextFreeSide(
                Direction.NORTH, occupied::contains));
    }

    @Test
    void keepsFacingWhenEverySideHasTransport() {
        assertEquals(Direction.UP, TubeFacingRules.nextFreeSide(
                Direction.UP, side -> true));
    }
}
