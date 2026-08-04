package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.item.ElementalShovelItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CycleShovelOrientationPacket {
    public static void encode(CycleShovelOrientationPacket packet, FriendlyByteBuf buffer) {}
    public static CycleShovelOrientationPacket decode(FriendlyByteBuf buffer) {
        return new CycleShovelOrientationPacket();
    }
    public static void handle(CycleShovelOrientationPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ElementalShovelItem) {
                ElementalShovelItem.cycleOrientation(stack);
                player.getInventory().setChanged();
            }
        });
        context.setPacketHandled(true);
    }
}
