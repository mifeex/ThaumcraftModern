package com.thaumcraftmodern.crucible;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record CrucibleRecipeDefinition(
        ResourceLocation id,
        String research,
        Ingredient catalyst,
        ItemStack output,
        Map<String, Integer> aspects
) {
    public CrucibleRecipeDefinition {
        Objects.requireNonNull(id, "id");
        research = Objects.requireNonNull(research, "research").trim();
        Objects.requireNonNull(catalyst, "catalyst");
        output = Objects.requireNonNull(output, "output").copy();
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Crucible output cannot be empty");
        }
        LinkedHashMap<String, Integer> validated = new LinkedHashMap<>();
        Objects.requireNonNull(aspects, "aspects").forEach((aspect, amount) -> {
            if (aspect == null || aspect.isBlank() || amount == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid crucible aspect cost");
            }
            validated.put(aspect, amount);
        });
        if (validated.isEmpty()) {
            throw new IllegalArgumentException("Crucible recipe needs essentia");
        }
        aspects = Map.copyOf(validated);
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}
