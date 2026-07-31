package com.thaumcraftmodern.knowledge;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.KnowledgeSyncPacket;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import net.minecraft.server.level.ServerPlayer;

public final class KnowledgeSync {
    private KnowledgeSync() {
    }

    public static void send(ServerPlayer player) {
        send(player, "unspecified");
    }

    public static void send(ServerPlayer player, String reason) {
        player.getCapability(KnowledgeCapabilities.PLAYER).ifPresent(knowledge -> {
            ResearchProgressService.Update progress =
                    ResearchProgressService.reconcile(knowledge);
            if (progress.changed()) {
                ResearchDiagnostics.log(
                        "SERVER_RESEARCH_PROGRESS",
                        "player={} reason={} revealed={} autoCompleted={}",
                        player.getGameProfile().getName(),
                        reason,
                        progress.revealed(),
                        progress.autoCompleted()
                );
            }
            ResearchDiagnostics.log(
                    "SERVER_SYNC_SEND",
                    "player={} reason={} amounts={} knownAspects={} scans={} revealedResearch={} completedResearch={} warp={}/{}/{} criteria={}",
                    player.getGameProfile().getName(),
                    reason,
                    knowledge.aspectAmounts(),
                    knowledge.knownAspects(),
                    knowledge.scans().size(),
                    knowledge.revealedResearch(),
                    knowledge.completedResearch(),
                    knowledge.warp(WarpType.PERMANENT),
                    knowledge.warp(WarpType.NORMAL),
                    knowledge.warp(WarpType.TEMPORARY),
                    knowledge.researchCriteria()
            );

            ModNetwork.sendTo(player, new KnowledgeSyncPacket(
                    knowledge.serializeNBT(),
                    AspectRegistryRuntime.serialize(),
                    ResearchRegistry.serialize(),
                    ResearchCategoryRegistry.serialize(),
                    ScanRegistry.serialize(),
                    WandComponentRegistry.serialize()
            ));
        });
    }

    static boolean applyAutomaticResearchUnlocks(PlayerThaumKnowledge knowledge) {
        return !ResearchProgressService.reconcile(knowledge).autoCompleted().isEmpty();
    }
}
