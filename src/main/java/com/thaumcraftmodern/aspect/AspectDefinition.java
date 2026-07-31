package com.thaumcraftmodern.aspect;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

/**
 * Immutable, runtime-independent description of a Thaumcraft aspect.
 */
public record AspectDefinition(
        String id,
        int color,
        String icon,
        List<String> components,
        int order
) {
    private static final int MAX_RGB_COLOR = 0xFFFFFF;

    public AspectDefinition {
        id = requireIdentifier(id, "id");
        icon = requireText(icon, "icon");
        if (ResourceLocation.tryParse(icon) == null) {
            throw new IllegalArgumentException("icon must be a valid resource location: " + icon);
        }
        components = List.copyOf(Objects.requireNonNull(components, "components"));

        if (color < 0 || color > MAX_RGB_COLOR) {
            throw new IllegalArgumentException("color must be a 24-bit RGB value");
        }
        if (components.size() != 0 && components.size() != 2) {
            throw new IllegalArgumentException("an aspect must have either zero or two components");
        }

        for (String component : components) {
            requireIdentifier(component, "component");
            if (id.equals(component)) {
                throw new IllegalArgumentException("an aspect cannot directly contain itself: " + id);
            }
        }
    }

    public AspectDefinition(String id, int color, String icon) {
        this(id, color, icon, List.of(), 0);
    }

    public AspectDefinition(String id, int color, String icon, String firstComponent, String secondComponent) {
        this(id, color, icon, List.of(firstComponent, secondComponent), 0);
    }

    public AspectDefinition(String id, int color, String icon, List<String> components) {
        this(id, color, icon, components, 0);
    }

    public boolean isPrimal() {
        return components.isEmpty();
    }

    public boolean isCompound() {
        return components.size() == 2;
    }

    public boolean hasDirectComponent(String aspectId) {
        return aspectId != null && components.contains(aspectId);
    }

    private static String requireIdentifier(String value, String fieldName) {
        String validated = requireText(value, fieldName);
        if (!validated.equals(validated.toLowerCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException(fieldName + " must be lowercase: " + validated);
        }
        if (validated.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(fieldName + " cannot contain whitespace: " + validated);
        }
        return validated;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank() || !value.equals(value.trim())) {
            throw new IllegalArgumentException(fieldName + " must be non-blank and trimmed");
        }
        return value;
    }
}
