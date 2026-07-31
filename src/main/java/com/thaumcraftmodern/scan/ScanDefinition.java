package com.thaumcraftmodern.scan;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

public record ScanDefinition(
        ScanTargetType type,
        String targetId,
        String displayKey,
        List<AspectReward> aspects
) {
    public ScanDefinition {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(targetId, "targetId");
        if (ResourceLocation.tryParse(targetId) == null) {
            throw new IllegalArgumentException("targetId must be a valid resource location: " + targetId);
        }
        displayKey = displayKey == null ? "" : displayKey;
        aspects = List.copyOf(Objects.requireNonNull(aspects, "aspects"));
        if (aspects.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("aspect rewards cannot contain null");
        }
    }

    public String scanKey() {
        return type.name().toLowerCase(java.util.Locale.ROOT) + ":" + targetId;
    }
}
