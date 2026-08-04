package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectCostProvider;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public interface ArcaneRecipe
        extends Recipe<CraftingContainer>, AspectCostProvider {
    String researchId();

    ArcaneVisCost visCost();

    default ArcaneVisCost visCost(CraftingContainer container) {
        return visCost();
    }

    default List<String> requiredResearchIds(CraftingContainer container) {
        return researchId().isBlank() ? List.of() : List.of(researchId());
    }

    @Override
    default List<AspectCost> aspectCosts() {
        return visCost().aspectCosts();
    }
}
