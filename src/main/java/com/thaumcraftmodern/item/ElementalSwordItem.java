package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ElementalSwordItem extends SwordItem {
    private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);

    public ElementalSwordItem(Properties properties) {
        super(ElementalTier.INSTANCE, 3, -2.4F, properties);
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }
    @Override public int getUseDuration(ItemStack stack) { return 72_000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(living instanceof Player player)) return;
        if (!player.isShiftKeyDown()) {
            player.stopUsingItem();
            return;
        }
        int elapsed = getUseDuration(stack) - remaining;
        Vec3 movement = player.getDeltaMovement();
        double y = movement.y;
        if (y < 0.0D) {
            y /= 1.2D;
            player.fallDistance /= 1.2F;
        }
        y += 0.08D;
        if (y > 0.5D) y = 0.2D;
        player.setDeltaMovement(movement.x, y, movement.z);
        AABB range = player.getBoundingBox().inflate(2.5D);
        for (Entity target : level.getEntities(player, range,
                entity -> entity.isAlive() && !(entity instanceof Player) && entity != player.getVehicle())) {
            Vec3 delta = target.position().subtract(player.position());
            double distance = delta.length() + 0.1D;
            target.setDeltaMovement(target.getDeltaMovement().add(delta.scale(1.0D / 2.5D / distance)));
            target.hurtMarked = true;
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + player.getBbHeight() / 2.0D,
                    player.getZ(), 5, 1.0D, player.getBbHeight() / 2.0D, 1.0D, 0.03D);
            if (elapsed == 0 || elapsed % 20 == 0) {
                server.playSound(null, player.blockPosition(), ModSounds.WIND.get(), SoundSource.PLAYERS,
                        0.5F, 0.9F + server.random.nextFloat() * 0.2F);
            }
        }
        if (elapsed % 20 == 0) {
            stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player) || attacker.level().isClientSide || SWEEPING.get()) return result;
        int hits = 0;
        SWEEPING.set(true);
        try {
            AABB range = target.getBoundingBox().inflate(1.2D, 1.1D, 1.2D);
            for (LivingEntity candidate : attacker.level().getEntitiesOfClass(LivingEntity.class, range,
                    entity -> entity != target && entity != attacker && entity.isAlive())) {
                if (candidate instanceof TamableAnimal tameable && tameable.isOwnedBy(player)) continue;
                if (candidate.isAlliedTo(player)) continue;
                player.attack(candidate);
                hits++;
            }
        } finally {
            SWEEPING.set(false);
        }
        if (hits > 0 && attacker.level() instanceof ServerLevel server) {
            server.playSound(null, target.blockPosition(), ModSounds.SWING.get(), SoundSource.PLAYERS,
                    1.0F, 0.9F + server.random.nextFloat() * 0.2F);
        }
        return result;
    }
}
