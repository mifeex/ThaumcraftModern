package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Server ticker preserving TC4 {@code TileWardingStone}. */
public final class PavingStoneOfWardingBlockEntity extends BlockEntity {
    private int count;

    public PavingStoneOfWardingBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(ModBlockEntities.PAVING_STONE_OF_WARDING.get(), position, state);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            PavingStoneOfWardingBlockEntity stone
    ) {
        if (stone.count == 0) {
            stone.count = level.random.nextInt(100);
        }

        if (stone.count % 5 == 0 && !level.hasNeighborSignal(position)) {
            repelAirborneCreatures(level, position);
        }

        if (++stone.count % 100 == 0) {
            ensureAura(level, position.above());
            ensureAura(level, position.above(2));
        }
    }

    private static void repelAirborneCreatures(
            Level level,
            BlockPos position
    ) {
        AABB wardingColumn = new AABB(
                position,
                position.offset(1, 3, 1)
        ).inflate(0.1D);
        for (LivingEntity living : level.getEntitiesOfClass(
                LivingEntity.class,
                wardingColumn
        )) {
            if (living.onGround() || living instanceof Player) {
                continue;
            }
            float angle = (living.getYRot() + 180.0F) * Mth.DEG_TO_RAD;
            living.push(
                    -Mth.sin(angle) * 0.2F,
                    -0.1D,
                    Mth.cos(angle) * 0.2F
            );
        }
    }

    private static void ensureAura(Level level, BlockPos position) {
        BlockState current = level.getBlockState(position);
        if (!current.is(ModBlocks.WARDING_AURA.get())
                && current.canBeReplaced()) {
            level.setBlock(
                    position,
                    ModBlocks.WARDING_AURA.get().defaultBlockState(),
                    3
            );
        }
    }
}
