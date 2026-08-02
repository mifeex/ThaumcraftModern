package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.EssentiaCentrifugeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class EssentiaCentrifugeBlock extends BaseEntityBlock {
    private static final VoxelShape CLASSIC_TUBE_SHAPE = box(4, 4, 4, 12, 12, 12);

    public EssentiaCentrifugeBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_TUBE_SHAPE;
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_TUBE_SHAPE;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EssentiaCentrifugeBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ESSENTIA_CENTRIFUGE.get(), EssentiaCentrifugeBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.ESSENTIA_CENTRIFUGE.get(), EssentiaCentrifugeBlockEntity::clientTick);
    }
}
