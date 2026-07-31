package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeState;

import java.util.Objects;
import java.util.UUID;

/**
 * One jar payload. Survival tokens are unique and replay-protected; the
 * creative template is deterministic and explicitly marked.
 */
public record NodeJarData(
        UUID payloadId,
        Origin origin,
        AuraNodeState node
) {
    public NodeJarData {
        payloadId = Objects.requireNonNull(payloadId, "payloadId");
        origin = Objects.requireNonNull(origin, "origin");
        node = Objects.requireNonNull(node, "node").copy();
    }

    @Override
    public AuraNodeState node() {
        return node.copy();
    }

    public enum Origin {
        SURVIVAL,
        CREATIVE_TEMPLATE
    }
}
