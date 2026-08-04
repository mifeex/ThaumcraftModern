package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.WandRechargePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** TC4 blockStoneDevice:5, kept separate from the infusion pedestal. */
public final class WandRechargePedestalBlock extends BaseEntityBlock {
    private static final VoxelShape OUTLINE = box(4, 0, 4, 12, 16, 12);

    public WandRechargePedestalBlock(Properties properties) { super(properties); }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) { return OUTLINE; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) { return Shapes.block(); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return interact(level, pos, player, hand);
    }

    public InteractionResult interact(Level level, BlockPos pos,
            Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof WandRechargePedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!pedestal.item().isEmpty()) {
            ItemStack removed = pedestal.removeItemNoUpdate(0);
            if (!player.getInventory().add(removed)) player.drop(removed, false);
            playPickup(level, pos, 1.5F);
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!WandVisService.isWand(held)) return InteractionResult.PASS;
        ItemStack placed = held.copy();
        placed.setCount(1);
        pedestal.setItem(0, placed);
        if (!player.getAbilities().instabuild) held.shrink(1);
        playPickup(level, pos, 1.6F);
        return InteractionResult.CONSUME;
    }

    private static void playPickup(Level level, BlockPos pos, float pitchScale) {
        float pitch = ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F)
                * pitchScale;
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof WandRechargePedestalBlockEntity pedestal) {
            Containers.dropContents(level, pos, pedestal);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof WandRechargePedestalBlockEntity pedestal)) return 0;
        return pedestal.comparatorLevel();
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WandRechargePedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.WAND_RECHARGE_PEDESTAL.get(),
                        WandRechargePedestalBlockEntity::serverTick)
                : null;
    }
}
