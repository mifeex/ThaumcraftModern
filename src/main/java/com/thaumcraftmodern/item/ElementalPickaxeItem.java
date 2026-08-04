package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.ElementalDowsingClient;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.context.UseOnContext;
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
        ItemStack stack = context.getItemInHand();
        if (context.getLevel() instanceof ServerLevel level) {
            stack.hurtAndBreak(5, player, broken -> broken.broadcastBreakEvent(context.getHand()));
            level.playSound(null, context.getClickedPos(), ModSounds.WAND_FAIL.get(),
                    SoundSource.PLAYERS, 0.2F, 0.2F + level.random.nextFloat() * 0.2F);
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ElementalDowsingClient.start(context.getClickedPos(), DOWSING_RADIUS, DOWSING_MILLIS));
            player.swing(context.getHand());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
