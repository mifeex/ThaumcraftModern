package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * TC4 tainted goo/tendril inventory contamination.
 */
public final class TaintedMaterialItem extends Item {
    public TaintedMaterialItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity owner,
            int slot,
            boolean selected
    ) {
        if (level.isClientSide
                || !(owner instanceof LivingEntity living)
                || living.getMobType()
                        == net.minecraft.world.entity.MobType.UNDEAD
                || living.hasEffect(ModEffects.FLUX_TAINT.get())
                || !TaintItemInfectionRules.shouldInfect(
                        level.random.nextInt(TaintItemInfectionRules.ROLL_BOUND),
                        stack.getCount()
                )) {
            return;
        }
        living.addEffect(new MobEffectInstance(
                ModEffects.FLUX_TAINT.get(),
                TaintItemInfectionRules.EFFECT_DURATION_TICKS,
                0
        ));
        if (living instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable(
                            "tc.taint_item_poison",
                            stack.getHoverName()
                    ),
                    true
            );
        }
        stack.shrink(1);
    }
}
