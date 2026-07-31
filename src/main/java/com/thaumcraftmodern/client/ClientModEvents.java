package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.render.ClassicWandRenderCalibration;
import com.thaumcraftmodern.client.render.ClientNodeRenderers;
import com.thaumcraftmodern.client.render.ResearchTableBlockEntityRenderer;
import com.thaumcraftmodern.client.render.CrucibleBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ResearchTableModel;
import com.thaumcraftmodern.client.render.ReloadSafeObjLoader;
import com.thaumcraftmodern.client.render.EldritchAltarPartRenderer;
import com.thaumcraftmodern.client.render.EtherealBloomBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaJarBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaTubeBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ArcaneAlembicBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EtherealBloomCrystalModel;
import com.thaumcraftmodern.client.screen.ArcaneWorkbenchScreen;
import com.thaumcraftmodern.client.screen.AlchemicalFurnaceScreen;
import com.thaumcraftmodern.client.screen.ResearchTableScreen;
import com.thaumcraftmodern.client.screen.PechScreen;
import com.thaumcraftmodern.item.AspectShardItem;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.client.particle.NodeBurstParticle;
import com.thaumcraftmodern.client.particle.EldritchHealParticle;
import com.thaumcraftmodern.client.particle.CrucibleBubbleParticle;
import com.thaumcraftmodern.client.particle.TubeVentParticle;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
            MenuScreens.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
            MenuScreens.register(ModMenus.PECH.get(), PechScreen::new);
            MenuScreens.register(
                    ModMenus.ALCHEMICAL_FURNACE.get(),
                    AlchemicalFurnaceScreen::new
            );
        });
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(
            ModelEvent.RegisterGeometryLoaders event
    ) {
        event.register("reload_safe_obj", ReloadSafeObjLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> stack.getItem() instanceof AspectShardItem shard
                        ? 0xFF000000 | shard.color()
                        : 0xFFFFFFFF,
                ModItems.AIR_SHARD.get(),
                ModItems.FIRE_SHARD.get(),
                ModItems.WATER_SHARD.get(),
                ModItems.EARTH_SHARD.get(),
                ModItems.ORDER_SHARD.get(),
                ModItems.ENTROPY_SHARD.get()
        );
        event.register(
                (stack, tintIndex) -> 0xFF000000
                        | EtherealEssenceItem.color(stack),
                ModItems.ETHEREAL_ESSENCE.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? 0xFF000000 | EssentiaPhialItem.color(stack)
                        : 0xFFFFFFFF,
                ModItems.ESSENTIA_PHIAL.get()
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(
            RegisterColorHandlersEvent.Block event
    ) {
        event.register(
                (state, level, position, tintIndex) ->
                        level != null && position != null
                                ? BiomeColors.getAverageWaterColor(
                                        level,
                                        position
                                )
                                : 0x3F76E4,
                ModBlocks.CRUCIBLE.get()
        );
        event.register(
                (state, level, position, tintIndex) -> {
                    if (tintIndex != 0 || level == null || position == null
                            || !(level.getBlockEntity(position)
                            instanceof EssentiaTubeBlockEntity tube)
                            || tube.filter() == null) {
                        return 0xFFFFFF;
                    }
                    return AspectRegistryRuntime.find(tube.filter())
                            .map(AspectDefinition::color).orElse(0xFFFFFF);
                },
                ModBlocks.FILTERED_ESSENTIA_TUBE.get()
        );
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        ClientNodeRenderers.register(
                event,
                ModBlockEntities.AURA_NODE.get(),
                ModBlockEntities.JARRED_AURA_NODE.get(),
                ModItems.THAUMOMETER.get(),
                ModItems.GOGGLES_OF_REVEALING.get()
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.RESEARCH_TABLE.get(),
                ResearchTableBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRUCIBLE.get(),
                CrucibleBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_JAR.get(),
                EssentiaJarBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_TUBE.get(),
                EssentiaTubeBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ARCANE_ALEMBIC.get(),
                ArcaneAlembicBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ELDRITCH_ALTAR_PART.get(),
                EldritchAltarPartRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ETHEREAL_BLOOM.get(),
                EtherealBloomBlockEntityRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {
        event.registerLayerDefinition(
                ResearchTableBlockEntityRenderer.LAYER,
                ResearchTableModel::createBodyLayer
        );
        event.registerLayerDefinition(
                EtherealBloomCrystalModel.LAYER,
                EtherealBloomCrystalModel::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "wand_vis",
                ClientWandVisOverlay::render
        );
        event.registerAboveAll("thaumometer_view", ClientThaumometerOverlay::render);
        event.registerAboveAll(
                "goggles_node_aspects",
                ClientGogglesNodeOverlay::render
        );
        event.registerAboveAll(
                "scan_notifications",
                ClientScanOverlay::renderNotification
        );
        event.registerAboveAll("warp", ClientWarpOverlay::render);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticles.NODE_BURST.get(),
                NodeBurstParticle.Provider::new
        );
        event.registerSpriteSet(
                ModParticles.ELDRITCH_HEAL.get(),
                EldritchHealParticle.Provider::new
        );
        event.registerSpriteSet(
                ModParticles.CRUCIBLE_BUBBLE.get(),
                sprites -> new CrucibleBubbleParticle.Provider(
                        sprites,
                        false
                )
        );
        event.registerSpriteSet(
                ModParticles.CRUCIBLE_FROTH.get(),
                sprites -> new CrucibleBubbleParticle.Provider(
                        sprites,
                        true
                )
        );
        event.registerSpriteSet(
                ModParticles.TUBE_VENT.get(),
                TubeVentParticle.Provider::new
        );
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(
            RegisterClientReloadListenersEvent event
    ) {
        ThaumometerHudLayout.registerReloadListener(event);
        ClassicWandRenderCalibration.registerReloadListener(event);
    }
}
