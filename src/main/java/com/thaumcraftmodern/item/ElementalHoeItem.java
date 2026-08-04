package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public final class ElementalHoeItem extends HoeItem {
    public ElementalHoeItem(Properties properties) {
        super(ElementalTier.INSTANCE, -4, 0.0F, properties);
    }

    @Override public int getEnchantmentValue() { return 5; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return super.useOn(context);
        boolean changed = false;
        BlockPos origin = context.getClickedPos();
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos target = origin.offset(x, 0, z);
            BlockHitResult hit = new BlockHitResult(context.getClickLocation().add(x, 0, z),
                    context.getClickedFace(), target, context.isInside());
            InteractionResult result = super.useOn(new UseOnContext(player, context.getHand(), hit));
            if (result.consumesAction()) {
                changed = true;
                sparkle(context, target, 2);
            }
        }
        if (!changed && context.getLevel() instanceof ServerLevel level) {
            ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
            changed = BoneMealItem.applyBonemeal(bonemeal, level, origin, player)
                    || BoneMealItem.growWaterPlant(bonemeal, level, origin, context.getClickedFace());
            if (changed) {
                context.getItemInHand().hurtAndBreak(1, player,
                        broken -> broken.broadcastBreakEvent(context.getHand()));
                level.levelEvent(1505, origin, 0);
                level.playSound(null, origin, ModSounds.WAND.get(), SoundSource.PLAYERS,
                        0.75F, 0.9F + level.random.nextFloat() * 0.2F);
                sparkle(context, origin, 3);
            }
        }
        return changed ? InteractionResult.sidedSuccess(context.getLevel().isClientSide) : InteractionResult.PASS;
    }

    private static void sparkle(UseOnContext context, BlockPos position, int count) {
        if (context.getLevel() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.getX() + 0.5D,
                    position.getY() + 0.8D, position.getZ() + 0.5D, count,
                    0.35D, 0.25D, 0.35D, 0.0D);
        }
    }
}
