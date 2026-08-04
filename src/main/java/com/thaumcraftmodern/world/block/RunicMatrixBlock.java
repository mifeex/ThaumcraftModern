package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.RunicMatrixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class RunicMatrixBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RunicMatrixBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(ACTIVE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(ACTIVE)
                || WandApi.state(player.getItemInHand(hand)).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof RunicMatrixBlockEntity matrix)) {
            return InteractionResult.PASS;
        }
        RunicMatrixBlockEntity.StartResult result = matrix.startCrafting(serverPlayer);
        return result == RunicMatrixBlockEntity.StartResult.STARTED
                || result == RunicMatrixBlockEntity.StartResult.ALREADY_CRAFTING
                ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunicMatrixBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                ModBlockEntities.RUNIC_MATRIX.get(),
                RunicMatrixBlockEntity::serverTick
        );
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level instanceof net.minecraft.server.level.ServerLevel server) {
            CraftingStructureDisassembly.matrixRemoved(server, pos, state);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
