package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record KnowledgeSyncPacket(
        CompoundTag knowledge,
        CompoundTag aspects,
        CompoundTag research,
        CompoundTag researchCategories,
        CompoundTag scans,
        CompoundTag wands
) {
    public static void encode(KnowledgeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.knowledge);
        buffer.writeNbt(packet.aspects);
        buffer.writeNbt(packet.research);
        buffer.writeNbt(packet.researchCategories);
        buffer.writeNbt(packet.scans);
        buffer.writeNbt(packet.wands);
    }

    public static KnowledgeSyncPacket decode(FriendlyByteBuf buffer) {
        return new KnowledgeSyncPacket(
                nonNull(buffer.readNbt()),
                nonNull(buffer.readNbt()),
                nonNull(buffer.readNbt()),
                nonNull(buffer.readNbt()),
                nonNull(buffer.readNbt()),
                nonNull(buffer.readNbt())
        );
    }

    private static CompoundTag nonNull(CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag;
    }

    public static void handle(KnowledgeSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.handleKnowledgeSync(packet));
        context.get().setPacketHandled(true);
    }
}
