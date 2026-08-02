package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ThaumatoriumMenu extends AbstractContainerMenu {
    private final Container container;
    private final Inventory inventory;
    private final ContainerLevelAccess access;
    private final ThaumatoriumBlockEntity machine;
    private final List<String> aspectIds;
    private final int[] synchronizedReserved;
    private final ContainerData reservedData;

    public static ThaumatoriumMenu fromNetwork(int id, Inventory inventory,
            FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos)
                instanceof ThaumatoriumBlockEntity machine) {
            return new ThaumatoriumMenu(id, inventory, machine);
        }
        return new ThaumatoriumMenu(id, inventory, new SimpleContainer(1), null);
    }

    public ThaumatoriumMenu(int id, Inventory inventory,
            ThaumatoriumBlockEntity machine) {
        this(id, inventory, machine, machine);
    }

    private ThaumatoriumMenu(int id, Inventory inventory,
            Container container, ThaumatoriumBlockEntity machine) {
        super(ModMenus.THAUMATORIUM.get(), id);
        this.container = container;
        this.inventory = inventory;
        this.machine = machine;
        this.aspectIds = AspectRegistryRuntime.catalog().definitions().stream()
                .map(definition -> definition.id())
                .sorted()
                .toList();
        this.synchronizedReserved = new int[aspectIds.size()];
        this.reservedData = new ContainerData() {
            @Override public int get(int index) {
                if (!inventory.player.level().isClientSide && machine != null) {
                    return machine.reservedEssentia()
                            .getOrDefault(aspectIds.get(index), 0);
                }
                return synchronizedReserved[index];
            }

            @Override public void set(int index, int value) {
                synchronizedReserved[index] = Math.max(0, value);
            }

            @Override public int getCount() {
                return aspectIds.size();
            }
        };
        this.access = machine == null ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos());
        checkContainerSize(container, 1);
        container.startOpen(inventory.player);
        addSlot(new Slot(container, 0, 48, 16));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9,
                    8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(reservedData);
    }

    public List<CrucibleRecipeDefinition> recipes() {
        return machine == null ? List.of() : machine.availableRecipes(inventory.player);
    }

    public boolean selected(CrucibleRecipeDefinition recipe) {
        return machine != null && machine.hasFormula(recipe.id());
    }

    public ResourceLocation displayedRecipeId() {
        return machine == null ? null : machine.displayedRecipe();
    }

    public int formulaCount() { return machine == null ? 0 : machine.formulaCount(); }
    public int formulaCapacity() { return machine == null ? 1 : machine.formulaCapacity(); }
    public int reservedAmount(String aspect) {
        int index = aspectIds.indexOf(aspect);
        if (index < 0) {
            return 0;
        }
        int menuSnapshot = reservedData.get(index);
        if (inventory.player.level().isClientSide && machine != null) {
            return Math.max(
                    menuSnapshot,
                    machine.reservedEssentia().getOrDefault(aspect, 0)
            );
        }
        return menuSnapshot;
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        List<CrucibleRecipeDefinition> recipes = recipes();
        return machine != null && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && id >= 0 && id < recipes.size()
                && (machine.hasFormula(recipes.get(id).id())
                        || machine.formulaCount() < machine.formulaCapacity())
                && machine.selectRecipe(serverPlayer, recipes.get(id).id());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();
        if (index == 0) {
            if (!moveItemStackTo(live, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(live, 0, 1, false)) {
            if (index < 28) {
                if (!moveItemStackTo(live, 28, 37, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(live, 1, 28, false)) return ItemStack.EMPTY;
        }
        if (live.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (live.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, live);
        return original;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.THAUMATORIUM.get());
    }
    @Override public void removed(Player player) { container.stopOpen(player); super.removed(player); }
}
