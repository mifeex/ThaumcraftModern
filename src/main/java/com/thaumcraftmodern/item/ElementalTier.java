package com.thaumcraftmodern.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Exact TC4 elemental material: level 4, 1561 uses, speed 10, damage 4, enchantability 22. */
public enum ElementalTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 1561; }
    @Override public float getSpeed() { return 10.0F; }
    @Override public float getAttackDamageBonus() { return 4.0F; }
    @Override public int getLevel() { return 4; }
    @Override public int getEnchantmentValue() { return 22; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/thaumium")));
    }
    @Override public TagKey<Block> getTag() { return BlockTags.NEEDS_DIAMOND_TOOL; }
}
