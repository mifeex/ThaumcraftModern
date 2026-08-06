package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** TC4 golem configuration menu: six material-framed ghost filters plus core toggles. */
public final class GolemMenu extends AbstractContainerMenu {
    private static final int VISIBLE_FILTERS = 6;
    public static final int FILTER_BUTTON_BASE = 100;
    private final ClassicGolemEntity golem;
    private final int filterSlots;
    private final int visibleFilters;
    private final DataSlot page = DataSlot.standalone();

    public GolemMenu(int containerId, Inventory playerInventory, ClassicGolemEntity golem) {
        super(ModMenus.GOLEM.get(), containerId);
        this.golem = golem;
        if (golem != null) golem.ensureConfigurationInventories();
        this.filterSlots = golem != null && golem.hasCoreInventory()
                ? golem.filters().getContainerSize() : 0;
        this.visibleFilters = Math.min(VISIBLE_FILTERS, filterSlots);
        for (int view = 0; view < visibleFilters; view++) {
            // These slots exist only so vanilla synchronizes the filter ItemStacks to the client.
            // They must never participate in hit-testing or normal container click handling: a
            // filter is an icon copied by clickMenuButton, not an inventory that owns player items.
            addSlot(new Slot(golem.filters(), view, -10_000, -10_000) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public int getMaxStackSize() { return 1; }
                @Override public boolean isActive() { return false; }
                @Override public boolean isHighlightable() { return false; }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        addDataSlot(page);
        if (golem != null) {
            golem.filters().startOpen(playerInventory.player);
            golem.setMenuPaused(true);
        }
    }

    public static GolemMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        Entity entity = inventory.player.level().getEntity(buffer.readVarInt());
        return new GolemMenu(containerId, inventory,
                entity instanceof ClassicGolemEntity golem ? golem : null);
    }

    public ClassicGolemEntity golem() { return golem; }
    public int filterSlots() { return filterSlots; }
    public int visibleFilters() { return visibleFilters; }
    public int page() { return page.get(); }
    public int maxPage() { return Math.max(0, (filterSlots - 1) / VISIBLE_FILTERS); }

    /** Defensive view of the synchronized icon; callers can never mutate the filter container. */
    public ItemStack filterIcon(int view) {
        if (view < 0 || view >= visibleFilters) return ItemStack.EMPTY;
        refreshPageIndices();
        int filter = page() * VISIBLE_FILTERS + view;
        return filter < filterSlots ? slots.get(view).getItem().copy() : ItemStack.EMPTY;
    }

    /** Slot.index is mutable in 1.20; keep the six views pointed at the synchronized page. */
    public void refreshPageIndices() {
        int offset = page() * VISIBLE_FILTERS;
        for (int view = 0; view < visibleFilters; view++) slots.get(view).index = offset + view;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (golem == null) return false;
        if (id >= FILTER_BUTTON_BASE && id < FILTER_BUTTON_BASE + filterSlots) {
            int filter = id - FILTER_BUTTON_BASE;
            ItemStack cursor = getCarried();
            golem.filters().setItem(filter, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copyWithCount(1));
            golem.filters().setChanged();
        } else if (id == 66 && page() > 0) page.set(page() - 1);
        else if (id == 67 && page() < maxPage()) page.set(page() + 1);
        else if (id >= 50 && id <= 57) golem.setToggle(id - 50, !golem.toggle(id - 50));
        else if (golem.upgradeAmount(com.thaumcraftmodern.entity.GolemUpgradeType.ORDO) > 0
                && id >= 0 && id < filterSlots) {
            int color = golem.filterColor(id) - 1;
            golem.setFilterColor(id, color < -1 ? 15 : color);
        } else if (golem.upgradeAmount(com.thaumcraftmodern.entity.GolemUpgradeType.ORDO) > 0
                && id >= filterSlots && id < filterSlots * 2) {
            int slot = id - filterSlots;
            int color = golem.filterColor(slot) + 1;
            golem.setFilterColor(slot, color > 15 ? -1 : color);
        }
        else return false;
        refreshPageIndices();
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), .2F, .8F);
        return true;
    }

    @Override
    public void broadcastChanges() {
        refreshPageIndices();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return golem != null && golem.isAlive() && player.distanceToSqr(golem) <= 64D;
    }

    /** Original ghost slots deliberately disable shift-transfer. */
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (golem != null) {
            golem.filters().stopOpen(player);
            golem.setMenuPaused(false);
        }
    }
}
