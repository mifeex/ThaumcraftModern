package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.research.ResearchNoteAcquisitionService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestResearchNotesPacket(String researchId) {
    private static final int MAX_RESEARCH_ID_LENGTH = 128;

    public RequestResearchNotesPacket {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
    }

    public static void encode(
            RequestResearchNotesPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(packet.researchId, MAX_RESEARCH_ID_LENGTH);
    }

    public static RequestResearchNotesPacket decode(FriendlyByteBuf buffer) {
        return new RequestResearchNotesPacket(
                buffer.readUtf(MAX_RESEARCH_ID_LENGTH)
        );
    }

    public static void handle(
            RequestResearchNotesPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        ServerPlayer sender = context.get().getSender();
        if (sender != null) {
            ResearchNoteAcquisitionService.request(sender, packet.researchId);
        }
        context.get().setPacketHandled(true);
    }
}
