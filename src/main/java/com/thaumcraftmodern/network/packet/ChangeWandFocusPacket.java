package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.focus.WandFocusService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeWandFocusPacket(String focusId) {
    public static void encode(ChangeWandFocusPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.focusId, 64);
    }

    public static ChangeWandFocusPacket decode(FriendlyByteBuf buffer) {
        return new ChangeWandFocusPacket(buffer.readUtf(64));
    }

    public static void handle(ChangeWandFocusPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null)
                WandFocusService.changeFocus(context.getSender(), packet.focusId);
        });
        context.setPacketHandled(true);
    }
}
