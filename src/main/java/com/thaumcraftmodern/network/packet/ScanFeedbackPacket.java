package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.aura.AuraNodeScanResult;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public record ScanFeedbackPacket(
        boolean success,
        String messageKey,
        String displayKey,
        List<AspectGain> aspects,
        Optional<NodeData> node
) {
    public ScanFeedbackPacket {
        aspects = List.copyOf(aspects);
        node = Objects.requireNonNull(node, "node");
    }

    public ScanFeedbackPacket(
            boolean success,
            String messageKey,
            String displayKey,
            List<AspectGain> aspects
    ) {
        this(success, messageKey, displayKey, aspects, Optional.empty());
    }

    public static void encode(ScanFeedbackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.messageKey);
        buffer.writeUtf(packet.displayKey);
        buffer.writeCollection(packet.aspects, (target, gain) -> {
            target.writeUtf(gain.aspectId());
            target.writeVarInt(gain.amount());
            target.writeVarInt(gain.total());
            target.writeBoolean(gain.newlyDiscovered());
        });
        buffer.writeOptional(packet.node, (target, node) -> {
            target.writeUUID(node.nodeId());
            target.writeUtf(node.type());
            target.writeUtf(node.modifier());
            target.writeVarLong(node.revision());
            target.writeCollection(node.aspects(), (aspectBuffer, aspect) -> {
                aspectBuffer.writeUtf(aspect.aspectId());
                aspectBuffer.writeVarInt(aspect.current());
                aspectBuffer.writeVarInt(aspect.maximum());
            });
        });
    }

    public static ScanFeedbackPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        String messageKey = buffer.readUtf();
        String displayKey = buffer.readUtf();
        List<AspectGain> aspects = buffer.readCollection(
                ArrayList::new,
                source -> new AspectGain(
                        source.readUtf(),
                        source.readVarInt(),
                        source.readVarInt(),
                        source.readBoolean()
                )
        );
        Optional<NodeData> node = buffer.readOptional(source -> new NodeData(
                source.readUUID(),
                source.readUtf(),
                source.readUtf(),
                source.readVarLong(),
                source.readCollection(
                        ArrayList::new,
                        aspectSource -> new NodeAspect(
                                aspectSource.readUtf(),
                                aspectSource.readVarInt(),
                                aspectSource.readVarInt()
                        )
                )
        ));
        return new ScanFeedbackPacket(success, messageKey, displayKey, aspects, node);
    }

    public static void handle(ScanFeedbackPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleScanFeedback(packet));
        context.get().setPacketHandled(true);
    }

    public record AspectGain(String aspectId, int amount, int total, boolean newlyDiscovered) {
        public AspectGain {
            if (aspectId == null || aspectId.isBlank()) {
                throw new IllegalArgumentException("aspectId must be non-blank");
            }
            if (amount <= 0 || total < 0) {
                throw new IllegalArgumentException("aspect amount must be positive and total non-negative");
            }
        }
    }

    /**
     * Full synchronized parameters of the scanned node. The current visual
     * overlay may consume only {@link ScanFeedbackPacket#aspects()}, while
     * this payload keeps current/max/type/modifier available to the same
     * Thaumometer result path without trusting client state.
     */
    public record NodeData(
            java.util.UUID nodeId,
            String type,
            String modifier,
            long revision,
            List<NodeAspect> aspects
    ) {
        public NodeData {
            nodeId = Objects.requireNonNull(nodeId, "nodeId");
            type = stableLowercase(type, "type");
            modifier = stableLowercase(modifier, "modifier");
            if (revision < 0L) {
                throw new IllegalArgumentException("revision cannot be negative");
            }
            aspects = List.copyOf(Objects.requireNonNull(aspects, "aspects"));
            Set<String> ids = new HashSet<>();
            for (NodeAspect aspect : aspects) {
                if (!ids.add(aspect.aspectId())) {
                    throw new IllegalArgumentException(
                            "duplicate node aspect " + aspect.aspectId()
                    );
                }
            }
            if (ids.isEmpty()) {
                throw new IllegalArgumentException(
                        "node payload must contain at least one aspect"
                );
            }
        }

        public static NodeData from(AuraNodeScanResult result) {
            Objects.requireNonNull(result, "result");
            return new NodeData(
                    result.nodeId(),
                    result.type().name().toLowerCase(Locale.ROOT),
                    result.modifier().name().toLowerCase(Locale.ROOT),
                    result.revision(),
                    result.aspectsCurrent().entrySet().stream()
                            .map(entry -> new NodeAspect(
                                    entry.getKey(),
                                    entry.getValue(),
                                    result.aspectsMaximum().get(entry.getKey())
                            ))
                            .toList()
            );
        }
    }

    public record NodeAspect(String aspectId, int current, int maximum) {
        public NodeAspect {
            aspectId = stableLowercase(aspectId, "aspectId");
            if (current < 0 || maximum < 0 || current > maximum) {
                throw new IllegalArgumentException(
                        "invalid node aspect pool " + aspectId
                                + ": " + current + "/" + maximum
                );
            }
        }
    }

    private static String stableLowercase(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()
                || !value.equals(value.trim())
                || !value.equals(value.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    fieldName + " must be non-blank, trimmed and lowercase"
            );
        }
        return value;
    }
}
