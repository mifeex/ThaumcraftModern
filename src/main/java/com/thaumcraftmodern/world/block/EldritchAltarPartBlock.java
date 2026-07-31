package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Invisible, non-dropping anchors for the original TC4 eldritch altar TESR.
 */
public final class EldritchAltarPartBlock extends BaseEntityBlock {
    static final float TNT_EXPLOSION_POWER = 4.0F;
    static final float ALTAR_COLLAPSE_EXPLOSION_POWER =
            TNT_EXPLOSION_POWER * 1.20F;
    public static final IntegerProperty PART =
            IntegerProperty.create("part", 0, 4);
    private static final ThreadLocal<Boolean> DESTROYING_ASSEMBLY =
            ThreadLocal.withInitial(() -> false);

    public EldritchAltarPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    public BlockState stateForPart(int part) {
        return defaultBlockState().setValue(PART, part);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        int part = state.getValue(PART);
        return part == 2 || part == 3
                ? null
                : new EldritchAltarPartBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (type != ModBlockEntities.ELDRITCH_ALTAR_PART.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (tickerLevel, position, tickerState, blockEntity) ->
                    EldritchAltarPartBlockEntity.clientTick(
                            tickerLevel,
                            position,
                            tickerState,
                            (EldritchAltarPartBlockEntity) blockEntity
                    );
        }
        return (tickerLevel, position, tickerState, blockEntity) ->
                    EldritchAltarPartBlockEntity.serverTick(
                            (net.minecraft.server.level.ServerLevel) tickerLevel,
                            position,
                            tickerState,
                            (EldritchAltarPartBlockEntity) blockEntity
                    );
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean movedByPiston
    ) {
        int part = state.getValue(PART);
        boolean removed = !state.is(newState.getBlock());
        if (level instanceof ServerLevel serverLevel && removed) {
            spawnTextureBreakParticles(serverLevel, position, state);
        }
        if (!level.isClientSide
                && removed
                && part != 4
                && !DESTROYING_ASSEMBLY.get()) {
            BlockPos center = findCenter(level, position, state);
            if (center != null) {
                destroyAltarAssembly(level, center);
            }
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    private static void spawnTextureBreakParticles(
            ServerLevel level,
            BlockPos position,
            BlockState state
    ) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D,
                28,
                0.36D,
                0.36D,
                0.36D,
                0.06D
        );
    }

    private static @Nullable BlockPos findCenter(
            Level level,
            BlockPos removedPosition,
            BlockState removedState
    ) {
        int removedPart = removedState.getValue(PART);
        if (removedPart == 0) {
            return removedPosition;
        }
        if (removedPart == 1) {
            return removedPosition.below(2);
        }
        if (removedPart == 2 || removedPart == 3) {
            for (int distance = 1; distance <= 4; distance++) {
                BlockPos candidate = removedPosition.below(distance);
                BlockState candidateState = level.getBlockState(candidate);
                if (candidateState.getBlock()
                        instanceof EldritchAltarPartBlock
                        && candidateState.getValue(PART) == 1) {
                    return candidate.below(2);
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Called by the aura-node block when it detects this exact altar layout.
     */
    public static void destroyFromAuraNode(
            Level level,
            BlockPos nodePosition
    ) {
        if (level.isClientSide || DESTROYING_ASSEMBLY.get()) {
            return;
        }
        BlockPos center = nodePosition.below();
        BlockState pedestal = level.getBlockState(center);
        BlockState obelisk = level.getBlockState(nodePosition.above());
        if (pedestal.getBlock() instanceof EldritchAltarPartBlock
                && pedestal.getValue(PART) == 0
                && obelisk.getBlock() instanceof EldritchAltarPartBlock
                && obelisk.getValue(PART) == 1) {
            destroyAltarAssembly(level, center);
        }
    }

    private static void destroyAltarAssembly(
            Level level,
            BlockPos center
    ) {
        DESTROYING_ASSEMBLY.set(true);
        try {
            for (int height = 6; height >= 0; height--) {
                BlockPos part = center.above(height);
                if (level.getBlockState(part).getBlock()
                        instanceof EldritchAltarPartBlock) {
                    level.setBlock(
                            part,
                            Blocks.AIR.defaultBlockState(),
                            3
                    );
                }
            }
            level.setBlock(
                    center.above(),
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        } finally {
            DESTROYING_ASSEMBLY.set(false);
        }
        level.explode(
                null,
                center.getX() + 0.5D,
                center.getY() + 1.5D,
                center.getZ() + 0.5D,
                ALTAR_COLLAPSE_EXPLOSION_POWER,
                Level.ExplosionInteraction.TNT
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(PART);
    }
}
