package com.thaumcraftmodern.crucible;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Validates every catalyst precondition before mutating water or essentia.
 */
public final class CrucibleTransaction {
    public static final int WATER_PER_RECIPE_MB = 50;

    private CrucibleTransaction() {
    }

    public static Optional<ItemStack> craft(
            ItemStack catalyst,
            int water,
            EssentiaStore essentia,
            Predicate<String> knowsResearch
    ) {
        if (catalyst.isEmpty() || water < WATER_PER_RECIPE_MB) {
            return Optional.empty();
        }
        Optional<CrucibleRecipeDefinition> match =
                CrucibleRecipeRegistry.findMatching(
                        catalyst,
                        essentia,
                        knowsResearch
                );
        if (match.isEmpty()) {
            return Optional.empty();
        }
        CrucibleRecipeDefinition recipe = match.get();
        if (!essentia.removeAll(recipe.aspects())) {
            throw new IllegalStateException(
                    "Validated Crucible transaction lost required essentia"
            );
        }
        return Optional.of(recipe.output());
    }
}
