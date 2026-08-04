package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Coordinates for TC4's short-lived node-to-node FXLightningBolt. */
public record NodeZapPacket(BlockPos from, BlockPos to, long seed) {
    public static void encode(NodeZapPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.from);
        buffer.writeBlockPos(packet.to);
        buffer.writeLong(packet.seed);
    }

    public static NodeZapPacket decode(FriendlyByteBuf buffer) {
        return new NodeZapPacket(
                buffer.readBlockPos(),
                buffer.readBlockPos(),
                buffer.readLong()
        );
    }

    public static void handle(
            NodeZapPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleNodeZap(packet)
        );
        context.get().setPacketHandled(true);
    }
}
