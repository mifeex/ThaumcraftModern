package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

public record ResearchPageDefinition(
        Type type,
        String titleKey,
        String bodyKey,
        String recipeId,
        List<AspectCost> aspectCosts,
        InfusionDisplayDefinition infusionDisplay,
        List<String> recipeIds
) {
    public ResearchPageDefinition {
        Objects.requireNonNull(type, "type");
        titleKey = titleKey == null ? "" : titleKey;
        bodyKey = bodyKey == null ? "" : bodyKey;
        recipeId = recipeId == null ? "" : recipeId;
        aspectCosts = List.copyOf(Objects.requireNonNull(
                aspectCosts,
                "aspectCosts"
        ));
        recipeIds = List.copyOf(Objects.requireNonNull(recipeIds, "recipeIds"));
        if (recipeIds.isEmpty() && !recipeId.isBlank()) {
            recipeIds = List.of(recipeId);
        } else if (!recipeIds.isEmpty()) {
            recipeId = recipeIds.get(0);
        }
        if ((type == Type.RECIPE || type == Type.COMPOUND_CRAFTING)
                && ResourceLocation.tryParse(recipeId) == null) {
            throw new IllegalArgumentException(
                    "recipeId must be a valid resource location for a recipe page: "
                            + recipeId
            );
        }
        for (String cycledRecipe : recipeIds) {
            if (ResourceLocation.tryParse(cycledRecipe) == null) {
                throw new IllegalArgumentException(
                        "recipeIds must contain valid resource locations: "
                                + cycledRecipe
                );
            }
        }
        if ((type == Type.INFUSION) != (infusionDisplay != null)) {
            throw new IllegalArgumentException(
                    "infusion page and display definition must be provided together"
            );
        }
    }

    public ResearchPageDefinition(
            Type type,
            String titleKey,
            String bodyKey,
            String recipeId
    ) {
        this(type, titleKey, bodyKey, recipeId, List.of(), null, List.of());
    }

    public ResearchPageDefinition(
            Type type,
            String titleKey,
            String bodyKey,
            String recipeId,
            List<AspectCost> aspectCosts
    ) {
        this(type, titleKey, bodyKey, recipeId, aspectCosts, null, List.of());
    }

    public ResearchPageDefinition(
            Type type,
            String titleKey,
            String bodyKey,
            String recipeId,
            List<AspectCost> aspectCosts,
            InfusionDisplayDefinition infusionDisplay
    ) {
        this(
                type,
                titleKey,
                bodyKey,
                recipeId,
                aspectCosts,
                infusionDisplay,
                List.of()
        );
    }

    public enum Type {
        TEXT,
        RECIPE,
        COMPOUND_CRAFTING,
        INFUSION,
        UNAVAILABLE
    }
}
