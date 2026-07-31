package com.thaumcraftmodern.network;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.network.packet.KnowledgeSyncPacket;
import com.thaumcraftmodern.network.packet.OpenThaumonomiconPacket;
import com.thaumcraftmodern.network.packet.PurchaseResearchPacket;
import com.thaumcraftmodern.network.packet.RequestResearchNotesPacket;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.network.packet.WispZapPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "10";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static boolean registered;

    private ModNetwork() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        int id = 0;
        CHANNEL.messageBuilder(KnowledgeSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(KnowledgeSyncPacket::encode)
                .decoder(KnowledgeSyncPacket::decode)
                .consumerMainThread(KnowledgeSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(ScanFeedbackPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScanFeedbackPacket::encode)
                .decoder(ScanFeedbackPacket::decode)
                .consumerMainThread(ScanFeedbackPacket::handle)
                .add();
        CHANNEL.messageBuilder(WarpFeedbackPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WarpFeedbackPacket::encode)
                .decoder(WarpFeedbackPacket::decode)
                .consumerMainThread(WarpFeedbackPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenThaumonomiconPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenThaumonomiconPacket::encode)
                .decoder(OpenThaumonomiconPacket::decode)
                .consumerMainThread(OpenThaumonomiconPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        WispZapPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(WispZapPacket::encode)
                .decoder(WispZapPacket::decode)
                .consumerMainThread(WispZapPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        RequestResearchNotesPacket.class,
                        id++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestResearchNotesPacket::encode)
                .decoder(RequestResearchNotesPacket::decode)
                .consumerMainThread(RequestResearchNotesPacket::handle)
                .add();
        CHANNEL.messageBuilder(
                        PurchaseResearchPacket.class,
                        id,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(PurchaseResearchPacket::encode)
                .decoder(PurchaseResearchPacket::decode)
                .consumerMainThread(PurchaseResearchPacket::handle)
                .add();
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToTracking(Entity entity, Object packet) {
        CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                packet
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
