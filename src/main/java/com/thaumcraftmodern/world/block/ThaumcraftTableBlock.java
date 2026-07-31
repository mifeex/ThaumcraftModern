package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The plain TC4 table. A non-staff wand converts it into the Arcane Workbench
 * and becomes the workbench's installed wand without consuming vis.
 */
public final class ThaumcraftTableBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 12, 0, 16, 16, 16),
            box(2, 4, 6, 6, 12, 10),
            box(10, 4, 6, 14, 12, 10),
            box(0, 0, 4, 16, 4, 12)
    );

    public ThaumcraftTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        ItemStack held = player.getItemInHand(hand);
        if (!WandVisService.isWand(held)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack installedWand = held.copy();
        installedWand.setCount(1);
        if (!level.setBlock(position, ModBlocks.ARCANE_WORKBENCH.get().defaultBlockState(), UPDATE_ALL)) {
            return InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(position) instanceof ArcaneWorkbenchBlockEntity workbench)) {
            level.setBlock(position, state, UPDATE_ALL);
            return InteractionResult.FAIL;
        }

        workbench.wand().setItem(0, installedWand);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(
                null,
                position,
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.BLOCKS,
                0.15F,
                0.5F
        );
        return InteractionResult.CONSUME;
    }
}
