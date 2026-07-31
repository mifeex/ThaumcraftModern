package com.thaumcraftmodern.item;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.WarpType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SanityCheckerItem extends Item {
    public SanityCheckerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            KnowledgeAccess.get(serverPlayer).ifPresent(knowledge -> {
                int permanent = knowledge.warp(WarpType.PERMANENT);
                int normal = knowledge.warp(WarpType.NORMAL);
                int temporary = knowledge.warp(WarpType.TEMPORARY);
                serverPlayer.displayClientMessage(
                        Component.translatable(
                                "tc.sanity.detail",
                                permanent,
                                normal,
                                temporary
                        ),
                        true
                );
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
