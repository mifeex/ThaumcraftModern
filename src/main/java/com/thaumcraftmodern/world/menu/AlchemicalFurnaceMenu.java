package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AlchemicalFurnaceMenu extends AbstractContainerMenu {
    private final Container furnace;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static AlchemicalFurnaceMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos)
                instanceof AlchemicalFurnaceBlockEntity furnace) {
            return new AlchemicalFurnaceMenu(id, inventory, furnace, furnace.data());
        }
        return new AlchemicalFurnaceMenu(id, inventory,
                new SimpleContainer(2), new SimpleContainerData(5));
    }

    public AlchemicalFurnaceMenu(int id, Inventory inventory,
            Container furnace, ContainerData data) {
        super(ModMenus.ALCHEMICAL_FURNACE.get(), id);
        checkContainerSize(furnace, 2);
        checkContainerDataCount(data, 5);
        this.furnace = furnace;
        this.data = data;
        this.access = furnace instanceof AlchemicalFurnaceBlockEntity entity
                ? ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos())
                : ContainerLevelAccess.NULL;
        furnace.startOpen(inventory.player);
        addSlot(new Slot(furnace, 0, 80, 8) {
            @Override public boolean mayPlace(ItemStack stack) {
                return furnace.canPlaceItem(0, stack);
            }
        });
        addSlot(new Slot(furnace, 1, 80, 48) {
            @Override public boolean mayPlace(ItemStack stack) {
                return furnace.canPlaceItem(1, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public int burnScaled(int height) {
        int total = data.get(1);
        return total <= 0 ? 0 : data.get(0) * height / total;
    }

    public int cookScaled(int height) {
        int total = data.get(3);
        return total <= 0 ? 0 : data.get(2) * height / total;
    }

    public int contentsScaled(int height) {
        return data.get(4) * height / AlchemicalFurnaceBlockEntity.MAX_ESSENTIA;
    }

    public boolean burning() { return data.get(0) > 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index < 2) {
            if (!moveItemStackTo(live, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (furnace.canPlaceItem(0, live)) {
            if (!moveItemStackTo(live, 0, 1, false)) return ItemStack.EMPTY;
        } else if (furnace.canPlaceItem(1, live)) {
            if (!moveItemStackTo(live, 1, 2, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            if (!moveItemStackTo(live, 29, 38, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(live, 2, 29, false)) {
            return ItemStack.EMPTY;
        }
        if (live.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (live.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, live);
        return original;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ALCHEMICAL_FURNACE.get());
    }

    @Override public void removed(Player player) {
        furnace.stopOpen(player);
        super.removed(player);
    }
}
