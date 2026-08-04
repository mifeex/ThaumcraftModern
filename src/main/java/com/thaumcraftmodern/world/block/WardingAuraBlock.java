package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.WardingAuraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Invisible TC4 warding-fence segment projected above a warding stone. */
public final class WardingAuraBlock extends BaseEntityBlock {
    public WardingAuraBlock(Properties properties) {
        super(properties);
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
        return new WardingAuraBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        type,
                        ModBlockEntities.WARDING_AURA.get(),
                        WardingAuraBlockEntity::serverTick
                );
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
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return Shapes.empty();
        }
        Entity entity = entityContext.getEntity();
        if (!(entity instanceof LivingEntity)
                || entity instanceof Player
                || !hasActiveSupport(level, position)) {
            return Shapes.empty();
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getOcclusionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return true;
    }

    @Override
    public float getShadeBrightness(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return 1.0F;
    }

    public static boolean hasAuraSpace(LevelReader level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.is(ModBlocks.WARDING_AURA.get()) || state.canBeReplaced();
    }

    public static boolean hasActiveSupport(
            BlockGetter level,
            BlockPos position
    ) {
        for (int offset = 1; offset <= 2; offset++) {
            BlockPos basePosition = position.below(offset);
            if (level.getBlockState(basePosition).is(
                    ModBlocks.PAVING_STONE_OF_WARDING.get()
            )) {
                return !(level instanceof Level wardingLevel)
                        || !wardingLevel.hasNeighborSignal(basePosition);
            }
        }
        return false;
    }
}
