package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class LiquidDeathBucketItem extends Item {
    public LiquidDeathBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        if (!player.mayUseItemAt(target, hit.getDirection(), stack)
                || !level.getBlockState(target).canBeReplaced()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            level.setBlock(target, ModBlocks.LIQUID_DEATH.get().defaultBlockState(), 3);
            level.playSound(null, target, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        return InteractionResultHolder.sidedSuccess(
                player.getAbilities().instabuild ? stack : new ItemStack(Items.BUCKET),
                level.isClientSide
        );
    }
}
