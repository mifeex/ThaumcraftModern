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

    @Override
    default List<AspectCost> aspectCosts() {
        return visCost().aspectCosts();
    }
}
