package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class NodeDeviceBlock extends BaseEntityBlock {
    public enum Kind { STABILIZER, ADVANCED_STABILIZER, TRANSDUCER }

    private final Kind kind;

    public NodeDeviceBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
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
        return kind == Kind.TRANSDUCER
                ? new NodeTransducerBlockEntity(position, state)
                : new NodeStabilizerBlockEntity(position, state);
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            net.minecraft.world.level.block.Block neighbor,
            BlockPos neighborPosition,
            boolean movedByPiston
    ) {
        super.neighborChanged(
                state, level, position, neighbor, neighborPosition, movedByPiston);
        if (!level.isClientSide && kind == Kind.TRANSDUCER
                && level.getBlockEntity(position)
                instanceof NodeTransducerBlockEntity transducer) {
            transducer.checkStatus();
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (kind == Kind.TRANSDUCER) {
            return level.isClientSide
                    ? createTickerHelper(type, ModBlockEntities.NODE_TRANSDUCER.get(),
                    NodeTransducerBlockEntity::clientTick)
                    : createTickerHelper(type, ModBlockEntities.NODE_TRANSDUCER.get(),
                    (world, pos, blockState, tile) ->
                            NodeTransducerBlockEntity.serverTick(
                                    (ServerLevel) world, pos, blockState, tile));
        }
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.NODE_STABILIZER.get(),
                NodeStabilizerBlockEntity::clientTick)
                : null;
    }
}
