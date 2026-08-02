package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class EssentiaBufferBlock extends BaseEntityBlock {
    private static final VoxelShape CLASSIC_TUBE_SHAPE = box(4, 4, 4, 12, 12, 12);

    public EssentiaBufferBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_TUBE_SHAPE;
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return CLASSIC_TUBE_SHAPE;
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (WandApi.state(player.getItemInHand(hand)).isEmpty()
                || !(level.getBlockEntity(pos) instanceof EssentiaBufferBlockEntity buffer)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) buffer.cycleChoke(hit.getDirection());
            else buffer.toggleSide(hit.getDirection());
            level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS,
                    0.5F, 0.9F + level.random.nextFloat() * 0.2F);
        }
        player.swing(hand, true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EssentiaBufferBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel ? createTickerHelper(type, ModBlockEntities.ESSENTIA_BUFFER.get(), EssentiaBufferBlockEntity::serverTick) : null;
    }
}
