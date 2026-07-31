package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime view of the classic NODEJAR compound construction recipe.
 *
 * <p>The Thaumonomicon page reads this descriptor instead of duplicating the
 * multiblock or its vis cost in client-only data. Cell order is the exact
 * top-to-bottom order consumed by TC4's compound-page renderer.</p>
 */
public final class NodeJarResearchRecipe {
    public static final ResourceLocation ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "node_jar_capture"
    );

    private static final List<PrimalAspect> CLASSIC_COST_ORDER = List.of(
            PrimalAspect.IGNIS,
            PrimalAspect.TERRA,
            PrimalAspect.AER,
            PrimalAspect.AQUA,
            PrimalAspect.ORDO,
            PrimalAspect.PERDITIO
    );
    private static final Snapshot SNAPSHOT = createSnapshot();

    private NodeJarResearchRecipe() {
    }

    public static Snapshot snapshot() {
        return SNAPSHOT;
    }

    private static Snapshot createSnapshot() {
        List<NodeJarStructure.Cell> source = NodeJarStructure.cells();
        int minX = source.stream().mapToInt(cell -> cell.offset().getX()).min()
                .orElseThrow();
        int maxX = source.stream().mapToInt(cell -> cell.offset().getX()).max()
                .orElseThrow();
        int minY = source.stream().mapToInt(cell -> cell.offset().getY()).min()
                .orElseThrow();
        int maxY = source.stream().mapToInt(cell -> cell.offset().getY()).max()
                .orElseThrow();
        int minZ = source.stream().mapToInt(cell -> cell.offset().getZ()).min()
                .orElseThrow();
        int maxZ = source.stream().mapToInt(cell -> cell.offset().getZ()).max()
                .orElseThrow();

        Map<BlockPos, NodeJarStructure.CellKind> byOffset = new LinkedHashMap<>();
        for (NodeJarStructure.Cell cell : source) {
            NodeJarStructure.CellKind previous = byOffset.put(
                    cell.offset(),
                    cell.kind()
            );
            if (previous != null) {
                throw new IllegalStateException(
                        "NODEJAR runtime structure has duplicate cell "
                                + cell.offset()
                );
            }
        }

        List<NodeJarStructure.CellKind> renderCells = new ArrayList<>(source.size());
        for (int y = maxY; y >= minY; y--) {
            for (int z = maxZ; z >= minZ; z--) {
                for (int x = maxX; x >= minX; x--) {
                    BlockPos offset = new BlockPos(x, y, z);
                    NodeJarStructure.CellKind kind = byOffset.get(offset);
                    if (kind == null) {
                        throw new IllegalStateException(
                                "NODEJAR runtime structure has a gap at " + offset
                        );
                    }
                    renderCells.add(kind);
                }
            }
        }

        List<AspectCost> costs = CLASSIC_COST_ORDER.stream()
                .map(aspect -> new AspectCost(
                        aspect,
                        NodeJarCost.BASE.get(aspect)
                ))
                .toList();
        return new Snapshot(
                maxX - minX + 1,
                maxY - minY + 1,
                maxZ - minZ + 1,
                costs,
                renderCells
        );
    }

    public record AspectCost(PrimalAspect aspect, int amount) {
        public AspectCost {
            if (aspect == null) {
                throw new IllegalArgumentException("aspect cannot be null");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException(
                        "aspect cost must be positive"
                );
            }
        }
    }

    public record Snapshot(
            int width,
            int height,
            int depth,
            List<AspectCost> costs,
            List<NodeJarStructure.CellKind> cells
    ) {
        public Snapshot {
            if (width <= 0 || height <= 0 || depth <= 0) {
                throw new IllegalArgumentException(
                        "compound recipe dimensions must be positive"
                );
            }
            costs = List.copyOf(costs);
            cells = List.copyOf(cells);
            int expectedCells = Math.multiplyExact(
                    Math.multiplyExact(width, height),
                    depth
            );
            if (cells.size() != expectedCells) {
                throw new IllegalArgumentException(
                        "compound recipe has " + cells.size()
                                + " cells, expected " + expectedCells
                );
            }
        }
    }
}
