package com.thaumcraftmodern.knowledge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class KnowledgeCapabilities {
    public static final Capability<PlayerThaumKnowledge> PLAYER =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private KnowledgeCapabilities() {
    }
}

