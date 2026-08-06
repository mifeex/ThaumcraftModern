package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientRunicShieldState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record RunicShieldSyncPacket(int charge, int maximum) {
    public static void encode(RunicShieldSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.charge); buffer.writeVarInt(packet.maximum);
    }
    public static RunicShieldSyncPacket decode(FriendlyByteBuf buffer) {
        return new RunicShieldSyncPacket(buffer.readVarInt(), buffer.readVarInt());
    }
    public static void handle(RunicShieldSyncPacket packet,
            Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientRunicShieldState.set(packet.charge, packet.maximum));
        context.get().setPacketHandled(true);
    }
}
