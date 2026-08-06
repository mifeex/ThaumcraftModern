package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.scan.InventoryScanService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One server-tick heartbeat while a carried Thaumometer hovers a menu slot. */
public record InventoryScanPacket(int containerId, int slotIndex) {
    public static void encode(InventoryScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeVarInt(packet.slotIndex);
    }

    public static InventoryScanPacket decode(FriendlyByteBuf buffer) {
        return new InventoryScanPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(InventoryScanPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                InventoryScanService.hover(context.getSender(),
                        packet.containerId, packet.slotIndex);
            }
        });
        context.setPacketHandled(true);
    }
}
