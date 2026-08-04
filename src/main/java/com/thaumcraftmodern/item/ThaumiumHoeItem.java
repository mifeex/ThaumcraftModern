package com.thaumcraftmodern.item;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class ThaumiumHoeItem extends HoeItem {
    public ThaumiumHoeItem(Properties properties) {
        super(ThaumiumTier.INSTANCE, -3, 0.0F, properties);
    }

    /** The original TC4 hoe deliberately overrides the material value with 5. */
    @Override public int getEnchantmentValue() { return 5; }
    @Override public Rarity getRarity(ItemStack stack) { return Rarity.UNCOMMON; }
}
