package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Exact TC4 compound-page descriptor for the 1x3x1 Thaumatorium assembly. */
public final class ThaumatoriumResearchRecipe {
    public static final ResourceLocation ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "thaumatorium_construct"
    );

    private ThaumatoriumResearchRecipe() {
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                1,
                3,
                1,
                List.of(
                        new AspectCost("ignis", 15),
                        new AspectCost("ordo", 30),
                        new AspectCost("aqua", 30)
                ),
                List.of(
                        Cell.ALCHEMICAL_CONSTRUCT,
                        Cell.ALCHEMICAL_CONSTRUCT,
                        Cell.ALCHEMICAL_FURNACE
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
        ALCHEMICAL_CONSTRUCT,
        ALCHEMICAL_FURNACE
    }
}
