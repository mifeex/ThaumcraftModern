package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.item.WardedJarItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EssentiaJarBlock extends BaseEntityBlock {
    /** Slightly reduced from TC4's 3..13 by 12 outline for modern perspective. */
    private static final VoxelShape SHAPE = box(3.5, 0, 3.5, 12.5, 11.5, 12.5);

    public EssentiaJarBlock(Properties properties) {
        super(properties);
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean hitFilter = hit.getDirection() == jar.filterFacing();
        if (player.isShiftKeyDown() && held.isEmpty()
                && jar.filter() != null && hitFilter) {
            if (!level.isClientSide) {
                Direction facing = jar.filterFacing();
                jar.setFilter(null, facing);
                level.addFreshEntity(new ItemEntity(level,
                        pos.getX() + 0.5D + facing.getStepX() / 3.0D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D + facing.getStepZ() / 3.0D,
                        new ItemStack(ModItems.JAR_LABEL.get())));
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (!level.isClientSide) {
                jar.emptyContents();
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY,
                        SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.getItem() instanceof JarLabelItem
                && jar.filter() == null) {
            String aspect = jar.amount() > 0
                    ? jar.aspect() : JarLabelItem.aspect(held).orElse(null);
            if (aspect == null) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                jar.setFilter(aspect, player.getDirection().getOpposite());
                if (!(player instanceof ServerPlayer serverPlayer)
                        || !serverPlayer.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack drop = new ItemStack(ModItems.WARDED_JAR.get());
        BlockEntity entity = builder.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY);
        if (entity instanceof EssentiaJarBlockEntity jar) {
            // TC4 uses the ordinary empty jar stack only when there is neither
            // essentia nor a label; filled/labeled jars carry their payload.
            if (jar.amount() > 0 || jar.filter() != null) {
                drop = WardedJarItem.withContents(
                        (WardedJarItem) (jar.amount() > 0
                                ? ModItems.FILLED_WARDED_JAR.get()
                                : ModItems.WARDED_JAR.get()),
                        jar.saveForItem());
            }
        }
        return List.of(drop);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar
                ? jar.comparatorSignal() : 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaJarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ESSENTIA_JAR.get(),
                        EssentiaJarBlockEntity::serverTick)
                : null;
    }
}
