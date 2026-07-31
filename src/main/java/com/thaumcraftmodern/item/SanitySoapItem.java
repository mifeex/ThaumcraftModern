package com.thaumcraftmodern.item;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * TC4 sanitizing soap: a long use clears all temporary warp and has a 33%
 * chance to remove one sticky point, increased by Warp Ward.
 */
public final class SanitySoapItem extends Item {
    public static final int USE_TICKS = 200;
    public static final int REQUIRED_TICKS = 196;

    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public void onUseTick(
            Level level,
            LivingEntity entity,
            ItemStack stack,
            int timeLeft
    ) {
        if (USE_TICKS - timeLeft >= REQUIRED_TICKS
                && entity instanceof ServerPlayer player) {
            cleanse(stack, player);
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            int timeLeft
    ) {
        if (!(entity instanceof ServerPlayer player)
                || USE_TICKS - timeLeft < REQUIRED_TICKS) {
            return;
        }
        cleanse(stack, player);
    }

    private static void cleanse(ItemStack stack, ServerPlayer player) {
        KnowledgeAccess.get(player).ifPresent(knowledge -> {
            int temporary = knowledge.warp(WarpType.TEMPORARY);
            int normal = knowledge.warp(WarpType.NORMAL);
            float chance = 0.33F;
            if (player.hasEffect(ModEffects.WARP_WARD.get())) {
                chance += 0.25F;
            }
            if (normal > 0 && player.getRandom().nextFloat() < chance) {
                knowledge.setWarp(WarpType.NORMAL, normal - 1);
                ModNetwork.sendTo(player, new WarpFeedbackPacket(
                        WarpFeedbackPacket.NORMAL,
                        -1,
                        WarpFeedbackPacket.VISUAL_NONE
                ));
            }
            if (temporary > 0) {
                knowledge.setWarp(WarpType.TEMPORARY, 0);
            }
            KnowledgeSync.send(player, "sanity_soap");
        });
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
