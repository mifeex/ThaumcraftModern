package com.thaumcraftmodern.crystal;

public enum CrystalClusterVariant {
    AIR("air", 0xFFFF7E),
    FIRE("fire", 0xFF3C01),
    WATER("water", 0x0090FF),
    EARTH("earth", 0x00A000),
    ORDER("order", 0xEECCFF),
    ENTROPY("entropy", 0x555577),
    BALANCED("balanced", 0xEECCFF);

    private final String id;
    private final int color;

    CrystalClusterVariant(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public int legacyMetadata() {
        return ordinal();
    }

    public int crystalColor(int crystalIndex) {
        if (this != BALANCED || crystalIndex == 0) {
            return color;
        }
        return crystalIndex == 5
                ? ENTROPY.color
                : values()[crystalIndex - 1].color;
    }
}
