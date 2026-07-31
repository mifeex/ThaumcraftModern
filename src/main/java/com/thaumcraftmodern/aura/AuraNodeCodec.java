package com.thaumcraftmodern.aura;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Versioned NBT codec shared by world nodes and jarred nodes.
 */
public final class AuraNodeCodec {
    public static final int SERIAL_VERSION = 2;

    private static final String VERSION_KEY = "version";
    private static final String NODE_ID_KEY = "node_id";
    private static final String TYPE_KEY = "type";
    private static final String MODIFIER_KEY = "modifier";
    private static final String REVISION_KEY = "revision";
    private static final String VIS_KEY = "vis";
    private static final String ASPECT_KEY = "aspect";
    private static final String CURRENT_KEY = "current";
    private static final String MAXIMUM_KEY = "maximum";

    private AuraNodeCodec() {
    }

    public static CompoundTag encode(AuraNodeState state) {
        Objects.requireNonNull(state, "state");
        AuraNodeState.Snapshot snapshot = state.snapshot();
        CompoundTag result = new CompoundTag();
        result.putInt(VERSION_KEY, SERIAL_VERSION);
        result.putUUID(NODE_ID_KEY, snapshot.nodeId());
        result.putString(TYPE_KEY, snapshot.type().name());
        result.putString(MODIFIER_KEY, snapshot.modifier().name());
        result.putLong(REVISION_KEY, snapshot.revision());

        ListTag vis = new ListTag();
        for (String aspect : snapshot.aspectsCurrent().keySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ASPECT_KEY, aspect);
            entry.putInt(CURRENT_KEY, snapshot.aspectsCurrent().get(aspect));
            entry.putInt(MAXIMUM_KEY, snapshot.aspectsMaximum().get(aspect));
            vis.add(entry);
        }
        result.put(VIS_KEY, vis);
        return result;
    }

    public static AuraNodeState decode(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        int version = tag.getInt(VERSION_KEY);
        if (version != 1 && version != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported aura node version " + version
                            + "; expected 1 or " + SERIAL_VERSION
            );
        }
        if (!tag.hasUUID(NODE_ID_KEY)) {
            throw new IllegalArgumentException("aura node is missing node_id");
        }

        AuraNodeType type = parseEnum(
                AuraNodeType.class,
                tag.getString(TYPE_KEY),
                TYPE_KEY
        );
        AuraNodeModifier modifier = parseEnum(
                AuraNodeModifier.class,
                tag.getString(MODIFIER_KEY),
                MODIFIER_KEY
        );
        long revision = tag.getLong(REVISION_KEY);
        if (revision < 0L) {
            throw new IllegalArgumentException("aura node revision cannot be negative");
        }

        Map<String, Integer> current = new LinkedHashMap<>();
        Map<String, Integer> maximum = new LinkedHashMap<>();
        ListTag entries = tag.getList(VIS_KEY, Tag.TAG_COMPOUND);
        for (Tag rawEntry : entries) {
            CompoundTag entry = (CompoundTag) rawEntry;
            String aspect = entry.getString(ASPECT_KEY);
            if (version == 1) {
                PrimalAspect.fromId(aspect);
            }
            if (current.put(aspect, entry.getInt(CURRENT_KEY)) != null) {
                throw new IllegalArgumentException(
                        "duplicate aspect in aura node: " + aspect
                );
            }
            maximum.put(aspect, entry.getInt(MAXIMUM_KEY));
        }

        return AuraNodeState.withAspects(
                tag.getUUID(NODE_ID_KEY),
                type,
                modifier,
                current,
                maximum,
                revision
        );
    }

    /**
     * Safe world-load path. Corrupt or future data is never partially applied;
     * the supplied normal-node fallback is returned with a diagnostic.
     */
    public static DecodeResult decodeOrRecover(
            CompoundTag tag,
            Supplier<AuraNodeState> fallback
    ) {
        Objects.requireNonNull(fallback, "fallback");
        try {
            return new DecodeResult(decode(tag), false, "");
        } catch (RuntimeException exception) {
            AuraNodeState recovered = Objects.requireNonNull(
                    fallback.get(),
                    "fallback returned null"
            );
            return new DecodeResult(
                    recovered,
                    true,
                    exception.getClass().getSimpleName() + ": " + exception.getMessage()
            );
        }
    }

    private static <E extends Enum<E>> E parseEnum(
            Class<E> type,
            String raw,
            String field
    ) {
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "invalid aura node " + field + ": " + raw,
                    exception
            );
        }
    }

    public record DecodeResult(
            AuraNodeState state,
            boolean recovered,
            String diagnostic
    ) {
        public DecodeResult {
            state = Objects.requireNonNull(state, "state");
            diagnostic = Objects.requireNonNull(diagnostic, "diagnostic");
        }
    }
}
