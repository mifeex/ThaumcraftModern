package com.thaumcraftmodern.research;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ResearchCategoryDefinition(
        String id,
        String titleKey,
        String iconItem,
        String iconResource,
        String backgroundTexture,
        int order
) {
    public ResearchCategoryDefinition(
            String id,
            String titleKey,
            String iconItem,
            String backgroundTexture,
            int order
    ) {
        this(id, titleKey, iconItem, "", backgroundTexture, order);
    }

    public ResearchCategoryDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(titleKey, "titleKey");
        Objects.requireNonNull(iconItem, "iconItem");
        Objects.requireNonNull(iconResource, "iconResource");
        Objects.requireNonNull(backgroundTexture, "backgroundTexture");
        if (id.isBlank() || !id.equals(id.trim())) {
            throw new IllegalArgumentException("category id must be non-blank and trimmed");
        }
        if (titleKey.isBlank()) {
            throw new IllegalArgumentException("category title key must not be blank");
        }
        if (iconItem.isBlank() == iconResource.isBlank()) {
            throw new IllegalArgumentException(
                    "category must define exactly one icon item or icon resource"
            );
        }
        if (!iconItem.isBlank() && ResourceLocation.tryParse(iconItem) == null) {
            throw new IllegalArgumentException("invalid category icon item: " + iconItem);
        }
        if (!iconResource.isBlank()
                && ResourceLocation.tryParse(iconResource) == null) {
            throw new IllegalArgumentException(
                    "invalid category icon resource: " + iconResource
            );
        }
        if (ResourceLocation.tryParse(backgroundTexture) == null) {
            throw new IllegalArgumentException(
                    "invalid category background texture: " + backgroundTexture
            );
        }
    }
}
