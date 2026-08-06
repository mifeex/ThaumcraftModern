package com.thaumcraftmodern.entity;

/** TC4 ItemGolemUpgrade metadata order and modern registry suffixes. */
public enum GolemUpgradeType {
    AER(0, "aer"), TERRA(1, "terra"), IGNIS(2, "ignis"), AQUA(3, "aqua"),
    ORDO(4, "ordo"), PERDITIO(5, "perditio");

    private final int legacyId;
    private final String id;

    GolemUpgradeType(int legacyId, String id) { this.legacyId = legacyId; this.id = id; }
    public int legacyId() { return legacyId; }
    public String id() { return id; }

    public static GolemUpgradeType byLegacyId(int id) {
        for (GolemUpgradeType type : values()) if (type.legacyId == id) return type;
        return null;
    }
}
