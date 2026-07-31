package com.thaumcraftmodern.worldgen;

import java.util.Locale;

/**
 * Individually registered TC4 world sites. Keeping each kind distinct makes
 * it visible to vanilla's structure registry and therefore to
 * {@code /locate structure}.
 */
public enum LegacyStructureKind {
    ANCIENT_MOUND(150, 9, 6),
    ELDRITCH_RING(66, 3, 7),
    HILLTOP_STONES(40, 4, HilltopStonesGeneration.NODE_HEIGHT),
    AURA_TOTEM(360, 1, AuraTotemGeneration.MAX_NODE_HEIGHT),
    WIZARD_TOWER(480, 2, 11),
    BANKER_HOME(360, 2, 5);

    private final int rarity;
    private final int horizontalRadius;
    private final int height;

    LegacyStructureKind(int rarity, int horizontalRadius, int height) {
        this.rarity = rarity;
        this.horizontalRadius = horizontalRadius;
        this.height = height;
    }

    public int rarity() {
        return rarity;
    }

    public int horizontalRadius() {
        return horizontalRadius;
    }

    public int height() {
        return height;
    }

    public boolean isVillageBuilding() {
        return this == WIZARD_TOWER || this == BANKER_HOME;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static LegacyStructureKind fromSerializedName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
