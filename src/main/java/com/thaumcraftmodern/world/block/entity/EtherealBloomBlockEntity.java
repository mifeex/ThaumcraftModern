package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;

/**
 * Every second a bloom repairs one random vertical column in its classic
 * radius and removes Flux Goo/Gas from that column.
 */
public final class EtherealBloomBlockEntity extends BlockEntity {
    private int ticks;
    private int growthCounter;

    public EtherealBloomBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ETHEREAL_BLOOM.get(), position, state);
    }

    public static void serverTick(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            EtherealBloomBlockEntity bloom
    ) {
        if (bloom.ticks == 0) {
            bloom.ticks = level.random.nextInt(100);
        }
        if (++bloom.ticks % 20 != 0) {
            return;
        }
        BlockPos target = position.offset(
                level.random.nextInt(8) - level.random.nextInt(8),
                0,
                level.random.nextInt(8) - level.random.nextInt(8)
        );
        if (position.distSqr(target) > 81.0D || !level.isLoaded(target)) {
            return;
        }
        TaintBiomeService.purifyColumn(level, target);
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        for (int y = minY; y < maxY; y++) {
            BlockPos scan = new BlockPos(target.getX(), y, target.getZ());
            BlockState found = level.getBlockState(scan);
            if (found.is(ModBlocks.FLUX_GOO.get())
                    || found.is(ModBlocks.FLUX_GAS.get())) {
                level.removeBlock(scan, false);
            }
        }
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            EtherealBloomBlockEntity bloom
    ) {
        if (bloom.growthCounter == 0) {
            level.playLocalSound(
                    position.getX() + 0.5D,
                    position.getY() + 0.5D,
                    position.getZ() + 0.5D,
                    ModSounds.ROOTS.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    0.6F,
                    false
            );
        }
        if (bloom.ticks == 0) {
            bloom.ticks = level.random.nextInt(100);
        }
        bloom.growthCounter++;
        bloom.ticks++;
    }

    public int growthCounter() {
        return growthCounter;
    }

    public int animationTicks() {
        return ticks;
    }
}
