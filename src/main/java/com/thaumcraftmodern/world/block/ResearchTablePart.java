package com.thaumcraftmodern.world.block;

import net.minecraft.util.StringRepresentable;

public enum ResearchTablePart implements StringRepresentable {
    MAIN("main"),
    COMPANION("companion");

    private final String serializedName;

    ResearchTablePart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
