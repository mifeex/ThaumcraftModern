package com.thaumcraftmodern.nodejar;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeJarStructureTest {
    @Test
    void classicBlueprintContainsNineSlabsTwentySixGlassAndOneNode() {
        long glass = NodeJarStructure.cells().stream()
                .filter(cell -> cell.kind() == NodeJarStructure.CellKind.GLASS)
                .count();
        long slabs = NodeJarStructure.cells().stream()
                .filter(cell -> cell.kind() == NodeJarStructure.CellKind.WOODEN_SLAB)
                .count();
        long nodes = NodeJarStructure.cells().stream()
                .filter(cell -> cell.kind() == NodeJarStructure.CellKind.AURA_NODE)
                .count();

        assertEquals(36, NodeJarStructure.cells().size());
        assertEquals(NodeJarStructure.GLASS_COUNT, glass);
        assertEquals(NodeJarStructure.WOODEN_SLAB_COUNT, slabs);
        assertEquals(1, nodes);
    }

    @Test
    void validatesEveryLoadedCellAndReportsExactMismatch() {
        BlockPos center = new BlockPos(10, 70, -4);
        UUID nodeId = UUID.randomUUID();
        FakeWorld world = completeWorld(center, nodeId);

        assertTrue(NodeJarStructure.validate(center, nodeId, world).valid());

        BlockPos missing = center.offset(1, 2, 0);
        world.cells.put(missing, NodeJarStructure.CellKind.GLASS);
        NodeJarStructure.Validation invalid = NodeJarStructure.validate(
                center,
                nodeId,
                world
        );
        assertFalse(invalid.valid());
        assertEquals(NodeJarStructure.Failure.WRONG_BLOCK, invalid.failure());
        assertEquals(missing, invalid.position());
        assertEquals(NodeJarStructure.CellKind.WOODEN_SLAB, invalid.expected());
    }

    static FakeWorld completeWorld(BlockPos center, UUID nodeId) {
        FakeWorld world = new FakeWorld(nodeId);
        for (NodeJarStructure.Cell cell : NodeJarStructure.cells()) {
            world.cells.put(center.offset(cell.offset()), cell.kind());
        }
        return world;
    }

    static final class FakeWorld implements NodeJarStructure.WorldView {
        private final UUID nodeId;
        private final Map<BlockPos, NodeJarStructure.CellKind> cells =
                new HashMap<>();

        private FakeWorld(UUID nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public boolean isLoaded(BlockPos position) {
            return cells.containsKey(position);
        }

        @Override
        public boolean isAuraNode(BlockPos position, UUID expectedNodeId) {
            return expectedNodeId.equals(nodeId)
                    && cells.get(position) == NodeJarStructure.CellKind.AURA_NODE;
        }

        @Override
        public boolean isGlass(BlockPos position) {
            return cells.get(position) == NodeJarStructure.CellKind.GLASS;
        }

        @Override
        public boolean isWoodenSlab(BlockPos position) {
            return cells.get(position) == NodeJarStructure.CellKind.WOODEN_SLAB;
        }
    }
}
