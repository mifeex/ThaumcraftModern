package com.thaumcraftmodern.entity;

/** The twelve TC4 golem animation cores, preserving their legacy metadata order. */
public enum GolemCoreType {
    FILL(0, "fill"), EMPTY(1, "empty"), GATHER(2, "gather"), HARVEST(3, "harvest"),
    GUARD(4, "guard"), LIQUID(5, "liquid"), ALCHEMY(6, "alchemy"), LUMBER(7, "lumber"),
    USE(8, "use"), BUTCHER(9, "butcher"), SORTING(10, "sorting"), FISHING(11, "fishing");

    private final int legacyId;
    private final String id;

    GolemCoreType(int legacyId, String id) {
        this.legacyId = legacyId;
        this.id = id;
    }

    public int legacyId() { return legacyId; }
    public String id() { return id; }
    public String textureId() { return this == ALCHEMY ? "essentia" : this == FISHING ? "fish" : id; }
    public boolean hasInventory() {
        return this == FILL || this == EMPTY || this == GATHER || this == LIQUID || this == USE;
    }
    public boolean hasGui() {
        return hasInventory() || this == GUARD || this == SORTING;
    }
    public int configurationSlots(int ignisUpgrades) {
        int fire = Math.max(0, ignisUpgrades);
        if (this == LIQUID) return 1 + fire;
        return hasInventory() ? 6 + fire * 6 : 1;
    }

    public static GolemCoreType byLegacyId(int id) {
        for (GolemCoreType type : values()) if (type.legacyId == id) return type;
        return null;
    }
}
