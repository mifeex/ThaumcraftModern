package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Immediate authoritative copy of the held bell after link/marker NBT edits. */
public record GolemBellSyncPacket(InteractionHand hand, ItemStack bell) {
    public GolemBellSyncPacket {
        bell = bell.copy();
    }

    public static void encode(GolemBellSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand());
        buffer.writeItem(packet.bell());
    }

    public static GolemBellSyncPacket decode(FriendlyByteBuf buffer) {
        return new GolemBellSyncPacket(
                buffer.readEnum(InteractionHand.class),
                buffer.readItem()
        );
    }

    public static void handle(
            GolemBellSyncPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleGolemBellSync(packet)
        );
        context.get().setPacketHandled(true);
    }
}
