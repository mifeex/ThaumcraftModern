package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectCostProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Server-side TC4 generateTags equivalent for arbitrary loaded recipes. */
public final class RuntimeRecipeScanGenerator {
    static final double INGREDIENT_SCALE = 0.75D;
    static final int MAX_ASPECT = 64;
    static final int MAX_INGREDIENTS = 64;
    static final int MAX_CHOICES_PER_INGREDIENT = 256;
    static final int MAX_RECIPES = 100_000;
    static final int MAX_PASSES = 64;
    private static volatile boolean invalidated = true;

    private RuntimeRecipeScanGenerator() {}

    public static void invalidate() {
        invalidated = true;
    }

    public static synchronized int rebuildIfNeeded(MinecraftServer server) {
        if (!invalidated) {
            return -1;
        }
        Map<Item, Map<String, Integer>> known = explicitItemAspects();
        Set<Item> explicitItems = Set.copyOf(known.keySet());
        Map<Item, Integer> depths = new HashMap<>();
        explicitItems.forEach(item -> depths.put(item, 0));
        Map<Item, Map<String, Candidate>> candidates = new HashMap<>();
        List<Recipe<?>> recipes = server.getRecipeManager().getRecipes().stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .limit(MAX_RECIPES)
                .toList();

        for (int pass = 0; pass < MAX_PASSES; pass++) {
            Map<Item, Map<String, Integer>> snapshot = Map.copyOf(known);
            Map<Item, Integer> depthSnapshot = Map.copyOf(depths);
            for (Recipe<?> recipe : recipes) {
                ItemStack output = recipe.getResultItem(server.registryAccess());
                if (output.isEmpty() || output.getCount() <= 0
                        || explicitItems.contains(output.getItem())) {
                    continue;
                }
                Optional<Candidate> derived = derive(
                        recipe, output, snapshot, depthSnapshot);
                if (derived.isEmpty()) {
                    continue;
                }
                candidates.computeIfAbsent(output.getItem(), ignored -> new HashMap<>())
                        .put(recipe.getId().toString(), derived.get());
            }
            boolean changed = false;
            for (Map.Entry<Item, Map<String, Candidate>> entry : candidates.entrySet()) {
                if (explicitItems.contains(entry.getKey())) {
                    continue;
                }
                Map<String, Integer> averaged = weightedAverage(
                        entry.getValue().values());
                if (!averaged.isEmpty() && !averaged.equals(known.get(entry.getKey()))) {
                    known.put(entry.getKey(), averaged);
                    depths.put(entry.getKey(), entry.getValue().values().stream()
                            .mapToInt(Candidate::depth).min().orElse(MAX_PASSES));
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        List<ScanDefinition> generated = new ArrayList<>();
        for (Map.Entry<Item, Map<String, Candidate>> entry : candidates.entrySet()) {
            Item item = entry.getKey();
            ScanTargetType type = ScanTargetType.ITEM;
            String target = BuiltInRegistries.ITEM.getKey(item).toString();
            if (item instanceof BlockItem blockItem) {
                type = ScanTargetType.BLOCK;
                target = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString();
            }
            Map<String, Integer> averaged = weightedAverage(entry.getValue().values());
            List<AspectReward> rewards = averaged.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(value -> new AspectReward(value.getKey(), value.getValue()))
                    .toList();
            if (!rewards.isEmpty()) {
                generated.add(new ScanDefinition(type, target, "", rewards));
            }
        }
        ScanRegistry.replaceGenerated(generated);
        invalidated = false;
        ThaumcraftModern.LOGGER.info(
                "Generated {} runtime recipe scan definitions from {} loaded recipes",
                generated.size(), recipes.size());
        return generated.size();
    }

    static Optional<Candidate> derive(
            Recipe<?> recipe,
            ItemStack output,
            Map<Item, Map<String, Integer>> known,
            Map<Item, Integer> depths
    ) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.size() > MAX_INGREDIENTS) {
            return Optional.empty();
        }
        Map<String, Integer> total = new LinkedHashMap<>();
        int used = 0;
        int maxDepth = 0;
        int existingOutputDepth = depths.getOrDefault(output.getItem(), Integer.MAX_VALUE);
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            Map<String, Integer> cheapest = null;
            int cheapestVis = Integer.MAX_VALUE;
            int cheapestDepth = Integer.MAX_VALUE;
            ItemStack[] choices = ingredient.getItems();
            if (choices.length > MAX_CHOICES_PER_INGREDIENT) {
                return Optional.empty();
            }
            for (ItemStack choice : choices) {
                if (choice.isEmpty() || choice.getItem() == output.getItem()
                        || choice.getItem().hasCraftingRemainingItem()) {
                    continue;
                }
                Map<String, Integer> aspects = known.get(choice.getItem());
                if (aspects == null || aspects.isEmpty()) {
                    continue;
                }
                int choiceDepth = depths.getOrDefault(choice.getItem(), Integer.MAX_VALUE);
                if (choiceDepth >= existingOutputDepth || choiceDepth >= MAX_PASSES) {
                    continue;
                }
                int vis = aspects.values().stream().mapToInt(Integer::intValue).sum();
                if (vis < cheapestVis || vis == cheapestVis
                        && choiceDepth < cheapestDepth) {
                    cheapest = aspects;
                    cheapestVis = vis;
                    cheapestDepth = choiceDepth;
                }
            }
            if (cheapest == null) {
                return Optional.empty();
            }
            used++;
            maxDepth = Math.max(maxDepth, cheapestDepth);
            cheapest.forEach((aspect, amount) -> total.merge(aspect, amount, Integer::sum));
        }
        if (used == 0) {
            return Optional.empty();
        }
        int count = Math.max(1, output.getCount());
        Map<String, Integer> result = scaleIngredients(total, count);
        if (recipe instanceof AspectCostProvider provider) {
            for (AspectCost cost : provider.aspectCosts()) {
                putPositive(result, cost.aspectId(),
                        (int) Math.floor(Math.sqrt(cost.amount()) / count));
            }
        }
        int weight = Math.max(1, total.values().stream().mapToInt(Integer::intValue).sum());
        if (recipe instanceof AspectCostProvider provider) {
            weight += provider.aspectCosts().stream()
                    .mapToInt(AspectCost::amount).sum();
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(new Candidate(
                recipe.getId().toString(), Map.copyOf(result),
                Math.min(4096, weight), maxDepth + 1));
    }

    static Map<String, Integer> scaleIngredients(
            Map<String, Integer> total,
            int outputCount
    ) {
        int count = Math.max(1, outputCount);
        Map<String, Integer> result = new LinkedHashMap<>();
        total.forEach((aspect, amount) -> putPositive(
                result, aspect,
                (int) Math.floor(amount * INGREDIENT_SCALE / count)));
        return result;
    }

    static Map<String, Integer> weightedAverage(Collection<Candidate> candidates) {
        long totalWeight = candidates.stream().mapToLong(Candidate::weight).sum();
        if (totalWeight <= 0) {
            return Map.of();
        }
        Map<String, Long> weighted = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            candidate.aspects().forEach((aspect, amount) -> weighted.merge(
                    aspect, (long) amount * candidate.weight(), Long::sum));
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        weighted.forEach((aspect, amount) -> putPositive(
                result, aspect, (int) Math.floor((double) amount / totalWeight)));
        return Map.copyOf(result);
    }

    private static Map<Item, Map<String, Integer>> explicitItemAspects() {
        Map<Item, Map<String, Integer>> result = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            ScanRegistry.findExplicitForItem(stack).ifPresent(definition -> {
                Map<String, Integer> aspects = new LinkedHashMap<>();
                definition.aspects().forEach(reward -> aspects.merge(
                        reward.aspectId(), reward.amount(), Integer::sum));
                if (!aspects.isEmpty()) {
                    result.put(item, Map.copyOf(aspects));
                }
            });
        }
        return result;
    }

    private static void putPositive(Map<String, Integer> target, String aspect, int amount) {
        if (amount > 0) {
            target.merge(aspect, Math.min(MAX_ASPECT, amount),
                    (left, right) -> Math.min(MAX_ASPECT, left + right));
        }
    }

    record Candidate(
            String recipeId,
            Map<String, Integer> aspects,
            int weight,
            int depth
    ) {}
}
