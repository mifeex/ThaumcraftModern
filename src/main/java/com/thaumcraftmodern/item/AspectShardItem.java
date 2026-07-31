package com.thaumcraftmodern.item;

import net.minecraft.world.item.Item;

public final class AspectShardItem extends Item {
    private final String aspectId;
    private final int color;

    public AspectShardItem(String aspectId, int color, Properties properties) {
        super(properties);
        this.aspectId = aspectId;
        this.color = color;
    }

    public String aspectId() {
        return aspectId;
    }

    public int color() {
        return color;
    }
}

