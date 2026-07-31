package com.thaumcraftmodern.effect;

import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class InfectiousVisExhaustEffect extends VisExhaustEffect {
    public InfectiousVisExhaustEffect() {
        super(0x4444AA, "potion.infvisexhaust");
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity carrier, int amplifier) {
        if (!(carrier.level() instanceof ServerLevel level)) {
            return;
        }
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                carrier.getBoundingBox().inflate(4.0D),
                candidate -> candidate != carrier
                        && !candidate.hasEffect(ModEffects.INFECTIOUS_VIS_EXHAUST.get())
        )) {
            target.addEffect(new MobEffectInstance(
                    amplifier > 0
                            ? ModEffects.INFECTIOUS_VIS_EXHAUST.get()
                            : ModEffects.VIS_EXHAUST.get(),
                    6000,
                    Math.max(0, amplifier - 1),
                    false,
                    true
            ));
        }
    }
}
