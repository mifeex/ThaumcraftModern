package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.ElementalDowsingClient;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ElementalDowsingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ElementalPickaxeItem extends PickaxeItem {
    public static final int DOWSING_RADIUS = 8;
    public static final long DOWSING_MILLIS = 5_000L;

    public ElementalPickaxeItem(Properties properties) {
        super(ElementalTier.INSTANCE, 1, -2.8F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide
                && (!(target instanceof Player victim)
                || !(attacker instanceof ServerPlayer player)
                || player.server.isPvpAllowed()
                || victim == attacker)) {
            target.setSecondsOnFire(2);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        activateDowsing(context.getLevel(), player, context.getHand(),
                context.getItemInHand(), context.getClickedPos());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        BlockPos center = hit instanceof BlockHitResult blockHit
                && hit.getType() == HitResult.Type.BLOCK
                ? blockHit.getBlockPos()
                : player.blockPosition();
        activateDowsing(level, player, hand, stack, center);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void activateDowsing(
            Level level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BlockPos center
    ) {
        if (level instanceof ServerLevel server) {
            stack.hurtAndBreak(5, player, broken -> broken.broadcastBreakEvent(hand));
            server.playSound(null, center, ModSounds.WAND_FAIL.get(),
                    SoundSource.PLAYERS, 0.55F,
                    0.2F + server.random.nextFloat() * 0.2F);
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.sendTo(serverPlayer, new ElementalDowsingPacket(
                        center,
                        DOWSING_RADIUS,
                        DOWSING_MILLIS
                ));
            }
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ElementalDowsingClient.start(center, DOWSING_RADIUS, DOWSING_MILLIS));
            player.swing(hand);
        }
    }
}
