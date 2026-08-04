package com.thaumcraftmodern.data;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class DefinitionEvents {
    private DefinitionEvents() {
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AspectReloadListener());
        event.addListener(new CrucibleRecipeReloadListener());
        event.addListener(new InfusionRecipeReloadListener());
        event.addListener(new ScanReloadListener());
        event.addListener(new ResearchCategoryReloadListener());
        event.addListener(new ResearchReloadListener());
        event.addListener(new WandDefinitionReloadListener());
        event.addListener(new ConstructionReloadListener());
        event.addListener(new EssentiaTransportReloadListener());
    }
}
