package com.thaumcraftmodern.api.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Active-effect contribution to a player's signed vis discount.
 */
public interface VisDiscountEffect {
    int visDiscountPercent(
            MobEffectInstance instance,
            Player player,
            PrimalAspect aspect
    );
}
