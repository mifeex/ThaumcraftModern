package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.GolemMaterial;
import com.thaumcraftmodern.registry.ModEntities;

public final class StrawGolemItem extends ClassicGolemItem {
    public StrawGolemItem(Properties properties) {
        super(properties, GolemMaterial.STRAW, ModEntities.STRAW_GOLEM);
    }
}
