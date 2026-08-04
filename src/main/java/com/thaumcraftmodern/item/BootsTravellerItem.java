package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BootsTravellerItem extends ArmorItem {
    public BootsTravellerItem(Properties properties) {
        super(TravellerArmorMaterial.INSTANCE, Type.BOOTS, properties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().flying && player.zza > 0.0F) {
            if (player.onGround()) {
                float bonus = player.isInWater() ? 0.055F / 4.0F : 0.055F;
                player.moveRelative(bonus, new net.minecraft.world.phys.Vec3(0.0D, 0.0D, 1.0D));
            } else {
                player.moveRelative(0.05F, new net.minecraft.world.phys.Vec3(0.0D, 0.0D, 1.0D));
            }
        }
        if (player.fallDistance > 0.0F) player.fallDistance = Math.max(0.0F, player.fallDistance - 0.25F);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID + ":textures/models/bootstraveler.png";
    }
}
