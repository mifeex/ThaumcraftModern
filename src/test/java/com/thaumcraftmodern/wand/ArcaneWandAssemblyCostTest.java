package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.arcane.ArcaneWandAssemblyRecipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArcaneWandAssemblyCostTest {
    @Test
    void classicWandsMultiplyRodAndCapCraftCosts() {
        assertEquals(3, ArcaneWandAssemblyRecipe.classicCraftCost(1, 3, false));
        assertEquals(36, ArcaneWandAssemblyRecipe.classicCraftCost(4, 9, false));
        assertEquals(216, ArcaneWandAssemblyRecipe.classicCraftCost(9, 24, false));
    }

    @Test
    void classicSceptresMultiplyByOneAndAHalfAndTruncate() {
        assertEquals(13, ArcaneWandAssemblyRecipe.classicCraftCost(3, 3, true));
        assertEquals(54, ArcaneWandAssemblyRecipe.classicCraftCost(4, 9, true));
    }
}
