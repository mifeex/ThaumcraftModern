package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Seven modern registry identities for TC4 BlockCrystal metadata 0..6. */
public final class CrystalClusterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private final CrystalClusterVariant variant;

    public CrystalClusterBlock(
            CrystalClusterVariant variant,
            Properties properties
    ) {
        super(properties);
        this.variant = variant;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public CrystalClusterVariant variant() {
        return variant;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(
                FACING,
                context.getClickedFace()
        );
        return state.canSurvive(context.getLevel(), context.getClickedPos())
                ? state
                : null;
    }

    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos position
    ) {
        Direction facing = state.getValue(FACING);
        BlockPos support = position.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(
                level,
                support,
                facing
        );
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level,
            BlockPos position, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level,
            BlockPos position) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level,
            BlockPos position) {
        return 1.0F;
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
        if (!state.canSurvive(level, position)) {
            level.destroyBlock(position, true);
            return;
        }
        super.neighborChanged(
                state,
                level,
                position,
                neighbor,
                neighborPosition,
                movedByPiston
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder builder
    ) {
        List<ItemStack> drops = new ArrayList<>();
        if (variant == CrystalClusterVariant.BALANCED) {
            drops.add(new ItemStack(ModItems.AIR_SHARD.get()));
            drops.add(new ItemStack(ModItems.FIRE_SHARD.get()));
            drops.add(new ItemStack(ModItems.WATER_SHARD.get()));
            drops.add(new ItemStack(ModItems.EARTH_SHARD.get()));
            drops.add(new ItemStack(ModItems.ORDER_SHARD.get()));
            drops.add(new ItemStack(ModItems.ENTROPY_SHARD.get()));
        } else {
            drops.add(new ItemStack(shardForVariant(), 6));
        }
        return drops;
    }

    private net.minecraft.world.item.Item shardForVariant() {
        return switch (variant) {
            case AIR -> ModItems.AIR_SHARD.get();
            case FIRE -> ModItems.FIRE_SHARD.get();
            case WATER -> ModItems.WATER_SHARD.get();
            case EARTH -> ModItems.EARTH_SHARD.get();
            case ORDER -> ModItems.ORDER_SHARD.get();
            case ENTROPY -> ModItems.ENTROPY_SHARD.get();
            case BALANCED -> throw new IllegalStateException(
                    "Balanced cluster has six distinct drops"
            );
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new CrystalClusterBlockEntity(position, state);
    }
}
