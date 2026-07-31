package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Replaceable, short-lived TC4 Flux Gas.
 */
public final class FluxGasBlock extends Block
        implements LiquidBlockContainer {
    public static final int DENSITY = -4;
    public static final int FLOW_TICK_DELAY = 12;
    public static final IntegerProperty LEVEL =
            IntegerProperty.create("level", 0, 7);

    public FluxGasBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
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
        return state.getValue(LEVEL) < 2;
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
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        flow(level, position, random);
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        if (level.isClientSide
                || !(entity instanceof LivingEntity living)
                || level.random.nextInt(10) != 0
                || living.getMobType()
                        == net.minecraft.world.entity.MobType.UNDEAD
                || living instanceof LegacyThaumcraftMob mob
                        && mob.kind().tainted()
                || living.hasEffect(ModEffects.VIS_EXHAUST.get())
                || living.hasEffect(MobEffects.CONFUSION)) {
            return;
        }
        int amount = state.getValue(LEVEL);
        if (level.random.nextBoolean()) {
            living.addEffect(new MobEffectInstance(
                    ModEffects.VIS_EXHAUST.get(),
                    1200,
                    amount / 3,
                    true,
                    true
            ));
        } else {
            living.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION,
                    80 + amount * 20,
                    0
            ));
        }
        consumeOne(level, position, state);
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        flow(level, position, random);
    }

    private void flow(
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        BlockState state = level.getBlockState(position);
        if (!state.is(this)
                || FluxWaterInteraction.washFromNeighbour(
                        level,
                        position,
                        state,
                        LEVEL
                )) {
            return;
        }
        FiniteFluxFlow.tick(
                level,
                position,
                this,
                LEVEL,
                Direction.UP,
                DENSITY,
                FLOW_TICK_DELAY,
                random
        );
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
}
