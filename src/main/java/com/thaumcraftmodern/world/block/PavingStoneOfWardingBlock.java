package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.world.block.entity.PavingStoneOfWardingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** TC4 paving stone that projects a two-block-high creature barrier. */
public final class PavingStoneOfWardingBlock extends BaseEntityBlock {
    public PavingStoneOfWardingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new PavingStoneOfWardingBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        type,
                        ModBlockEntities.PAVING_STONE_OF_WARDING.get(),
                        PavingStoneOfWardingBlockEntity::serverTick
                );
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos position,
            RandomSource random
    ) {
        if (level.hasNeighborSignal(position)) {
            spawnRunes(
                    level,
                    position,
                    ModParticles.WARDING_RUNE_DISABLED.get(),
                    2,
                    position.getY() + 1.2D
            );
            return;
        }

        if (!WardingAuraBlock.hasAuraSpace(level, position.above())
                || !WardingAuraBlock.hasAuraSpace(
                        level,
                        position.above(2)
                )) {
            spawnRunes(
                    level,
                    position,
                    ModParticles.WARDING_RUNE_BLOCKED.get(),
                    3,
                    position.getY() + 1.2D
            );
            return;
        }

        for (LivingEntity living : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(position).inflate(1.0D)
        )) {
            if (living instanceof Player) {
                continue;
            }
            level.addParticle(
                    ModParticles.WARDING_RUNE_ACTIVE.get(),
                    position.getX() + 0.5D,
                    position.getY() + 1.1D
                            + random.nextFloat()
                            * Math.max(0.8F, living.getEyeHeight()),
                    position.getZ() + 0.5D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            break;
        }
    }

    private static void spawnRunes(
            Level level,
            BlockPos position,
            SimpleParticleType particle,
            int count,
            double y
    ) {
        for (int index = 0; index < count; index++) {
            level.addParticle(
                    particle,
                    position.getX() + 0.5D,
                    y,
                    position.getZ() + 0.5D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
}
