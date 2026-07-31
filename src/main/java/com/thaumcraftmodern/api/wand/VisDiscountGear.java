package com.thaumcraftmodern.api.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Equipment-provided vis discount, in signed percentage points.
 *
 * <p>Positive values reduce vis cost and negative values increase it. The
 * value may depend on the primal aspect, matching TC4's discount gear API.</p>
 */
public interface VisDiscountGear {
    int visDiscountPercent(
            ItemStack stack,
            Player player,
            PrimalAspect aspect
    );
}
