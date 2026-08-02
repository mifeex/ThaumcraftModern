package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Client particle source matching TC4 {@code TileNitor}. */
public final class NitorBlockEntity extends BlockEntity {
    private static final int LARGE_WISP_INTERVAL = 5;
    private static final int SMALL_WISP_INTERVAL = 7;

    public NitorBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.NITOR.get(), position, state);
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            NitorBlockEntity nitor
    ) {
        double centerX = position.getX() + 0.5D;
        double centerY = position.getY() + 0.5D;
        double centerZ = position.getZ() + 0.5D;
        if (level.random.nextInt(LARGE_WISP_INTERVAL) == 0) {
            level.addParticle(
                    ModParticles.NITOR_WISP_LARGE.get(),
                    centerX,
                    centerY,
                    centerZ,
                    position.getX() + 0.3D
                            + level.random.nextFloat() * 0.4D,
                    centerY,
                    position.getZ() + 0.3D
                            + level.random.nextFloat() * 0.4D
            );
        }
        if (level.random.nextInt(SMALL_WISP_INTERVAL) == 0) {
            level.addParticle(
                    ModParticles.NITOR_WISP_SMALL.get(),
                    centerX,
                    centerY,
                    centerZ,
                    position.getX() + 0.4D
                            + level.random.nextFloat() * 0.2D,
                    centerY,
                    position.getZ() + 0.4D
                            + level.random.nextFloat() * 0.2D
            );
        }
    }
}
