package com.thaumcraftmodern.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Exact TC4 thaumium tool material: level 3, 500 uses, speed 7, damage 2.5, enchantability 18. */
public enum ThaumiumTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 500; }
    @Override public float getSpeed() { return 7.0F; }
    @Override public float getAttackDamageBonus() { return 2.5F; }
    @Override public int getLevel() { return 3; }
    @Override public int getEnchantmentValue() { return 18; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/thaumium")));
    }
    @Override public TagKey<Block> getTag() {
        return BlockTags.NEEDS_DIAMOND_TOOL;
    }
}
