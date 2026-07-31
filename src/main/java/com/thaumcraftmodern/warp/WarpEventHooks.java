package com.thaumcraftmodern.warp;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class WarpEventHooks {
    private WarpEventHooks() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 10 == 0
                && player.hasEffect(ModEffects.DEATH_GAZE.get())) {
            WarpEvents.checkDeathGaze(player);
        }
        if (player.tickCount > 0
                && player.tickCount % WarpEvents.CHECK_INTERVAL_TICKS == 0
                && !player.hasEffect(ModEffects.WARP_WARD.get())) {
            WarpEvents.check(player);
        }
    }
}
