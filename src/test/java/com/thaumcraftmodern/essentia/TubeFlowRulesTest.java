package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.essentia.tube.TubeFlowRules;
import com.thaumcraftmodern.essentia.tube.TubePolicy;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TubeFlowRulesTest {
    @Test
    void restrictedTubeHalvesSuctionButPlainTubeLosesOne() {
        TubePolicy restricted = TubePolicyRegistry.require(
                TubePolicyRegistry.RESTRICTED);
        TubePolicy plain = TubePolicyRegistry.require(TubePolicyRegistry.PLAIN);

        assertEquals(16, TubeFlowRules.propagatedSuction(restricted, 32));
        assertEquals(15, TubeFlowRules.propagatedSuction(restricted, 31));
        assertEquals(31, TubeFlowRules.propagatedSuction(plain, 32));
    }

    @Test
    void oneWayTubeReceivesSuctionAndEssentiaFromOppositeSides() {
        TubePolicy oneWay = TubePolicyRegistry.require(TubePolicyRegistry.ONE_WAY);
        Direction facing = Direction.UP;

        assertTrue(TubeFlowRules.acceptsSuctionFrom(
                oneWay, facing, Direction.DOWN));
        assertFalse(TubeFlowRules.mayPullFrom(
                oneWay, facing, Direction.DOWN));
        assertFalse(TubeFlowRules.acceptsSuctionFrom(
                oneWay, facing, Direction.UP));
        assertTrue(TubeFlowRules.mayPullFrom(
                oneWay, facing, Direction.UP));
    }

    @Test
    void facingSelectionChecksTheOppositePhysicalConnection() {
        assertEquals(Direction.DOWN,
                TubeFlowRules.controlledSide(Direction.UP));
        assertEquals(Direction.WEST,
                TubeFlowRules.controlledSide(Direction.EAST));
    }
}
