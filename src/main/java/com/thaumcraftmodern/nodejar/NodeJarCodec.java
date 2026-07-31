package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Single versioned schema used by captured, placed and creative jar stacks.
 */
public final class NodeJarCodec {
    public static final int SERIAL_VERSION = 1;
    public static final String ITEM_TAG_KEY = "ThaumcraftModernNodeJar";

    private static final String VERSION_KEY = "version";
    private static final String PAYLOAD_ID_KEY = "payload_id";
    private static final String ORIGIN_KEY = "origin";
    private static final String NODE_KEY = "node";

    private NodeJarCodec() {
    }

    public static CompoundTag encode(NodeJarData data) {
        Objects.requireNonNull(data, "data");
        CompoundTag result = new CompoundTag();
        result.putInt(VERSION_KEY, SERIAL_VERSION);
        result.putUUID(PAYLOAD_ID_KEY, data.payloadId());
        result.putString(ORIGIN_KEY, data.origin().name());
        result.put(NODE_KEY, AuraNodeCodec.encode(data.node()));
        return result;
    }

    public static NodeJarData decode(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        int version = tag.getInt(VERSION_KEY);
        if (version != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported node jar version " + version
                            + "; expected " + SERIAL_VERSION
            );
        }
        if (!tag.hasUUID(PAYLOAD_ID_KEY)) {
            throw new IllegalArgumentException("node jar is missing payload_id");
        }
        NodeJarData.Origin origin;
        try {
            origin = NodeJarData.Origin.valueOf(tag.getString(ORIGIN_KEY));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "invalid node jar origin: " + tag.getString(ORIGIN_KEY),
                    exception
            );
        }
        return new NodeJarData(
                tag.getUUID(PAYLOAD_ID_KEY),
                origin,
                AuraNodeCodec.decode(tag.getCompound(NODE_KEY))
        );
    }

    public static void write(ItemStack stack, NodeJarData data) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("cannot write node jar data to an empty stack");
        }
        stack.getOrCreateTag().put(ITEM_TAG_KEY, encode(data));
    }

    public static Optional<NodeJarData> read(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ITEM_TAG_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(decode(root.getCompound(ITEM_TAG_KEY)));
    }
}
