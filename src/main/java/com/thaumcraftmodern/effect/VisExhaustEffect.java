package com.thaumcraftmodern.effect;

import com.thaumcraftmodern.api.wand.VisDiscountEffect;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.VisExhaustClientExtensions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

/**
 * TC4 Flux Flu / Flux Phage vis-cost penalty.
 */
public class VisExhaustEffect extends MobEffect
        implements VisDiscountEffect {
    public static final int PENALTY_PERCENT_PER_LEVEL = 10;

    private final String descriptionId;

    public VisExhaustEffect(int color, String descriptionId) {
        super(MobEffectCategory.HARMFUL, color);
        this.descriptionId = descriptionId;
    }

    @Override
    public String getDescriptionId() {
        return descriptionId;
    }

    @Override
    public void initializeClient(
            Consumer<IClientMobEffectExtensions> consumer
    ) {
        consumer.accept(VisExhaustClientExtensions.INSTANCE);
    }

    @Override
    public int visDiscountPercent(
            MobEffectInstance instance,
            Player player,
            PrimalAspect aspect
    ) {
        return -Math.multiplyExact(
                instance.getAmplifier() + 1,
                PENALTY_PERCENT_PER_LEVEL
        );
    }
}
