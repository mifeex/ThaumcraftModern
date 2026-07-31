package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Collision infection shared by all modern representations of TC4 fibrous
 * taint metadata.
 */
final class TaintExposure {
    private TaintExposure() {
    }

    static void touch(Level level, Entity entity) {
        if (level.isClientSide
                || !(entity instanceof LivingEntity living)
                || living.getMobType()
                        == net.minecraft.world.entity.MobType.UNDEAD) {
            return;
        }
        boolean player = living instanceof Player;
        int chance = player ? 1000 : 500;
        if (level.random.nextInt(chance) != 0) {
            return;
        }
        living.addEffect(new MobEffectInstance(
                ModEffects.FLUX_TAINT.get(),
                player ? 80 : 160,
                0,
                false,
                true
        ));
    }
}
