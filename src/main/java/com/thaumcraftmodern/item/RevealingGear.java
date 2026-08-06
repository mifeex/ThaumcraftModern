package com.thaumcraftmodern.item;

import net.minecraft.world.item.ItemStack;

/** Common server/client predicate for TC4 node- and essentia-revealing headgear. */
public interface RevealingGear {
    boolean reveals(ItemStack stack);

    static boolean equipped(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RevealingGear gear
                && gear.reveals(stack);
    }
}
