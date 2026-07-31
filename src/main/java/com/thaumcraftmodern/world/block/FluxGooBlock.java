package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundSource;

/**
 * Finite TC4 Flux Goo. Its level mirrors the old metadata range 0..7.
 */
public final class FluxGooBlock extends Block
        implements LiquidBlockContainer {
    public static final int DENSITY = 8;
    public static final int FLOW_TICK_DELAY = 30;
    public static final IntegerProperty LEVEL =
            IntegerProperty.create("level", 0, 7);

    public FluxGooBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 7));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(LEVEL);
    }

    @Override
    public boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {
        return isReplaceableLevel(state.getValue(LEVEL));
    }

    public static boolean isReplaceableLevel(int level) {
        return level < 2;
    }

    /**
     * The block model is deliberately hidden. Its non-empty fluid state makes
     * Minecraft's liquid renderer draw the animated, sloped surface.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int amount = state.getValue(LEVEL) + 1;
        if (amount == FiniteFluxFlow.QUANTA_PER_BLOCK) {
            return ModFluids.FLUX_GOO_SOURCE.get().getSource(false);
        }
        return ModFluids.FLUX_GOO_FLOWING.get().getFlowing(amount, false);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public boolean canPlaceLiquid(
            BlockGetter level,
            BlockPos position,
            BlockState state,
            Fluid fluid
    ) {
        return fluid == Fluids.WATER;
    }

    @Override
    public boolean placeLiquid(
            LevelAccessor level,
            BlockPos position,
            BlockState state,
            FluidState fluidState
    ) {
        if (!FluxWaterInteraction.mayReplaceDirectly(
                state.getValue(LEVEL),
                fluidState
        )) {
            return false;
        }
        level.setBlock(position, Blocks.WATER.defaultBlockState(), 3);
        level.scheduleTick(
                position,
                Fluids.WATER,
                Fluids.WATER.getTickDelay(level)
        );
        return true;
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            Block neighbor,
            BlockPos neighborPosition,
            boolean moved
    ) {
        super.neighborChanged(
                state,
                level,
                position,
                neighbor,
                neighborPosition,
                moved
        );
        if (level instanceof ServerLevel serverLevel) {
            if (FluxWaterInteraction.washFromNeighbour(
                    serverLevel,
                    position,
                    state,
                    LEVEL
            )) {
                return;
            }
            FiniteFluxFlow.schedule(
                    serverLevel,
                    position,
                    this,
                    FLOW_TICK_DELAY
            );
        }
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState previousState,
            boolean moved
    ) {
        super.onPlace(state, level, position, previousState, moved);
        if (level instanceof ServerLevel serverLevel
                && !previousState.is(this)) {
            if (FluxWaterInteraction.washFromNeighbour(
                    serverLevel,
                    position,
                    state,
                    LEVEL
            )) {
                return;
            }
            FiniteFluxFlow.schedule(
                    serverLevel,
                    position,
                    this,
                    FLOW_TICK_DELAY
            );
        }
    }

    @Override
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        updateFlux(level, position, random);
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        int amount = state.getValue(LEVEL);
        if (entity instanceof LegacyThaumcraftMob slime
                && slime.kind() == LegacyMobKind.THAUMIC_SLIME) {
            if (!level.isClientSide
                    && slime.thaumicSlimeSize() < amount
                    && slime.tickCount % 20 == 0
                    && level.random.nextBoolean()) {
                slime.setThaumicSlimeSize(slime.thaumicSlimeSize() + 1);
                consumeOne(level, position, state);
            }
            return;
        }
        double drag = Math.max(0.0D, 1.0D - (amount + 1) / 8.0D);
        entity.setDeltaMovement(
                entity.getDeltaMovement().multiply(drag, 1.0D, drag)
        );
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    ModEffects.VIS_EXHAUST.get(),
                    600,
                    amount / 3,
                    true,
                    true
            ));
        }
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        updateFlux(level, position, random);
    }

    private void updateFlux(
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        BlockState initialState = level.getBlockState(position);
        if (!initialState.is(this)
                || FluxWaterInteraction.washFromNeighbour(
                        level,
                        position,
                        initialState,
                        LEVEL
                )) {
            return;
        }
        FiniteFluxFlow.tick(
                level,
                position,
                this,
                LEVEL,
                Direction.DOWN,
                DENSITY,
                FLOW_TICK_DELAY,
                random
        );
        BlockState state = level.getBlockState(position);
        if (!state.is(this)) {
            return;
        }
        int amount = state.getValue(LEVEL);
        boolean openAbove = level.isEmptyBlock(position.above());
        if (amount >= 2 && amount < 6
                && openAbove
                && random.nextInt(25) == 0) {
            level.removeBlock(position, false);
            spawnSlime(level, position, 1);
        } else if (amount >= 6 && openAbove) {
            if (random.nextInt(25) == 0) {
                level.removeBlock(position, false);
                spawnSlime(level, position, 2);
            } else if (ThaumcraftModernServerConfig.taintFromFlux()
                    && random.nextInt(50) == 0) {
                TaintBiomeService.taintColumn(level, position);
                level.removeBlock(position, false);
                TaintEcology.placeFibres(level, position);
            }
        } else if (random.nextInt(30) == 0) {
            consumeOne(level, position, state);
            if (amount > 0
                    && random.nextBoolean()
                    && level.isEmptyBlock(position.above())) {
                level.setBlock(
                        position.above(),
                        ModBlocks.FLUX_GAS.get().defaultBlockState(),
                        3
                );
            }
        }
    }

    private static void consumeOne(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        int amount = state.getValue(LEVEL);
        if (amount <= 0) {
            level.removeBlock(position, false);
        } else {
            level.setBlock(position, state.setValue(LEVEL, amount - 1), 3);
        }
    }

    private static void spawnSlime(
            ServerLevel level,
            BlockPos position,
            int size
    ) {
        LegacyThaumcraftMob slime =
                ModEntities.THAUMIC_SLIME.get().create(level);
        if (slime == null) {
            return;
        }
        slime.setThaumicSlimeSize(size);
        slime.moveTo(
                position.getX() + 0.5D,
                position.getY() + 0.1D,
                position.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        level.addFreshEntity(slime);
        level.playSound(
                null,
                position,
                com.thaumcraftmodern.registry.ModSounds.GORE.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );
    }
}
