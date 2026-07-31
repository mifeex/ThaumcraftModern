package com.thaumcraftmodern.knowledge;

import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Consumer;

public final class KnowledgeAccess {
    private KnowledgeAccess() {
    }

    public static Optional<PlayerThaumKnowledge> get(Player player) {
        return player.getCapability(KnowledgeCapabilities.PLAYER).resolve();
    }

    public static void mutate(Player player, Consumer<PlayerThaumKnowledge> mutation) {
        player.getCapability(KnowledgeCapabilities.PLAYER).ifPresent(knowledge -> {
            mutation.accept(knowledge);
            if (!player.level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                KnowledgeSync.send(serverPlayer);
            }
        });
    }
}

