package com.thaumcraftmodern.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** TC4 4.2.3.5 EnchantmentHaste. */
public final class HasteEnchantment extends Enchantment {
    public HasteEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return 51;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BookItem
                || stack.getItem() instanceof ArmorItem armor
                && armor.getType() == ArmorItem.Type.BOOTS;
    }
}
