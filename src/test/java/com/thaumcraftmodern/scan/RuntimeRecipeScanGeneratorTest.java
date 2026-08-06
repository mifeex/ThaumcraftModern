package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeRecipeScanGeneratorTest {
    @Test
    void ingredientAspectsUseClassicScaleOutputDivisionFloorAndCap() {
        assertEquals(
                Map.of("terra", 4, "machina", 64),
                RuntimeRecipeScanGenerator.scaleIngredients(
                        Map.of("terra", 12, "machina", 1000), 2));
    }

    @Test
    void zeroAndNegativeContributionsAreDiscarded() {
        assertEquals(
                Map.of(),
                RuntimeRecipeScanGenerator.scaleIngredients(
                        Map.of("terra", 1, "perditio", -10), 8));
    }

    @Test
    void recipesAreAveragedByIngredientComplexityInsteadOfTakingMinimum() {
        var simple = new RuntimeRecipeScanGenerator.Candidate(
                "example:simple", Map.of("terra", 2), 2, 1);
        var complex = new RuntimeRecipeScanGenerator.Candidate(
                "example:complex", Map.of("terra", 8, "machina", 6), 8, 3);

        assertEquals(
                Map.of("terra", 6, "machina", 4),
                RuntimeRecipeScanGenerator.weightedAverage(List.of(simple, complex)));
    }
}
