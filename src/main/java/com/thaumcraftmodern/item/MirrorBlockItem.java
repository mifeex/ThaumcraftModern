package com.thaumcraftmodern.item;

import com.thaumcraftmodern.mirror.LinkedMirrorBlockEntity;
import com.thaumcraftmodern.mirror.MirrorLink;
import com.thaumcraftmodern.world.block.MagicMirrorBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Pairing item contract from TC4's BlockMirrorItem. */
public final class MirrorBlockItem extends BlockItem {
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    public MirrorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level rawLevel = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(rawLevel instanceof ServerLevel level)
                || !(getBlock() instanceof MagicMirrorBlock carried)
                || !(rawLevel.getBlockState(pos).getBlock()
                instanceof MagicMirrorBlock placed)
                || carried.essentia() != placed.essentia()
                || !(rawLevel.getBlockEntity(pos)
                instanceof LinkedMirrorBlockEntity mirror)
                || mirror.validReciprocalLink()) return InteractionResult.PASS;
        ItemStack linked = stack.copy();
        linked.setCount(1);
        CompoundTag payload = new CompoundTag();
        MirrorLink.of(level, pos).save(payload);
        linked.addTagElement(BLOCK_ENTITY_TAG, payload);
        Player player = context.getPlayer();
        if (player != null) {
            if (!player.getInventory().add(linked)) player.drop(linked, false);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.swing(context.getHand(), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && context.getLevel() instanceof ServerLevel
                && context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof LinkedMirrorBlockEntity mirror) {
            CompoundTag payload = context.getItemInHand().getTagElement(BLOCK_ENTITY_TAG);
            MirrorLink destination = payload == null ? null : MirrorLink.load(payload);
            if (destination != null) {
                mirror.setDestination(destination);
                mirror.restoreLink();
            }
        }
        return placed;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        CompoundTag payload = stack.getTagElement(BLOCK_ENTITY_TAG);
        MirrorLink link = payload == null ? null : MirrorLink.load(payload);
        if (link != null) {
            tooltip.add(Component.translatable("tc.handmirrorlinkedto")
                    .append(" " + link.display()).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
