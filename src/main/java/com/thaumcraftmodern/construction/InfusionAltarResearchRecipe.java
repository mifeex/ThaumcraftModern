package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Exact TC4 compound-page descriptor for the 3x3x3 Infusion Altar. */
public final class InfusionAltarResearchRecipe {
    public static final ResourceLocation ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "infusion_altar_construct"
    );

    private InfusionAltarResearchRecipe() {
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                3,
                3,
                3,
                List.of(
                        new AspectCost("ignis", 25),
                        new AspectCost("terra", 25),
                        new AspectCost("ordo", 25),
                        new AspectCost("aer", 25),
                        new AspectCost("perditio", 25),
                        new AspectCost("aqua", 25)
                ),
                List.of(
                        Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
                        Cell.EMPTY, Cell.RUNIC_MATRIX, Cell.EMPTY,
                        Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,

                        Cell.ARCANE_STONE, Cell.EMPTY, Cell.ARCANE_STONE,
                        Cell.EMPTY, Cell.EMPTY, Cell.EMPTY,
                        Cell.ARCANE_STONE, Cell.EMPTY, Cell.ARCANE_STONE,

                        Cell.ARCANE_STONE_BRICK, Cell.EMPTY, Cell.ARCANE_STONE_BRICK,
                        Cell.EMPTY, Cell.ARCANE_PEDESTAL, Cell.EMPTY,
                        Cell.ARCANE_STONE_BRICK, Cell.EMPTY, Cell.ARCANE_STONE_BRICK
                )
        );
    }

    public record Snapshot(
            int width,
            int height,
            int depth,
            List<AspectCost> costs,
            List<Cell> cells
    ) {
        public Snapshot {
            if (width <= 0 || height <= 0 || depth <= 0) {
                throw new IllegalArgumentException(
                        "compound recipe dimensions must be positive"
                );
            }
            costs = List.copyOf(costs);
            cells = List.copyOf(cells);
            int expected = Math.multiplyExact(Math.multiplyExact(width, height), depth);
            if (cells.size() != expected) {
                throw new IllegalArgumentException(
                        "compound recipe has " + cells.size()
                                + " cells, expected " + expected
                );
            }
        }
    }

    public enum Cell {
        EMPTY,
        RUNIC_MATRIX,
        ARCANE_STONE,
        ARCANE_STONE_BRICK,
        ARCANE_PEDESTAL
    }
}
