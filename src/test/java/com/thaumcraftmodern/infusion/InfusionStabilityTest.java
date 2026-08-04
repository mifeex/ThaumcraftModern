package com.thaumcraftmodern.infusion;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfusionStabilityTest {
    private static final BlockPos MATRIX = new BlockPos(0, 4, 0);

    @Test
    void mirroredPedestalsAndItemsAreNeutralButOneOccupiedPedestalCostsThree() {
        BlockPos west = new BlockPos(-3, 2, 0);
        BlockPos east = new BlockPos(3, 2, 0);
        assertEquals(0, symmetry(
                List.of(new InfusionStability.Pedestal(west, true),
                        new InfusionStability.Pedestal(east, true)), Set.of()));
        assertEquals(3, symmetry(
                List.of(new InfusionStability.Pedestal(west, true)), Set.of()));
    }

    @Test
    void sixMirroredStabilizerPairsReachMinusOneAfterTc4FloatTruncation() {
        Set<BlockPos> stabilizers = new HashSet<>();
        for (int z : new int[]{-3, -2, -1, 1, 2, 3}) {
            stabilizers.add(new BlockPos(-5, 2, z));
            stabilizers.add(new BlockPos(5, 2, z));
        }
        assertEquals(-1, symmetry(List.of(), stabilizers));
    }

    private static int symmetry(List<InfusionStability.Pedestal> pedestals,
            Set<BlockPos> stabilizers) {
        Set<BlockPos> pedestalPositions = new HashSet<>();
        Set<BlockPos> occupied = new HashSet<>();
        for (InfusionStability.Pedestal pedestal : pedestals) {
            pedestalPositions.add(pedestal.position());
            if (pedestal.occupied()) occupied.add(pedestal.position());
        }
        return InfusionStability.symmetry(MATRIX, pedestals,
                new ArrayList<>(stabilizers), pedestalPositions::contains,
                occupied::contains, stabilizers::contains);
    }
}
