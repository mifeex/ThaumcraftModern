package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Hybrid protection between thaumaturge robes and thaumium fortress armor. */
public enum WingedMantleArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 13 * 32;
            case LEGGINGS -> 15 * 32;
            case CHESTPLATE -> 16 * 32;
            case HELMET -> 11 * 32;
        };
    }

    @Override public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS, HELMET -> 2;
            case LEGGINGS -> 5;
            case CHESTPLATE -> 6;
        };
    }

    @Override public int getEnchantmentValue() { return 25; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
    @Override public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/thaumium")));
    }
    @Override public String getName() { return ThaumcraftModern.MOD_ID + ":winged_mantle"; }
    @Override public float getToughness() { return 1.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
