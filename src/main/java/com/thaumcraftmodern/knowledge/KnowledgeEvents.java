package com.thaumcraftmodern.knowledge;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.scan.ScanSessionManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
final class CapabilityRegistration {
    private CapabilityRegistration() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerThaumKnowledge.class);
    }
}

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class KnowledgeEvents {
    private static final ResourceLocation KEY =
            new ResourceLocation(ThaumcraftModern.MOD_ID, "player_knowledge");

    private KnowledgeEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            PlayerKnowledgeProvider provider = new PlayerKnowledgeProvider();
            event.addCapability(KEY, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(KnowledgeCapabilities.PLAYER).ifPresent(oldKnowledge ->
                event.getEntity().getCapability(KnowledgeCapabilities.PLAYER).ifPresent(newKnowledge ->
                        newKnowledge.copyFrom(oldKnowledge)
                )
        );
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KnowledgeSync.send(player);
        }
    }

    @SubscribeEvent
    public static void playerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KnowledgeSync.send(player);
        }
    }

    @SubscribeEvent
    public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KnowledgeSync.send(player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ScanSessionManager.cancel(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void datapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            KnowledgeSync.send(event.getPlayer());
        } else {
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                KnowledgeSync.send(player);
            }
        }
    }

    @SubscribeEvent
    public static void advancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String id = event.getAdvancement().getId().toString();
            ResearchProgressService.recordCriterion(
                    player,
                    "advancement:" + id,
                    "advancement:" + id
            );
        }
    }

    @SubscribeEvent
    public static void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !event.getCrafting().isEmpty()) {
            String id = BuiltInRegistries.ITEM.getKey(
                    event.getCrafting().getItem()
            ).toString();
            ResearchProgressService.recordCriterion(
                    player,
                    "crafted:" + id,
                    "crafted:" + id
            );
        }
    }

    @SubscribeEvent
    public static void itemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !event.getSmelting().isEmpty()) {
            String id = BuiltInRegistries.ITEM.getKey(
                    event.getSmelting().getItem()
            ).toString();
            ResearchProgressService.recordCriterion(
                    player,
                    "smelted:" + id,
                    "smelted:" + id
            );
        }
    }
}
