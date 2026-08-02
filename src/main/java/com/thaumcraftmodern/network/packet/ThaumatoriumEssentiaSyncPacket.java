package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Immediate client snapshot for Thaumatorium progress bars and goggles HUD. */
public record ThaumatoriumEssentiaSyncPacket(
        BlockPos position,
        CompoundTag essentia
) {
    public static void encode(
            ThaumatoriumEssentiaSyncPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.position);
        buffer.writeNbt(packet.essentia);
    }

    public static ThaumatoriumEssentiaSyncPacket decode(FriendlyByteBuf buffer) {
        BlockPos position = buffer.readBlockPos();
        CompoundTag essentia = buffer.readNbt();
        return new ThaumatoriumEssentiaSyncPacket(
                position,
                essentia == null ? new CompoundTag() : essentia
        );
    }

    public static void handle(
            ThaumatoriumEssentiaSyncPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleThaumatoriumEssentia(packet)
        );
        context.get().setPacketHandled(true);
    }
}
