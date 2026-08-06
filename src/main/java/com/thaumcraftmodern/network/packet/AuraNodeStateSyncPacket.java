package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Immediate full node snapshot used by the renderer and revealing HUD. */
public record AuraNodeStateSyncPacket(BlockPos position, CompoundTag state) {
    public AuraNodeStateSyncPacket {
        state = state.copy();
    }

    public static void encode(
            AuraNodeStateSyncPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.position);
        buffer.writeNbt(packet.state);
    }

    public static AuraNodeStateSyncPacket decode(FriendlyByteBuf buffer) {
        BlockPos position = buffer.readBlockPos();
        CompoundTag state = buffer.readNbt();
        return new AuraNodeStateSyncPacket(
                position,
                state == null ? new CompoundTag() : state
        );
    }

    public static void handle(
            AuraNodeStateSyncPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleAuraNodeState(packet)
        );
        context.get().setPacketHandled(true);
    }
}
