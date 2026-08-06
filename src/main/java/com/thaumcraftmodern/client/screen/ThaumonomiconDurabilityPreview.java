package com.thaumcraftmodern.client.screen;

import net.minecraft.world.item.ItemStack;

/** TC4 wildcard-durability animation used by recipe-book item previews. */
final class ThaumonomiconDurabilityPreview {
    /** One shared visual phase keeps items with different maxima in lockstep. */
    static final long CYCLE_MILLIS = 4_410L;

    private ThaumonomiconDurabilityPreview() {
    }

    static ItemStack atTime(ItemStack source, long timeMillis) {
        if (source.isEmpty() || !source.isDamageableItem()) return source;
        ItemStack displayed = source.copy();
        displayed.setDamageValue(damageAtTime(timeMillis, source.getMaxDamage()));
        return displayed;
    }

    static int damageAtTime(long timeMillis, int maxDamage) {
        if (maxDamage <= 0) return 0;
        long phase = Math.floorMod(timeMillis, CYCLE_MILLIS);
        return Math.min(maxDamage,
                (int) (phase * ((long) maxDamage + 1L) / CYCLE_MILLIS));
    }
}
