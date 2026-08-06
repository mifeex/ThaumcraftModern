package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ItemGrateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Stateless virtual inventory used by hoppers and pipes above an open grate. */
public final class ItemGrateBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int[] SLOT = {0};
    private LazyOptional<IItemHandler> handler = createHandler();

    public ItemGrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_GRATE.get(), pos, state);
    }

    private boolean open() {
        return getBlockState().hasProperty(ItemGrateBlock.OPEN)
                && getBlockState().getValue(ItemGrateBlock.OPEN);
    }

    private void eject(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty() || !open()) return;
        ItemEntity item = new ItemEntity(level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.6D,
                worldPosition.getZ() + 0.5D,
                stack.copy());
        item.setDeltaMovement(0.0D, -0.1D, 0.0D);
        level.addFreshEntity(item);
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return true; }
    @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) { eject(stack); }
    @Override public boolean stillValid(Player player) { return false; }
    @Override public void clearContent() { }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack,
            @Nullable Direction side) { return side == Direction.UP && open(); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack,
            Direction side) { return false; }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side == Direction.UP) {
            return handler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); handler.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); handler = createHandler(); }

    private LazyOptional<IItemHandler> createHandler() {
        return LazyOptional.of(GrateHandler::new);
    }

    private final class GrateHandler implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !open()) return stack;
            if (!simulate) eject(stack);
            return ItemStack.EMPTY;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && open();
        }
    }
}
