package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.item.HandMirrorItem;
import com.thaumcraftmodern.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Exact one-input-slot portable mirror layout; insertion transports immediately. */
public final class HandMirrorMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final int mirrorSlot;
    private boolean changing;
    private final SimpleContainer input = new SimpleContainer(1) {
        @Override public void setChanged() {
            super.setChanged();
            transportInput();
        }
    };

    public static HandMirrorMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        return new HandMirrorMenu(id, inventory, buffer.readEnum(InteractionHand.class),
                buffer.readVarInt());
    }

    public HandMirrorMenu(int id, Inventory inventory, InteractionHand hand,
            int mirrorSlot) {
        super(ModMenus.HAND_MIRROR.get(), id);
        this.playerInventory = inventory;
        this.hand = hand;
        this.mirrorSlot = mirrorSlot;
        addSlot(new Slot(input, 0, 80, 24) {
            @Override public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && !(stack.getItem() instanceof HandMirrorItem);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addPlayerSlot(column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            addPlayerSlot(column, 8 + column * 18, 142);
        }
    }

    private void addPlayerSlot(int index, int x, int y) {
        addSlot(new Slot(playerInventory, index, x, y) {
            @Override public boolean mayPickup(Player player) {
                return index != mirrorSlot;
            }
            @Override public boolean mayPlace(ItemStack stack) {
                return index != mirrorSlot;
            }
        });
    }

    private ItemStack mirror() {
        return hand == InteractionHand.OFF_HAND
                ? playerInventory.player.getOffhandItem()
                : mirrorSlot >= 0 && mirrorSlot < playerInventory.items.size()
                        ? playerInventory.items.get(mirrorSlot) : ItemStack.EMPTY;
    }

    private void transportInput() {
        if (changing || !(playerInventory.player instanceof ServerPlayer player)) return;
        ItemStack stack = input.getItem(0);
        if (stack.isEmpty()) return;
        ItemStack mirror = mirror();
        if (mirror.getItem() instanceof HandMirrorItem
                && HandMirrorItem.transport(mirror, stack, player)) {
            changing = true;
            input.setItem(0, ItemStack.EMPTY);
            changing = false;
            broadcastChanges();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!(live.getItem() instanceof HandMirrorItem)) {
            if (!moveItemStackTo(live, 0, 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (live.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!input.getItem(0).isEmpty()) clearContainer(player, input);
    }

    @Override public boolean stillValid(Player player) {
        return player == playerInventory.player && player.isAlive()
                && mirror().getItem() instanceof HandMirrorItem;
    }
}
