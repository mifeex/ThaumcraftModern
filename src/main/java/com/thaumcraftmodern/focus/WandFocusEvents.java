package com.thaumcraftmodern.focus;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Restores Equal Trade's TC4 left-click single-block operation. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class WandFocusEvents {
    private WandFocusEvents() {}

    @SubscribeEvent
    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (WandFocusService.tradeLeftClick(player, event.getPos()) == InteractionResult.CONSUME) {
            event.setCanceled(true);
        }
    }
}
