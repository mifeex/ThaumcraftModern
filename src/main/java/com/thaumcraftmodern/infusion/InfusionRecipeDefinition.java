package com.thaumcraftmodern.infusion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Server-side, datapack-owned description of a TC4 infusion recipe. */
public record InfusionRecipeDefinition(
        ResourceLocation id,
        String research,
        int instability,
        Ingredient central,
        List<Ingredient> components,
        ItemStack output,
        Map<String, Integer> essentia
) {
    public InfusionRecipeDefinition {
        Objects.requireNonNull(id, "id");
        research = Objects.requireNonNull(research, "research").trim();
        if (instability < 0) {
            throw new IllegalArgumentException("Infusion instability cannot be negative");
        }
        Objects.requireNonNull(central, "central");
        components = List.copyOf(Objects.requireNonNull(components, "components"));
        if (components.isEmpty()) {
            throw new IllegalArgumentException("Infusion recipe needs components");
        }
        output = Objects.requireNonNull(output, "output").copy();
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Infusion output cannot be empty");
        }
        LinkedHashMap<String, Integer> costs = new LinkedHashMap<>();
        Objects.requireNonNull(essentia, "essentia").forEach((aspect, amount) -> {
            if (aspect == null || aspect.isBlank() || amount == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid infusion essentia cost");
            }
            costs.put(aspect, amount);
        });
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Infusion recipe needs essentia");
        }
        essentia = Collections.unmodifiableMap(costs);
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}
