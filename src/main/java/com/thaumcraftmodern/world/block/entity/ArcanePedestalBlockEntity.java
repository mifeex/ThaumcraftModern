package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ArcanePedestalBlockEntity extends BlockEntity
        implements WorldlyContainer {
    private static final int[] SLOT = {0};
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public ArcanePedestalBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ARCANE_PEDESTAL.get(), position, state);
    }

    public ItemStack item() {
        return items.get(0);
    }

    public void setInfusionItem(ItemStack stack) {
        setItem(0, stack);
        if (level != null && !level.isClientSide) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), 12, 0);
        }
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return item().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return slot == 0 ? item() : ItemStack.EMPTY; }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack removed = ContainerHelper.removeItem(items, 0, amount);
        if (!removed.isEmpty()) sync();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack removed = ContainerHelper.takeItem(items, 0);
        if (!removed.isEmpty()) sync();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        ItemStack stored = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!stored.isEmpty()) stored.setCount(1);
        items.set(0, stored);
        sync();
    }

    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }
    @Override public void clearContent() { items.set(0, ItemStack.EMPTY); sync(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0 && item().isEmpty();
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        if (!item().isEmpty()) item().setCount(1);
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }
}
