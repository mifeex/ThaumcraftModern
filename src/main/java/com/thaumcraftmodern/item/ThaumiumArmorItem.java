package com.thaumcraftmodern.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;

public final class ThaumiumArmorItem extends ArmorItem {
    public ThaumiumArmorItem(Type type, Properties properties) {
        super(ThaumiumArmorMaterial.INSTANCE, type, properties);
    }

}
