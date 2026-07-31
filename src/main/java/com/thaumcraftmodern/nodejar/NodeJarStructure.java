package com.thaumcraftmodern.nodejar;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Classic 3x4x3 Node-in-a-Jar multiblock around the node.
 *
 * <p>Relative to the node: a complete 3x3x3 glass shell surrounds the center
 * (26 glass blocks), and a 3x3 wooden-slab cap sits one block above the shell.
 * The shape is represented explicitly so validation and world mutation use
 * the same 36 cells.</p>
 */
public final class NodeJarStructure {
    public static final int GLASS_COUNT = 26;
    public static final int WOODEN_SLAB_COUNT = 9;
    private static final List<Cell> CELLS = buildCells();

    private NodeJarStructure() {
    }

    public static List<Cell> cells() {
        return CELLS;
    }

    public static Validation validate(
            BlockPos nodePosition,
            UUID expectedNodeId,
            WorldView world
    ) {
        Objects.requireNonNull(nodePosition, "nodePosition");
        Objects.requireNonNull(expectedNodeId, "expectedNodeId");
        Objects.requireNonNull(world, "world");

        for (Cell cell : CELLS) {
            BlockPos position = nodePosition.offset(cell.offset());
            if (!world.isLoaded(position)) {
                return new Validation(
                        false,
                        Failure.CHUNK_NOT_LOADED,
                        position,
                        cell.kind()
                );
            }
            boolean matches = switch (cell.kind()) {
                case AURA_NODE -> world.isAuraNode(position, expectedNodeId);
                case GLASS -> world.isGlass(position);
                case WOODEN_SLAB -> world.isWoodenSlab(position);
            };
            if (!matches) {
                return new Validation(false, Failure.WRONG_BLOCK, position, cell.kind());
            }
        }
        return new Validation(true, Failure.NONE, nodePosition, CellKind.AURA_NODE);
    }

    public static List<BlockPos> materialPositions(BlockPos nodePosition) {
        Objects.requireNonNull(nodePosition, "nodePosition");
        return CELLS.stream()
                .filter(cell -> cell.kind() != CellKind.AURA_NODE)
                .map(cell -> nodePosition.offset(cell.offset()))
                .toList();
    }

    private static List<Cell> buildCells() {
        List<Cell> cells = new ArrayList<>(36);
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    CellKind kind = x == 0 && y == 0 && z == 0
                            ? CellKind.AURA_NODE
                            : CellKind.GLASS;
                    cells.add(new Cell(new BlockPos(x, y, z), kind));
                }
            }
        }
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                cells.add(new Cell(new BlockPos(x, 2, z), CellKind.WOODEN_SLAB));
            }
        }
        return List.copyOf(cells);
    }

    public interface WorldView {
        boolean isLoaded(BlockPos position);

        boolean isAuraNode(BlockPos position, UUID expectedNodeId);

        boolean isGlass(BlockPos position);

        boolean isWoodenSlab(BlockPos position);
    }

    public record Cell(BlockPos offset, CellKind kind) {
        public Cell {
            offset = Objects.requireNonNull(offset, "offset").immutable();
            kind = Objects.requireNonNull(kind, "kind");
        }
    }

    public record Validation(
            boolean valid,
            Failure failure,
            BlockPos position,
            CellKind expected
    ) {
        public Validation {
            failure = Objects.requireNonNull(failure, "failure");
            position = Objects.requireNonNull(position, "position").immutable();
            expected = Objects.requireNonNull(expected, "expected");
        }
    }

    public enum CellKind {
        AURA_NODE,
        GLASS,
        WOODEN_SLAB
    }

    public enum Failure {
        NONE,
        CHUNK_NOT_LOADED,
        WRONG_BLOCK
    }
}
