package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Non-living entities such as boats, minecarts, paintings and projectiles do
 * not call Item#interactLivingEntity. Intercepting Forge's entity interaction
 * events makes those targets behave exactly like animals while a Thaumometer
 * is held.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class ThaumometerInteractionEvents {
    /*
     * SUCCESS/sidedSuccess asks the client to play the ordinary interaction
     * swing. Entity scans use the held-use lifecycle just like block scans, so
     * consume the click without starting that additional vanilla animation.
     */
    static final InteractionResult ENTITY_SCAN_RESULT = InteractionResult.CONSUME;

    private ThaumometerInteractionEvents() {
    }

    @SubscribeEvent
    public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        start(event, event.getTarget());
    }

    @SubscribeEvent
    public static void interactEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
        start(event, event.getTarget());
    }

    private static void start(PlayerInteractEvent event, Entity target) {
        if (!(event.getItemStack().getItem() instanceof ThaumometerItem)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(ENTITY_SCAN_RESULT);
        ThaumometerItem.tryStartEntityTarget(event.getEntity(), event.getHand(), target);
    }
}
