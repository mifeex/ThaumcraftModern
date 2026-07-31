package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.effect.VisExhaustEffect;
import com.thaumcraftmodern.item.GogglesOfRevealingItem;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisDiscountServiceTest {
    @Test
    void classicGogglesProvideFivePercent() {
        assertEquals(5, GogglesOfRevealingItem.VIS_DISCOUNT_PERCENT);
    }

    @Test
    void fluxFluAddsTenPercentCostPerEffectLevel() {
        VisExhaustEffect effect =
                new VisExhaustEffect(0x8888FF, "potion.visexhaust");

        assertEquals(
                -30,
                effect.visDiscountPercent(
                        new MobEffectInstance(effect, 200, 2),
                        null,
                        PrimalAspect.AER
                )
        );
    }
}
