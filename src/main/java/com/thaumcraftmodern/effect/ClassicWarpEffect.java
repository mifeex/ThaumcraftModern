package com.thaumcraftmodern.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Stateful TC4 warp ailments. Purely visual ailments are still represented as
 * real synchronized effects; their screen treatment is handled client-side.
 */
public final class ClassicWarpEffect extends MobEffect {
    public enum Behavior {
        UNNATURAL_HUNGER,
        WARP_WARD,
        DEATH_GAZE,
        BLURRED_VISION,
        SUN_SCORNED,
        THAUMARHIA
    }

    private final String descriptionId;
    private final Behavior behavior;

    public ClassicWarpEffect(
            MobEffectCategory category,
            int color,
            String descriptionId,
            Behavior behavior
    ) {
        super(category, color);
        this.descriptionId = descriptionId;
        this.behavior = behavior;
    }

    @Override
    public String getDescriptionId() {
        return descriptionId;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return switch (behavior) {
            case UNNATURAL_HUNGER, SUN_SCORNED ->
                    duration % Math.max(1, 40 >> amplifier) == 0;
            case THAUMARHIA -> duration % 20 == 0;
            case DEATH_GAZE -> duration % Math.max(1, 60 >> amplifier) == 0;
            default -> false;
        };
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (behavior == Behavior.UNNATURAL_HUNGER && entity instanceof Player player) {
            player.getFoodData().setFoodLevel(
                    Math.max(0, player.getFoodData().getFoodLevel() - 1)
            );
        } else if (behavior == Behavior.SUN_SCORNED
                && entity.level() instanceof ServerLevel level) {
            BlockPos position = entity.blockPosition();
            if (level.isDay()
                    && level.canSeeSky(position)
                    && level.getMaxLocalRawBrightness(position) > 10) {
                entity.setSecondsOnFire(8);
            }
        } else if (behavior == Behavior.DEATH_GAZE
                && entity.getMobType() == net.minecraft.world.entity.MobType.UNDEAD
                && entity.getHealth() <= 1.0F) {
            entity.hurt(entity.damageSources().magic(), 20.0F);
        } else if (behavior == Behavior.THAUMARHIA
                && entity.level() instanceof ServerLevel level
                && entity.getRandom().nextInt(15) == 0) {
            BlockPos position = entity.blockPosition();
            if (level.isEmptyBlock(position)) {
                level.setBlock(
                        position,
                        com.thaumcraftmodern.registry.ModBlocks.FLUX_GOO
                                .get()
                                .defaultBlockState(),
                        3
                );
            }
        }
    }
}
