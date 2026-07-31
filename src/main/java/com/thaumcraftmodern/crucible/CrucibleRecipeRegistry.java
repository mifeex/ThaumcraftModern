package com.thaumcraftmodern.crucible;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
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
                .filter(recipe -> recipe.catalyst().test(catalyst))
                .filter(recipe -> essentia.contains(recipe.aspects()))
                .findFirst();
    }
}
