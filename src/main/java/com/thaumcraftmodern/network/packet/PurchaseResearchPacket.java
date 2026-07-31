package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.research.ResearchPurchaseService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PurchaseResearchPacket(String researchId) {
    private static final int MAX_RESEARCH_ID_LENGTH = 128;

    public PurchaseResearchPacket {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
    }

    public static void encode(
            PurchaseResearchPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(packet.researchId, MAX_RESEARCH_ID_LENGTH);
    }

    public static PurchaseResearchPacket decode(FriendlyByteBuf buffer) {
        return new PurchaseResearchPacket(
                buffer.readUtf(MAX_RESEARCH_ID_LENGTH)
        );
    }

    public static void handle(
            PurchaseResearchPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) {
            ResearchPurchaseService.purchase(sender, packet.researchId);
        }
        context.get().setPacketHandled(true);
    }
}
