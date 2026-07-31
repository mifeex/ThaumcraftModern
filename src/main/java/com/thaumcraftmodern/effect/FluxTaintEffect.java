package com.thaumcraftmodern.effect;

import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * TC4 Flux Taint: one point of magic damage every 40 ticks. Tainted
 * creatures are sustained by it instead.
 */
public final class FluxTaintEffect extends MobEffect {
    public FluxTaintEffect() {
        super(MobEffectCategory.HARMFUL, 0x663366);
    }

    @Override
    public String getDescriptionId() {
        return "potion.fluxtaint";
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof LegacyThaumcraftMob mob
                && mob.kind().tainted()) {
            entity.heal(1.0F);
            return;
        }
        if (entity.getMobType()
                != net.minecraft.world.entity.MobType.UNDEAD) {
            entity.hurt(entity.damageSources().magic(), 1.0F);
        }
    }
}
