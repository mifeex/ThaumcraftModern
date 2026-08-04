package com.thaumcraftmodern.crucible;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record CrucibleRecipeDefinition(
        ResourceLocation id,
        String research,
        Ingredient catalyst,
        String catalystAspect,
        ItemStack output,
        Map<String, Integer> aspects
) {
    public CrucibleRecipeDefinition {
        Objects.requireNonNull(id, "id");
        research = Objects.requireNonNull(research, "research").trim();
        Objects.requireNonNull(catalyst, "catalyst");
        catalystAspect = catalystAspect == null ? "" : catalystAspect.trim();
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
        aspects = Collections.unmodifiableMap(validated);
    }

    public CrucibleRecipeDefinition(ResourceLocation id, String research,
            Ingredient catalyst, ItemStack output, Map<String, Integer> aspects) {
        this(id, research, catalyst, "", output, aspects);
    }

    public boolean matchesCatalyst(ItemStack stack) {
        if (!catalyst.test(stack)) return false;
        return catalystAspect.isBlank()
                || com.thaumcraftmodern.item.EssentiaPhialItem.aspect(stack)
                .filter(catalystAspect::equals).isPresent();
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}
