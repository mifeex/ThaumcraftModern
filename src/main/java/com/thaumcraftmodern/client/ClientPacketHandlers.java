package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.screen.ThaumonomiconScreen;
import com.thaumcraftmodern.knowledge.KnowledgeCapabilities;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.packet.KnowledgeSyncPacket;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.network.packet.ThaumatoriumEssentiaSyncPacket;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.network.packet.WispZapPacket;
import com.thaumcraftmodern.client.render.ClientWispZapRenderer;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import net.minecraft.client.Minecraft;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleKnowledgeSync(KnowledgeSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ResearchDiagnostics.log(
                "CLIENT_SYNC_RECEIVE",
                "player={} rawKnowledge={}",
                minecraft.player == null
                        ? "<no-player>"
                        : minecraft.player.getGameProfile().getName(),
                packet.knowledge()
        );
        if (minecraft.player != null) {
            minecraft.player.getCapability(KnowledgeCapabilities.PLAYER)
                    .ifPresent(knowledge -> knowledge.deserializeNBT(packet.knowledge()));
        }
        AspectRegistryRuntime.replace(AspectRegistryRuntime.deserialize(packet.aspects()));
        ResearchRegistry.replace(ResearchRegistry.deserialize(packet.research()));
        ResearchCategoryRegistry.replace(
                ResearchCategoryRegistry.deserialize(packet.researchCategories())
        );
        ScanRegistry.replace(ScanRegistry.deserialize(packet.scans()));
        WandComponentRegistry.replace(
                WandComponentRegistry.deserialize(packet.wands())
        );
        if (minecraft.player != null) {
                    minecraft.player.getCapability(KnowledgeCapabilities.PLAYER)
                    .ifPresent(knowledge -> ResearchDiagnostics.log(
                            "CLIENT_SYNC_APPLIED",
                            "player={} amounts={} knownAspects={} revealedResearch={} completedResearch={} warp={}/{}/{} criteria={} aspectDefinitions={} researchDefinitions={} categories={}",
                            minecraft.player.getGameProfile().getName(),
                            knowledge.aspectAmounts(),
                            knowledge.knownAspects(),
                            knowledge.revealedResearch(),
                            knowledge.completedResearch(),
                            knowledge.warp(WarpType.PERMANENT),
                            knowledge.warp(WarpType.NORMAL),
                            knowledge.warp(WarpType.TEMPORARY),
                            knowledge.researchCriteria(),
                            AspectRegistryRuntime.catalog().definitions().size(),
                            ResearchRegistry.all().size(),
                            ResearchCategoryRegistry.all().size()
                    ));
        }
    }

    public static void handleScanFeedback(ScanFeedbackPacket packet) {
        ClientThaumometerResultState.accept(packet);
        ClientScanOverlay.show(packet);
    }

    public static void handleWarpFeedback(WarpFeedbackPacket packet) {
        ClientWarpOverlay.accept(packet);
    }

    public static void handleWispZap(WispZapPacket packet) {
        ClientWispZapRenderer.accept(packet);
    }

    public static void handleThaumatoriumEssentia(
            ThaumatoriumEssentiaSyncPacket packet
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null
                && minecraft.level.getBlockEntity(packet.position())
                instanceof ThaumatoriumBlockEntity machine) {
            machine.applyClientEssentiaSnapshot(packet.essentia());
        }
    }

    public static void openThaumonomicon() {
        ResearchDiagnostics.log(
                "CLIENT_THAUMONOMICON_OPEN_PACKET",
                "opening screen"
        );
        Minecraft.getInstance().setScreen(new ThaumonomiconScreen());
    }
}
