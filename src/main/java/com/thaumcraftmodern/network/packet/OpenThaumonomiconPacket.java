package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenThaumonomiconPacket() {
    public static void encode(OpenThaumonomiconPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenThaumonomiconPacket decode(FriendlyByteBuf buffer) {
        return new OpenThaumonomiconPacket();
    }

    public static void handle(OpenThaumonomiconPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandlers::openThaumonomicon);
        context.get().setPacketHandled(true);
    }
}

