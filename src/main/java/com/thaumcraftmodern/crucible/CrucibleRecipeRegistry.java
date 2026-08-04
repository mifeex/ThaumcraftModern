package com.thaumcraftmodern.crucible;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class CrucibleRecipeRegistry {
    private static volatile List<CrucibleRecipeDefinition> recipes = List.of();

    private CrucibleRecipeRegistry() {
    }

    public static synchronized void replace(
            Collection<CrucibleRecipeDefinition> definitions
    ) {
        recipes = List.copyOf(definitions);
    }

    public static List<CrucibleRecipeDefinition> all() {
        return recipes;
    }

    public static Optional<CrucibleRecipeDefinition> findMatching(
            ItemStack catalyst,
            EssentiaStore essentia,
            Predicate<String> knowsResearch
    ) {
        return recipes.stream()
                .filter(recipe -> recipe.research().isBlank()
                        || knowsResearch.test(recipe.research()))
                .filter(recipe -> recipe.matchesCatalyst(catalyst))
                .filter(recipe -> essentia.contains(recipe.aspects()))
                // TC4 selects the matching recipe with the greatest number
                // of distinct required aspects. This resolves shared
                // catalysts such as glowstone (Nitor vs duplication).
                .sorted(Comparator.comparingInt(
                        (CrucibleRecipeDefinition recipe) ->
                                recipe.aspects().size()
                ).reversed())
                .findFirst();
    }
}
