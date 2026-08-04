package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.world.block.entity.DeconstructionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

/** Exact one-slot TC4 deconstruction-table container layout. */
public final class DeconstructionTableMenu extends AbstractContainerMenu {
    public static final int CLAIM_ASPECT_BUTTON = 1;
    private static final int TABLE_SLOT_COUNT = 1;

    private final Container table;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static DeconstructionTableMenu fromNetwork(
            int id,
            Inventory inventory,
            FriendlyByteBuf buffer
    ) {
        BlockPos position = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(position)
                instanceof DeconstructionTableBlockEntity table) {
            return new DeconstructionTableMenu(
                    id,
                    inventory,
                    table,
                    table.data()
            );
        }
        return new DeconstructionTableMenu(
                id,
                inventory,
                new SimpleContainer(TABLE_SLOT_COUNT),
                new SimpleContainerData(1)
        );
    }

    public DeconstructionTableMenu(
            int id,
            Inventory inventory,
            Container table,
            ContainerData data
    ) {
        super(ModMenus.DECONSTRUCTION_TABLE.get(), id);
        checkContainerSize(table, TABLE_SLOT_COUNT);
        checkContainerDataCount(data, 1);
        this.table = table;
        this.data = data;
        this.access = table instanceof DeconstructionTableBlockEntity entity
                ? ContainerLevelAccess.create(
                        entity.getLevel(),
                        entity.getBlockPos()
                )
                : ContainerLevelAccess.NULL;
        table.startOpen(inventory.player);

        addSlot(new Slot(table, 0, 64, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return table.canPlaceItem(0, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    inventory,
                    column,
                    8 + column * 18,
                    142
            ));
        }
        addDataSlots(data);
    }

    public int breakTimeScaled(int scale) {
        return data.get(0) * scale
                / com.thaumcraftmodern.deconstruction.DeconstructionTableLogic
                .BREAK_TICKS;
    }

    public String aspectId() {
        return table instanceof DeconstructionTableBlockEntity entity
                ? entity.aspectId()
                : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != CLAIM_ASPECT_BUTTON
                || !(player instanceof ServerPlayer serverPlayer)
                || !(table instanceof DeconstructionTableBlockEntity entity)) {
            return false;
        }
        String aspect = entity.aspectId();
        if (aspect == null) {
            return false;
        }
        return KnowledgeAccess.get(serverPlayer).map(knowledge -> {
            // Our return value means "newly learned", while TC4's call meant
            // "pool update accepted". Primals are already known, so always
            // credit the point and clear the table result.
            knowledge.addAspectPoints(aspect, 1);
            if (!entity.clearAspect(aspect)) {
                return false;
            }
            KnowledgeSync.send(serverPlayer, "deconstruction_table");
            return true;
        }).orElse(false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size()
                ? slots.get(index)
                : null;
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (table.canPlaceItem(0, live)) {
            if (!moveItemStackTo(live, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 28) {
            if (!moveItemStackTo(live, 28, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(live, 1, 28, false)) {
            return ItemStack.EMPTY;
        }
        if (live.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (live.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, live);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                access,
                player,
                ModBlocks.DECONSTRUCTION_TABLE.get()
        );
    }

    @Override
    public void removed(Player player) {
        table.stopOpen(player);
        super.removed(player);
    }
}
