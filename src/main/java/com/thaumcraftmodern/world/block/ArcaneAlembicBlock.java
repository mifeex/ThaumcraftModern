package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.essentia.ArcaneAlembicFacingRules;
import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class ArcaneAlembicBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 16, 15);

    public ArcaneAlembicBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING, ArcaneAlembicFacingRules.facingPlayer(
                        context.getHorizontalDirection()));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneAlembicBlockEntity alembic)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (WandApi.state(held).isPresent() && hit.getDirection().getAxis().isHorizontal()) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FACING, hit.getDirection()), UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        boolean hitLabelSlot = hit.getDirection() == state.getValue(FACING);
        if (player.isShiftKeyDown() && alembic.filterAspect() != null) {
            if (!level.isClientSide) alembic.setFilter(null);
            if (!level.isClientSide) {
                Direction facing = state.getValue(FACING);
                ItemEntity dropped = new ItemEntity(level,
                        pos.getX() + 0.5D + facing.getStepX() / 3.0D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D + facing.getStepZ() / 3.0D,
                        new ItemStack(ModItems.JAR_LABEL.get()));
                level.addFreshEntity(dropped);
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0F, 1.1F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (!level.isClientSide) {
                alembic.emptyContents();
                level.playSound(null, pos, SoundEvents.PLAYER_SWIM,
                        SoundSource.BLOCKS, 0.5F,
                        1.0F + (level.random.nextFloat()
                                - level.random.nextFloat()) * 0.3F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (hitLabelSlot && held.getItem() instanceof JarLabelItem
                && alembic.filterAspect() == null) {
            String aspect = alembic.storedAmount() > 0
                    ? alembic.storedAspect()
                    : JarLabelItem.aspect(held).orElse(null);
            if (aspect == null) return InteractionResult.sidedSuccess(level.isClientSide);
            if (!level.isClientSide) {
                alembic.setFilter(aspect);
                if (!(player instanceof ServerPlayer serverPlayer)
                        || !serverPlayer.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ArcaneAlembicBlockEntity alembic
                ? alembic.comparatorSignal() : 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneAlembicBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ArcaneAlembicBlockEntity alembic
                && alembic.filterAspect() != null && !level.isClientSide) {
            popResource(level, pos, new ItemStack(ModItems.JAR_LABEL.get()));
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
