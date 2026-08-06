package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Exact TC4 compound-page descriptor for the 3x3x3 Infernal Furnace. */
public final class InfernalFurnaceResearchRecipe {
    public static final ResourceLocation ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "infernal_furnace_construct");

    private InfernalFurnaceResearchRecipe() {}

    public static Snapshot snapshot() {
        return new Snapshot(3, 3, 3,
                List.of(new AspectCost("ignis", 50),
                        new AspectCost("terra", 50)),
                List.of(
                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS,
                        Cell.OBSIDIAN, Cell.EMPTY, Cell.OBSIDIAN,
                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS,

                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS,
                        Cell.OBSIDIAN, Cell.LAVA, Cell.IRON_BARS,
                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS,

                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS,
                        Cell.OBSIDIAN, Cell.OBSIDIAN, Cell.OBSIDIAN,
                        Cell.NETHER_BRICKS, Cell.OBSIDIAN, Cell.NETHER_BRICKS
                ));
    }

    public record Snapshot(int width, int height, int depth,
            List<AspectCost> costs, List<Cell> cells) {
        public Snapshot {
            costs = List.copyOf(costs);
            cells = List.copyOf(cells);
            if (cells.size() != width * height * depth) {
                throw new IllegalArgumentException("invalid Infernal Furnace layout");
            }
        }
    }

    public enum Cell { EMPTY, NETHER_BRICKS, OBSIDIAN, LAVA, IRON_BARS }
}
