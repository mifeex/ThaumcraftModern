package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TC4's Vishroom applies ten seconds of level-one nausea whenever a living
 * entity touches it.
 */
public final class VishroomBlock extends FlowerBlock {
    static final int NAUSEA_DURATION_TICKS = 200;
    static final int NAUSEA_AMPLIFIER = 0;

    public VishroomBlock(
            MobEffect suspiciousStewEffect,
            int effectDuration,
            Properties properties
    ) {
        super(suspiciousStewEffect, effectDuration, properties);
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION,
                    NAUSEA_DURATION_TICKS,
                    NAUSEA_AMPLIFIER
            ));
        }
        super.entityInside(state, level, position, entity);
    }
}
