package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * TC4's SPECIAL armor material as used by the Goggles of Revealing.
 *
 * <p>The original goggles replace the material-derived helmet durability with
 * 350, so that exact value is exposed directly here.</p>
 */
public enum RevealingArmorMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return type == ArmorItem.Type.HELMET ? 350 : 25;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET, BOOTS -> 1;
            case LEGGINGS -> 2;
            case CHESTPLATE -> 3;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.GOLD_INGOT);
    }

    @Override
    public String getName() {
        return ThaumcraftModern.MOD_ID + ":revealing";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
