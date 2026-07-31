package com.thaumcraftmodern.client;

import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;

import java.util.Optional;

/**
 * Client-side view of the optional server-authored node payload carried by
 * the latest Thaumometer result.
 *
 * <p>This is a narrow handoff contract for the independently developed
 * Thaumometer visual. It does not derive or mutate node data client-side.</p>
 */
public final class ClientThaumometerResultState {
    private static Optional<ScanFeedbackPacket.NodeData> latestNode = Optional.empty();

    private ClientThaumometerResultState() {
    }

    public static synchronized void accept(ScanFeedbackPacket packet) {
        latestNode = packet.success() ? packet.node() : Optional.empty();
    }

    public static synchronized Optional<ScanFeedbackPacket.NodeData> latestNode() {
        return latestNode;
    }

    public static synchronized void clear() {
        latestNode = Optional.empty();
    }
}
