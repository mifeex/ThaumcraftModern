package com.thaumcraftmodern.warp;

import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Classic item-warp contribution used only while the relevant stack is held
 * or worn. Entries are added here as their legacy items become playable.
 */
public final class WarpGearService {
    private WarpGearService() {
    }

    public static int equippedWarp(Player player) {
        int warp = warp(player.getMainHandItem()) + warp(player.getOffhandItem());
        for (ItemStack armor : player.getArmorSlots()) {
            warp += warp(armor);
        }
        return warp;
    }

    public static int warp(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(ModItems.PRIMAL_STAFF_ROD.get())) {
            return 1;
        }
        if (stack.is(ModItems.BOTTLED_TAINT.get())
                || stack.is(ModItems.LIQUID_DEATH_BUCKET.get())) {
            return 1;
        }
        return stack.hasTag()
                ? Math.max(0, stack.getTag().getInt("ThaumcraftWarp"))
                : 0;
    }
}
