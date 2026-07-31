package com.thaumcraftmodern.research;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Data-only Thaumonomicon preview for classic infusion recipe layout.
 *
 * <p>This is deliberately presentation data, not an executable recipe. It
 * lets archived and test research show faithful component placement without
 * pretending that the infusion backend is complete.</p>
 */
public record InfusionDisplayDefinition(
        String outputItem,
        String centralItem,
        List<ComponentStack> components,
        Instability instability,
        String detailKey
) {
    public InfusionDisplayDefinition {
        outputItem = requireItem(outputItem, "outputItem");
        centralItem = requireItem(centralItem, "centralItem");
        components = List.copyOf(Objects.requireNonNull(
                components,
                "components"
        ));
        if (components.isEmpty()) {
            throw new IllegalArgumentException(
                    "infusion display requires at least one component"
            );
        }
        instability = Objects.requireNonNull(instability, "instability");
        detailKey = detailKey == null ? "" : detailKey;
    }

    private static String requireItem(String value, String field) {
        Objects.requireNonNull(value, field);
        if (ResourceLocation.tryParse(value) == null) {
            throw new IllegalArgumentException(
                    "invalid infusion " + field + ": " + value
            );
        }
        return value;
    }

    public record ComponentStack(String item, int count) {
        public ComponentStack {
            item = requireItem(item, "component item");
            if (count <= 0) {
                throw new IllegalArgumentException(
                        "infusion component count must be positive"
                );
            }
        }
    }

    public enum Instability {
        NEGLIGIBLE,
        MINOR,
        MODERATE,
        HIGH,
        VERY_HIGH,
        DANGEROUS;

        public static Instability parse(String value) {
            return valueOf(value.toUpperCase(Locale.ROOT));
        }

        public String translationKey() {
            return "screen.thaumcraftmodern.thaumonomicon.instability."
                    + name().toLowerCase(Locale.ROOT);
        }

        public int color() {
            return switch (this) {
                case NEGLIGIBLE -> 0x5E8D36;
                case MINOR -> 0x99A52D;
                case MODERATE -> 0xC000C0;
                case HIGH -> 0xD06A00;
                case VERY_HIGH, DANGEROUS -> 0xB02020;
            };
        }
    }
}
