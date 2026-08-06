package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientRunicShieldEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record RunicShieldFxPacket(int entityId, int sourceId) {
    public static void encode(RunicShieldFxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId); buffer.writeInt(packet.sourceId);
    }
    public static RunicShieldFxPacket decode(FriendlyByteBuf buffer) {
        return new RunicShieldFxPacket(buffer.readVarInt(), buffer.readInt());
    }
    public static void handle(RunicShieldFxPacket packet,
            Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientRunicShieldEffect.accept(packet.entityId, packet.sourceId));
        context.get().setPacketHandled(true);
    }
}
