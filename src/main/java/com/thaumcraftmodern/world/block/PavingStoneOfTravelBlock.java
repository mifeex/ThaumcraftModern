package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Dedicated port of TC4 BlockCosmeticSolid metadata 2. */
public final class PavingStoneOfTravelBlock extends Block {
    public static final int EFFECT_TICKS = 40;
    public static final int SPEED_AMPLIFIER = 1;
    public static final int JUMP_AMPLIFIER = 0;
    public static final int SPARKLE_COUNT = 5;

    public PavingStoneOfTravelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(
            Level level,
            BlockPos position,
            BlockState state,
            Entity entity
    ) {
        if (entity instanceof LivingEntity living) {
            if (level.isClientSide) {
                spawnSparkles(level, position);
            } else {
                living.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        EFFECT_TICKS,
                        SPEED_AMPLIFIER,
                        false,
                        false
                ));
                living.addEffect(new MobEffectInstance(
                        MobEffects.JUMP,
                        EFFECT_TICKS,
                        JUMP_AMPLIFIER,
                        false,
                        false
                ));
            }
        }
        super.stepOn(level, position, state, entity);
    }

    private static void spawnSparkles(Level level, BlockPos position) {
        for (int index = 0; index < SPARKLE_COUNT; index++) {
            level.addParticle(
                    ModParticles.TRAVEL_SPARKLE.get(),
                    position.getX() - 0.1D + level.random.nextDouble() * 1.2D,
                    position.getY() - 0.1D + level.random.nextDouble() * 1.2D,
                    position.getZ() - 0.1D + level.random.nextDouble() * 1.2D,
                    0.0D,
                    level.random.nextDouble() * 0.02D,
                    0.0D
            );
        }
    }
}
