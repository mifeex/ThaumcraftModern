package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class VisRelayBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            DirectionProperty.create("facing");
    private final boolean charger;

    public VisRelayBlock(boolean charger, Properties properties) {
        super(properties);
        this.charger = charger;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                charger ? Direction.UP : context.getClickedFace()
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        if (charger) {
            return chargerCrystalShape();
        }
        return relayCrystalShape(state.getValue(FACING));
    }

    static VoxelShape chargerCrystalShape() {
        // Only the relay crystal is selectable. The workbench and the four
        // rendered supports belong visually below this block.
        return box(5, 8, 5, 11, 16, 11);
    }

    static VoxelShape relayCrystalShape(Direction facing) {
        return switch (facing) {
            case DOWN -> box(5, 8, 5, 11, 16, 11);
            case UP -> box(5, 0, 5, 11, 8, 11);
            case NORTH -> box(5, 5, 8, 11, 11, 16);
            case SOUTH -> box(5, 5, 0, 11, 11, 8);
            case WEST -> box(8, 5, 5, 16, 11, 11);
            case EAST -> box(0, 5, 5, 8, 11, 11);
        };
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!WandVisService.isWand(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(position)
                instanceof VisRelayBlockEntity relay) {
            relay.cycleAttunement();
            level.playSound(
                    null,
                    position,
                    ModSounds.CRYSTAL.get(),
                    SoundSource.BLOCKS,
                    0.2F,
                    1.0F
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return charger
                ? new VisChargeRelayBlockEntity(position, state)
                : new VisRelayBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return charger
                    ? createTickerHelper(type,
                    ModBlockEntities.VIS_CHARGE_RELAY.get(),
                    VisNetworkNodeBlockEntity::clientTick)
                    : createTickerHelper(type,
                    ModBlockEntities.VIS_RELAY.get(),
                    VisNetworkNodeBlockEntity::clientTick);
        }
        return charger
                ? createTickerHelper(type, ModBlockEntities.VIS_CHARGE_RELAY.get(),
                (world, position, blockState, node) ->
                        VisNetworkNodeBlockEntity.serverTick(
                                (ServerLevel) world, position, blockState, node))
                : createTickerHelper(type, ModBlockEntities.VIS_RELAY.get(),
                (world, position, blockState, node) ->
                        VisNetworkNodeBlockEntity.serverTick(
                                (ServerLevel) world, position, blockState, node));
    }
}
