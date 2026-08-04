package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only equivalents of TC4 BlockAiry hit effects and its zero-sized
 * selection box. The logical hit shape remains selectable for mining/wands.
 */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientAuraNodeHitFeedback {
    private static final long FLASH_DURATION_NANOS = 280_000_000L;
    private static final long PARTICLE_INTERVAL_NANOS = 80_000_000L;

    private static ResourceKey<Level> lastDimension;
    private static BlockPos lastPosition;
    private static long lastHitNanos = Long.MIN_VALUE;
    private static long lastParticleNanos = Long.MIN_VALUE;

    private ClientAuraNodeHitFeedback() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(
            PlayerInteractEvent.LeftClickBlock event
    ) {
        if (!event.getLevel().isClientSide
                || !event.getLevel().getBlockState(event.getPos())
                .is(ModBlocks.AURA_NODE.get())) {
            return;
        }
        PlayerInteractEvent.LeftClickBlock.Action action = event.getAction();
        if (action != PlayerInteractEvent.LeftClickBlock.Action.START
                && action != PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD) {
            return;
        }

        long now = System.nanoTime();
        lastDimension = event.getLevel().dimension();
        lastPosition = event.getPos().immutable();
        lastHitNanos = now;

        if (now - lastParticleNanos < PARTICLE_INTERVAL_NANOS) {
            return;
        }
        lastParticleNanos = now;
        /*
         * The former END_ROD placeholder was not part of TC4 and could render
         * as a missing sprite in some resource-pack combinations. The hit
         * flash is drawn directly from nodes.png by the node renderer.
         */
    }

    @SubscribeEvent
    public static void hideBlockOutline(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        if (level != null
                && (level.getBlockState(event.getTarget().getBlockPos())
                .is(ModBlocks.AURA_NODE.get())
                || level.getBlockState(event.getTarget().getBlockPos())
                .is(ModBlocks.ENERGIZED_AURA_NODE.get()))) {
            event.setCanceled(true);
        }
    }

    static float flashStrength(Level level, BlockPos position) {
        if (lastDimension == null
                || lastPosition == null
                || !lastDimension.equals(level.dimension())
                || !lastPosition.equals(position)) {
            return 0.0F;
        }
        long elapsed = System.nanoTime() - lastHitNanos;
        if (elapsed < 0L || elapsed >= FLASH_DURATION_NANOS) {
            return 0.0F;
        }
        return 1.0F - elapsed / (float) FLASH_DURATION_NANOS;
    }
}
