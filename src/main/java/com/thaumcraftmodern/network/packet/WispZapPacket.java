package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * TC4's PacketFXWispZap: tells tracking clients to draw a short-lived
 * textured bolt between two already synchronized entities.
 */
public record WispZapPacket(int sourceId, int targetId, long seed) {
    public static void encode(
            WispZapPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(packet.sourceId);
        buffer.writeVarInt(packet.targetId);
        buffer.writeLong(packet.seed);
    }

    public static WispZapPacket decode(FriendlyByteBuf buffer) {
        return new WispZapPacket(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readLong()
        );
    }

    public static void handle(
            WispZapPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleWispZap(packet)
        );
        context.get().setPacketHandled(true);
    }
}
