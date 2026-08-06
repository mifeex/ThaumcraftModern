package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Builds runtime recipe scans before the ordinary registry/knowledge sync. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class RuntimeRecipeScanEvents {
    private RuntimeRecipeScanEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void datapackSync(OnDatapackSyncEvent event) {
        RuntimeRecipeScanGenerator.rebuildIfNeeded(
                event.getPlayerList().getServer());
    }
}
