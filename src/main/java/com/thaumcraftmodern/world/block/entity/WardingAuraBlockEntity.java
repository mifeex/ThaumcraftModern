package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Self-cleaning invisible fence segment from TC4. */
public final class WardingAuraBlockEntity extends BlockEntity {
    private int count;

    public WardingAuraBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.WARDING_AURA.get(), position, state);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            WardingAuraBlockEntity aura
    ) {
        if (aura.count == 0) {
            aura.count = level.random.nextInt(100);
        }
        if (++aura.count % 100 != 0) {
            return;
        }
        boolean supported = level.getBlockState(position.below()).is(
                ModBlocks.PAVING_STONE_OF_WARDING.get()
        ) || level.getBlockState(position.below(2)).is(
                ModBlocks.PAVING_STONE_OF_WARDING.get()
        );
        if (!supported) {
            level.removeBlock(position, false);
        }
    }
}
