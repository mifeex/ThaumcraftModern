package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.item.MirrorBlockItem;
import com.thaumcraftmodern.mirror.LinkedMirrorBlockEntity;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import com.thaumcraftmodern.world.block.entity.MagicMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Six-face wall/ceiling/floor mirror shell shared by both TC4 variants. */
public final class MagicMirrorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final double WIDTH = 1.0D;
    private final boolean essentia;

    public MagicMirrorBlock(Properties properties, boolean essentia) {
        super(properties);
        this.essentia = essentia;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean essentia() { return essentia; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return canSupport(context.getLevel(), context.getClickedPos(), face)
                ? defaultBlockState().setValue(FACING, face) : null;
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level,
            BlockPos pos) {
        return canSupport(level, pos, state.getValue(FACING));
    }

    private static boolean canSupport(net.minecraft.world.level.LevelReader level,
            BlockPos pos, Direction outward) {
        return level.getBlockState(pos.relative(outward.getOpposite()))
                .isFaceSturdy(level, pos.relative(outward.getOpposite()), outward);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction,
            BlockState neighbour, net.minecraft.world.level.LevelAccessor level,
            BlockPos pos, BlockPos neighbourPos) {
        return direction == state.getValue(FACING).getOpposite()
                && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,
            BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        double w = WIDTH;
        return switch (state.getValue(FACING)) {
            case UP -> box(0, 0, 0, 16, w, 16);
            case DOWN -> box(0, 16 - w, 0, 16, 16, 16);
            case NORTH -> box(0, 0, 16 - w, 16, 16, 16);
            case SOUTH -> box(0, 0, 0, 16, 16, w);
            case WEST -> box(16 - w, 0, 0, 16, 16, 16);
            case EAST -> box(0, 0, 0, w, 16, 16);
        };
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) { return Shapes.empty(); }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!essentia && !level.isClientSide && entity instanceof ItemEntity item
                && item.isAlive()
                && level.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mirror) {
            mirror.transport(item);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity mirror) {
            mirror.invalidatePair();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack drop = new ItemStack(essentia
                ? ModItems.ESSENTIA_MIRROR.get() : ModItems.MAGIC_MIRROR.get());
        BlockEntity entity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof LinkedMirrorBlockEntity mirror && mirror.linked()) {
            drop.addTagElement(MirrorBlockItem.BLOCK_ENTITY_TAG,
                    mirror.saveLinkForItem());
        }
        return List.of(drop);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return essentia ? new EssentiaMirrorBlockEntity(pos, state)
                : new MagicMirrorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel)) return null;
        return essentia
                ? createTickerHelper(type, ModBlockEntities.ESSENTIA_MIRROR.get(),
                        EssentiaMirrorBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.MAGIC_MIRROR.get(),
                        MagicMirrorBlockEntity::serverTick);
    }
}
