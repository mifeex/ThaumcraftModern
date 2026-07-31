package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.EtherealBloomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * TC4 Ethereal Bloom with its server-side biome purification ticker.
 */
public final class EtherealBloomBlock extends FlowerBlock
        implements EntityBlock {
    public EtherealBloomBlock(
            MobEffect effect,
            int duration,
            Properties properties
    ) {
        super(effect, duration, properties);
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new EtherealBloomBlockEntity(position, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (type != ModBlockEntities.ETHEREAL_BLOOM.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (clientLevel, position, blockState, blockEntity) ->
                    EtherealBloomBlockEntity.clientTick(
                            clientLevel,
                            position,
                            blockState,
                            (EtherealBloomBlockEntity) blockEntity
                    );
        }
        return (serverLevel, position, blockState, blockEntity) ->
                    EtherealBloomBlockEntity.serverTick(
                            (ServerLevel) serverLevel,
                            position,
                            blockState,
                            (EtherealBloomBlockEntity) blockEntity
                    );
    }
}
