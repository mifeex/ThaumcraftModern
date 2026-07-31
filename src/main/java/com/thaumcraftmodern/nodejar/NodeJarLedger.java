package com.thaumcraftmodern.nodejar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistent survival-payload ownership ledger.
 *
 * <p>A payload is either in an item ({@code JARRED}) or placed at one exact
 * world key ({@code PLACED}). A stale copied stack cannot claim a payload that
 * is already placed. Creative templates intentionally bypass this survival
 * ledger.</p>
 */
public final class NodeJarLedger {
    public static final int SERIAL_VERSION = 1;
    private static final String VERSION_KEY = "version";
    private static final String ENTRIES_KEY = "entries";
    private static final String PAYLOAD_KEY = "payload";
    private static final String NODE_KEY = "node";
    private static final String STATUS_KEY = "status";
    private static final String PLACEMENT_KEY = "placement";

    private final LinkedHashMap<UUID, Entry> entries = new LinkedHashMap<>();

    public synchronized boolean registerCaptured(NodeJarData data) {
        Objects.requireNonNull(data, "data");
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        return entries.putIfAbsent(
                data.payloadId(),
                new Entry(data.node().nodeId(), Status.JARRED, "")
        ) == null;
    }

    public synchronized boolean removeCaptured(NodeJarData data) {
        Objects.requireNonNull(data, "data");
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        Entry current = entries.get(data.payloadId());
        if (current == null
                || current.status() != Status.JARRED
                || !current.nodeId().equals(data.node().nodeId())) {
            return false;
        }
        entries.remove(data.payloadId());
        return true;
    }

    public synchronized boolean claimPlacement(NodeJarData data, String placementKey) {
        Objects.requireNonNull(data, "data");
        String key = requirePlacementKey(placementKey);
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        Entry current = entries.get(data.payloadId());
        if (current == null
                || current.status() != Status.JARRED
                || !current.nodeId().equals(data.node().nodeId())) {
            return false;
        }
        entries.put(
                data.payloadId(),
                new Entry(current.nodeId(), Status.PLACED, key)
        );
        return true;
    }

    public synchronized boolean rollbackPlacement(
            NodeJarData data,
            String placementKey
    ) {
        Objects.requireNonNull(data, "data");
        String key = requirePlacementKey(placementKey);
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        Entry current = entries.get(data.payloadId());
        if (current == null
                || current.status() != Status.PLACED
                || !current.nodeId().equals(data.node().nodeId())
                || !current.placementKey().equals(key)) {
            return false;
        }
        entries.put(
                data.payloadId(),
                new Entry(current.nodeId(), Status.JARRED, "")
        );
        return true;
    }

    /**
     * Called by the registered jar block when it is legitimately broken.
     */
    public synchronized boolean returnToJar(
            NodeJarData data,
            String placementKey
    ) {
        return rollbackPlacement(data, placementKey);
    }

    /**
     * Compatibility for jars captured before 1.5.7. Those transactions put a
     * physical jar in the world but accidentally left its ledger entry in the
     * item-owned state. Accept that exact legacy state once so existing worlds
     * can recover the jar instead of silently losing it.
     */
    public synchronized boolean returnToJarOrRecoverLegacyCapture(
            NodeJarData data,
            String placementKey
    ) {
        if (rollbackPlacement(data, placementKey)) {
            return true;
        }
        Objects.requireNonNull(data, "data");
        Entry current = entries.get(data.payloadId());
        return current != null
                && current.status() == Status.JARRED
                && current.nodeId().equals(data.node().nodeId());
    }

    /**
     * Consumes a placed jar token when its node is released back into the
     * world. The payload no longer exists as an item or placed jar afterward.
     */
    public synchronized boolean releasePlacedNode(
            NodeJarData data,
            String placementKey
    ) {
        Objects.requireNonNull(data, "data");
        String key = requirePlacementKey(placementKey);
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        Entry current = entries.get(data.payloadId());
        if (current == null
                || current.status() != Status.PLACED
                || !current.nodeId().equals(data.node().nodeId())
                || !current.placementKey().equals(key)) {
            return false;
        }
        entries.remove(data.payloadId());
        return true;
    }

    public synchronized boolean restoreReleasedNode(
            NodeJarData data,
            String placementKey
    ) {
        Objects.requireNonNull(data, "data");
        String key = requirePlacementKey(placementKey);
        if (data.origin() == NodeJarData.Origin.CREATIVE_TEMPLATE) {
            return true;
        }
        return entries.putIfAbsent(
                data.payloadId(),
                new Entry(data.node().nodeId(), Status.PLACED, key)
        ) == null;
    }

    public synchronized Status status(UUID payloadId) {
        Entry entry = entries.get(payloadId);
        return entry == null ? Status.UNKNOWN : entry.status();
    }

    public synchronized CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        root.putInt(VERSION_KEY, SERIAL_VERSION);
        ListTag serializedEntries = new ListTag();
        for (Map.Entry<UUID, Entry> mapEntry : entries.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(PAYLOAD_KEY, mapEntry.getKey());
            entry.putUUID(NODE_KEY, mapEntry.getValue().nodeId());
            entry.putString(STATUS_KEY, mapEntry.getValue().status().name());
            entry.putString(PLACEMENT_KEY, mapEntry.getValue().placementKey());
            serializedEntries.add(entry);
        }
        root.put(ENTRIES_KEY, serializedEntries);
        return root;
    }

    public static NodeJarLedger deserialize(CompoundTag root) {
        Objects.requireNonNull(root, "root");
        int version = root.getInt(VERSION_KEY);
        if (version != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported node jar ledger version " + version
                            + "; expected " + SERIAL_VERSION
            );
        }
        NodeJarLedger ledger = new NodeJarLedger();
        for (Tag raw : root.getList(ENTRIES_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag serialized = (CompoundTag) raw;
            if (!serialized.hasUUID(PAYLOAD_KEY) || !serialized.hasUUID(NODE_KEY)) {
                throw new IllegalArgumentException("node jar ledger entry is missing UUID");
            }
            Status status;
            try {
                status = Status.valueOf(serialized.getString(STATUS_KEY));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "invalid node jar ledger status: "
                                + serialized.getString(STATUS_KEY),
                        exception
                );
            }
            if (status == Status.UNKNOWN) {
                throw new IllegalArgumentException("UNKNOWN cannot be persisted");
            }
            String placement = serialized.getString(PLACEMENT_KEY);
            if (status == Status.PLACED) {
                requirePlacementKey(placement);
            } else if (!placement.isEmpty()) {
                throw new IllegalArgumentException(
                        "JARRED node jar ledger entry has a placement"
                );
            }
            UUID payloadId = serialized.getUUID(PAYLOAD_KEY);
            Entry previous = ledger.entries.put(
                    payloadId,
                    new Entry(serialized.getUUID(NODE_KEY), status, placement)
            );
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate node jar ledger payload: " + payloadId
                );
            }
        }
        return ledger;
    }

    private static String requirePlacementKey(String value) {
        String key = Objects.requireNonNull(value, "placementKey").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("placementKey cannot be blank");
        }
        return key;
    }

    private record Entry(UUID nodeId, Status status, String placementKey) {
        private Entry {
            nodeId = Objects.requireNonNull(nodeId, "nodeId");
            status = Objects.requireNonNull(status, "status");
            placementKey = Objects.requireNonNull(placementKey, "placementKey");
        }
    }

    public enum Status {
        UNKNOWN,
        JARRED,
        PLACED
    }
}
