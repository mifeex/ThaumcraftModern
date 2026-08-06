package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-confirmed activation of the Pickaxe of the Core ore reveal. */
public record ElementalDowsingPacket(
        BlockPos center,
        int radius,
        long durationMillis
) {
    public static void encode(ElementalDowsingPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.center);
        buffer.writeVarInt(packet.radius);
        buffer.writeVarLong(packet.durationMillis);
    }

    public static ElementalDowsingPacket decode(FriendlyByteBuf buffer) {
        return new ElementalDowsingPacket(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarLong()
        );
    }

    public static void handle(
            ElementalDowsingPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleElementalDowsing(packet)
        );
        context.get().setPacketHandled(true);
    }
}
