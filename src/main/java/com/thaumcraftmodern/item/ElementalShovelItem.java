package com.thaumcraftmodern.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public final class ElementalShovelItem extends ShovelItem {
    private static final String ORIENTATION = "or";
    private static final String BREAK_FACE = "tc4BreakFace";
    private static final String AREA_BREAKING = "tc4AreaBreaking";

    public ElementalShovelItem(Properties properties) {
        super(ElementalTier.INSTANCE, 1.5F, -3.0F, properties);
    }

    public static int orientation(ItemStack stack) {
        return Math.floorMod(stack.getOrCreateTag().getByte(ORIENTATION), 3);
    }

    public static int cycleOrientation(ItemStack stack) {
        int next = (orientation(stack) + 1) % 3;
        stack.getOrCreateTag().putByte(ORIENTATION, (byte) next);
        return next;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return super.useOn(context);
        Level level = context.getLevel();
        if (level.getBlockEntity(context.getClickedPos()) != null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockState copied = level.getBlockState(context.getClickedPos());
        BlockPos normal = context.getClickedPos().relative(context.getClickedFace());
        boolean placed = false;
        for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
            BlockPos target = normal.offset(planeOffset(a, b, context.getClickedFace(),
                    orientation(context.getItemInHand()), player));
            if (!level.mayInteract(player, target) || !player.mayUseItemAt(target,
                    context.getClickedFace(), context.getItemInHand()) || !canReplace(level, target)) continue;
            BlockState state = copied;
            if (!consumeBlock(player, state.getBlock())) {
                if (!copied.is(Blocks.GRASS_BLOCK) || !consumeBlock(player, Blocks.DIRT)) continue;
                state = Blocks.DIRT.defaultBlockState();
            }
            if (level.setBlock(target, state, Block.UPDATE_ALL)) {
                placed = true;
                level.playSound(null, target, state.getSoundType(level, target, player).getPlaceSound(),
                        SoundSource.BLOCKS, (state.getSoundType(level, target, player).getVolume() + 1.0F) / 2.0F,
                        state.getSoundType(level, target, player).getPitch() * 0.8F);
                context.getItemInHand().hurtAndBreak(1, player,
                        broken -> broken.broadcastBreakEvent(context.getHand()));
            }
        }
        return placed ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos position, Player player) {
        if (!stack.getOrCreateTag().getBoolean(AREA_BREAKING)) {
            BlockHitResult hit = getPlayerPOVHitResult(player.level(), player, net.minecraft.world.level.ClipContext.Fluid.NONE);
            stack.getOrCreateTag().putByte(BREAK_FACE, (byte) hit.getDirection().get3DDataValue());
        }
        return super.onBlockStartBreak(stack, position, player);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos position, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, position, miner);
        if (miner.isShiftKeyDown() || stack.getOrCreateTag().getBoolean(AREA_BREAKING)
                || !(miner instanceof ServerPlayer player) || !isShovelEffective(state)) return result;
        Direction face = Direction.from3DDataValue(stack.getOrCreateTag().getByte(BREAK_FACE));
        stack.getOrCreateTag().putBoolean(AREA_BREAKING, true);
        try {
            for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
                BlockPos target = position.offset(defaultPlaneOffset(a, b, face));
                if (target.equals(position)) continue;
                BlockState targetState = level.getBlockState(target);
                if (targetState.getDestroySpeed(level, target) >= 0.0F && isShovelEffective(targetState)) {
                    player.gameMode.destroyBlock(target);
                }
            }
        } finally {
            stack.getOrCreateTag().remove(AREA_BREAKING);
        }
        return result;
    }

    private static boolean canReplace(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.canBeReplaced() || state.getFluidState().is(Fluids.WATER)
                || state.is(Blocks.FIRE) || state.is(Blocks.VINE);
    }

    private static boolean consumeBlock(Player player, Block block) {
        if (player.getAbilities().instabuild) return true;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.getItem() instanceof BlockItem item && item.getBlock() == block) {
                candidate.shrink(1);
                return true;
            }
        }
        return false;
    }

    static BlockPos planeOffset(int a, int b, Direction face, int orientation, Player player) {
        if (orientation == 0) return defaultPlaneOffset(a, b, face);
        if (orientation == 1) {
            if (face.getAxis().isVertical()) {
                Direction horizontal = player.getDirection();
                return horizontal.getAxis() == Direction.Axis.Z ? new BlockPos(a, b, 0) : new BlockPos(0, b, a);
            }
            return face.getAxis() == Direction.Axis.Z ? new BlockPos(a, b, 0) : new BlockPos(0, b, a);
        }
        if (face.getAxis().isVertical()) {
            Direction horizontal = player.getDirection();
            return horizontal.getAxis() == Direction.Axis.Z ? new BlockPos(a, b, 0) : new BlockPos(0, b, a);
        }
        return new BlockPos(a, 0, b);
    }

    private static BlockPos defaultPlaneOffset(int a, int b, Direction face) {
        return switch (face.getAxis()) {
            case Y -> new BlockPos(a, 0, b);
            case Z -> new BlockPos(a, b, 0);
            case X -> new BlockPos(0, b, a);
        };
    }

    private static boolean isShovelEffective(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }
}
