package com.thaumcraftmodern.infusion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class InfusionRecipeRegistry {
    private static volatile Map<ResourceLocation, InfusionRecipeDefinition> recipes = Map.of();

    private InfusionRecipeRegistry() {
    }

    public static synchronized void replace(Collection<InfusionRecipeDefinition> definitions) {
        LinkedHashMap<ResourceLocation, InfusionRecipeDefinition> next = new LinkedHashMap<>();
        definitions.stream().sorted(java.util.Comparator.comparing(recipe -> recipe.id().toString()))
                .forEach(recipe -> next.put(recipe.id(), recipe));
        recipes = Map.copyOf(next);
    }

    public static Collection<InfusionRecipeDefinition> all() {
        return recipes.values();
    }

    public static Optional<InfusionRecipeDefinition> find(ResourceLocation id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public static Optional<InfusionRecipeDefinition> findMatching(
            ItemStack central,
            List<ItemStack> components,
            Predicate<String> knowsResearch
    ) {
        return recipes.values().stream()
                .filter(recipe -> recipe.research().isBlank()
                        || knowsResearch.test(recipe.research()))
                .filter(recipe -> recipe.matchesCentral(central))
                .filter(recipe -> matchesComponents(
                        recipe.effectiveComponents(central), components))
                .findFirst();
    }

    static boolean matchesComponents(List<Ingredient> required, List<ItemStack> present) {
        if (required.size() != present.size()) {
            return false;
        }
        boolean[] used = new boolean[present.size()];
        for (Ingredient ingredient : required) {
            boolean found = false;
            for (int index = 0; index < present.size(); index++) {
                if (!used[index] && ingredient.test(present.get(index))) {
                    used[index] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public static List<ItemStack> copySingles(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                copies.add(copy);
            }
        }
        return List.copyOf(copies);
    }
}
