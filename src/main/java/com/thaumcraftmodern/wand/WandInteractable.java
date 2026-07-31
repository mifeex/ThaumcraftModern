package com.thaumcraftmodern.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Modern counterpart of TC4's {@code IWandable} block callback.
 *
 * <p>{@link com.thaumcraftmodern.item.WandItem} invokes this path directly,
 * including while the player is sneaking, because vanilla skips ordinary
 * block interaction for many shift-use item actions.</p>
 */
public interface WandInteractable {
    InteractionResult onWandRightClick(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    );
}
