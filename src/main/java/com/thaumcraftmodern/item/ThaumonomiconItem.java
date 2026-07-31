package com.thaumcraftmodern.item;

import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.OpenThaumonomiconPacket;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThaumonomiconItem extends Item {
    public ThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ResearchDiagnostics.log(
                    "SERVER_THAUMONOMICON_USE",
                    "player={} hand={} stack={}",
                    serverPlayer.getGameProfile().getName(),
                    hand,
                    stack
            );
            KnowledgeSync.send(serverPlayer, "thaumonomicon.open");
            ModNetwork.sendTo(serverPlayer, new OpenThaumonomiconPacket());
            ResearchDiagnostics.log(
                    "SERVER_THAUMONOMICON_PACKET",
                    "player={} openPacketSent=true",
                    serverPlayer.getGameProfile().getName()
            );
            serverPlayer.playNotifySound(
                    ModSounds.PAGE.get(),
                    SoundSource.PLAYERS,
                    0.65F,
                    0.95F + player.getRandom().nextFloat() * 0.1F
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
