package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.arcane.ArcaneRecipe;
import com.thaumcraftmodern.arcane.ArcaneVisCost;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.ArcaneCraftingInventory;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    public static final int RESULT_MENU_SLOT = 0;
    public static final int GRID_MENU_SLOT_START = 1;
    public static final int GRID_MENU_SLOT_END = 10;
    public static final int WAND_MENU_SLOT = 10;
    public static final int PLAYER_MENU_SLOT_START = 11;
    public static final int PLAYER_MENU_SLOT_END = 47;

    private final ArcaneWorkbenchBlockEntity workbench;
    private final ArcaneCraftingInventory crafting;
    private final SimpleContainer wand;
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player owner;
    private final ContainerListener contentListener = this::slotsChanged;
    private boolean completingCraft;

    public ArcaneWorkbenchMenu(
            int containerId,
            Inventory playerInventory,
            ArcaneWorkbenchBlockEntity workbench
    ) {
        super(ModMenus.ARCANE_WORKBENCH.get(), containerId);
        this.workbench = workbench;
        this.crafting = workbench.crafting();
        this.wand = workbench.wand();
        this.owner = playerInventory.player;
        this.access = ContainerLevelAccess.create(owner.level(), workbench.getBlockPos());

        addSlot(new ArcaneResultSlot(result, 0, 160, 64));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = column + row * 3;
                addSlot(new Slot(crafting, slot, 40 + column * 24, 40 + row * 24));
            }
        }
        addSlot(new Slot(wand, 0, 160, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return WandVisService.isCraftingTool(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        16 + column * 18,
                        151 + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 16 + column * 18, 209));
        }

        crafting.addListener(contentListener);
        wand.addListener(contentListener);
        slotsChanged(crafting);
    }

    public static ArcaneWorkbenchMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf buffer
    ) {
        BlockPos position = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(position)
                instanceof ArcaneWorkbenchBlockEntity workbench) {
            return new ArcaneWorkbenchMenu(containerId, inventory, workbench);
        }
        ThaumcraftModern.LOGGER.warn(
                "Arcane Workbench menu opened before its block entity was available at {}; "
                        + "using a non-valid client placeholder",
                position
        );
        ArcaneWorkbenchBlockEntity placeholder = new ArcaneWorkbenchBlockEntity(
                position,
                ModBlocks.ARCANE_WORKBENCH.get().defaultBlockState()
        );
        return new ArcaneWorkbenchMenu(containerId, inventory, placeholder);
    }

    public ItemStack wandStack() {
        return wand.getItem(0);
    }

    public Optional<ArcaneRecipe> previewRecipe(Player player) {
        return matchingArcaneRecipe(player).filter(recipe -> hasResearch(player, recipe));
    }

    public ArcaneVisCost previewCost(Player player) {
        return previewRecipe(player).map(ArcaneRecipe::visCost).orElse(ArcaneVisCost.EMPTY);
    }

    public Map<String, Integer> previewCostCentivis(Player player) {
        if (!WandVisService.isCraftingTool(wandStack())) {
            return Map.of();
        }
        try {
            return previewRecipe(player)
                    .map(recipe -> WandVisService.adjustedCostCentivis(
                            player,
                            wandStack(),
                            recipe.visCost().amounts()
                    ))
                    .orElse(Map.of());
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    /**
     * Returns the cost that the client should render for a correctly assembled
     * arcane recipe. A wand is not required for the recipe to advertise its
     * base cost; inserting one replaces that preview with the cap- and
     * player-discount-adjusted cost.
     */
    public Map<String, Integer> displayCostCentivis(Player player) {
        Optional<ArcaneRecipe> recipe = previewRecipe(player);
        if (recipe.isEmpty()) {
            return Map.of();
        }
        if (WandVisService.isCraftingTool(wandStack())) {
            try {
                return WandVisService.adjustedCostCentivis(
                        player,
                        wandStack(),
                        recipe.orElseThrow().visCost().amounts()
                );
            } catch (RuntimeException exception) {
                return Map.of();
            }
        }

        try {
            LinkedHashMap<String, Integer> baseCost = new LinkedHashMap<>();
            for (String primal : ArcaneVisCost.PRIMALS) {
                int amount = recipe.orElseThrow().visCost().amount(primal);
                if (amount > 0) {
                    baseCost.put(
                            primal,
                            Math.multiplyExact(amount, WandVisService.CENTIVIS_PER_VIS)
                    );
                }
            }
            return Map.copyOf(baseCost);
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    public boolean hasEnoughVis(Player player) {
        return previewRecipe(player)
                .filter(recipe -> WandVisService.isCraftingTool(wandStack()))
                .map(recipe -> WandVisService.canConsume(
                        player,
                        wandStack(),
                        recipe.visCost().amounts()
                ))
                .orElse(false);
    }

    @Override
    public void slotsChanged(Container changed) {
        super.slotsChanged(changed);
        workbench.setChanged();
        if (owner.level().isClientSide || completingCraft) {
            return;
        }
        Optional<ArcaneRecipe> arcaneRecipe = matchingArcaneRecipe(owner);
        ItemStack output;
        if (arcaneRecipe.isPresent()) {
            output = arcaneRecipe
                    .filter(recipe -> hasResearch(owner, recipe))
                    .filter(recipe -> WandVisService.isCraftingTool(wandStack()))
                    .filter(recipe -> WandVisService.canConsume(
                            owner,
                            wandStack(),
                            recipe.visCost().amounts()
                    ))
                    .map(recipe -> previewOutput(recipe, owner))
                    .orElse(ItemStack.EMPTY);
        } else {
            output = matchingVanillaRecipe(owner)
                    .map(recipe -> previewOutput(recipe, owner))
                    .orElse(ItemStack.EMPTY);
        }
        result.setItem(0, output);
        broadcastChanges();
    }

    private ItemStack previewOutput(Recipe<CraftingContainer> recipe, Player player) {
        result.setRecipeUsed(recipe);
        return recipe.assemble(crafting, player.level().registryAccess());
    }

    private Optional<ArcaneRecipe> matchingArcaneRecipe(Player player) {
        return player.level().getRecipeManager().getRecipeFor(
                ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get(),
                crafting,
                player.level()
        );
    }

    private Optional<CraftingRecipe> matchingVanillaRecipe(Player player) {
        return player.level().getRecipeManager().getRecipeFor(
                RecipeType.CRAFTING,
                crafting,
                player.level()
        );
    }

    private static boolean hasResearch(Player player, ArcaneRecipe recipe) {
        return KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch(recipe.researchId()))
                .orElse(false);
    }

    private boolean canCompleteCraft(ServerPlayer player) {
        if (!stillValid(player)) {
            return false;
        }
        Optional<ArcaneRecipe> arcaneRecipe = matchingArcaneRecipe(player);
        if (arcaneRecipe.isPresent()) {
            return arcaneRecipe
                    .filter(recipe -> hasResearch(player, recipe))
                    .filter(recipe -> WandVisService.isCraftingTool(wandStack()))
                    .filter(recipe -> WandVisService.canConsume(
                            player,
                            wandStack(),
                            recipe.visCost().amounts()
                    ))
                    .isPresent();
        }
        return matchingVanillaRecipe(player).isPresent();
    }

    private boolean completeCraft(ServerPlayer player, ItemStack output) {
        if (!stillValid(player)) {
            return false;
        }
        Optional<ArcaneRecipe> arcaneRecipe = matchingArcaneRecipe(player);
        if (arcaneRecipe.isPresent()) {
            return completeArcaneCraft(player, arcaneRecipe.orElseThrow(), output);
        }
        return matchingVanillaRecipe(player)
                .map(recipe -> completeVanillaCraft(player, recipe, output))
                .orElse(false);
    }

    private boolean completeArcaneCraft(
            ServerPlayer player,
            ArcaneRecipe recipe,
            ItemStack output
    ) {
        if (!hasResearch(player, recipe)
                || !WandVisService.isCraftingTool(wandStack())
                || !WandVisService.canConsume(
                        player,
                        wandStack(),
                        recipe.visCost().amounts()
                )) {
            return false;
        }
        ItemStack expected = recipe.assemble(crafting, player.level().registryAccess());
        if (!ItemStack.isSameItemSameTags(output, expected)
                || output.getCount() != expected.getCount()) {
            return false;
        }

        NonNullList<ItemStack> recipeInputs = NonNullList.withSize(
                crafting.getContainerSize(),
                ItemStack.EMPTY
        );
        for (int slot = 0; slot < crafting.getContainerSize(); slot++) {
            recipeInputs.set(slot, crafting.getItem(slot).copy());
        }
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(crafting);
        if (!WandVisService.consume(player, wandStack(), recipe.visCost().amounts())) {
            return false;
        }
        consumeRecipeInputs(player, recipe, recipeInputs, remainders);
        return true;
    }

    private boolean completeVanillaCraft(
            ServerPlayer player,
            CraftingRecipe recipe,
            ItemStack output
    ) {
        ItemStack expected = recipe.assemble(crafting, player.level().registryAccess());
        if (!ItemStack.isSameItemSameTags(output, expected)
                || output.getCount() != expected.getCount()) {
            return false;
        }
        NonNullList<ItemStack> recipeInputs = NonNullList.withSize(
                crafting.getContainerSize(),
                ItemStack.EMPTY
        );
        for (int slot = 0; slot < crafting.getContainerSize(); slot++) {
            recipeInputs.set(slot, crafting.getItem(slot).copy());
        }
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(crafting);
        consumeRecipeInputs(player, recipe, recipeInputs, remainders);
        return true;
    }

    private void consumeRecipeInputs(
            ServerPlayer player,
            Recipe<CraftingContainer> recipe,
            NonNullList<ItemStack> recipeInputs,
            NonNullList<ItemStack> remainders
    ) {
        completingCraft = true;
        try {
            for (int slot = 0; slot < crafting.getContainerSize(); slot++) {
                ItemStack ingredient = crafting.getItem(slot);
                if (!ingredient.isEmpty()) {
                    crafting.removeItem(slot, 1);
                }
                ItemStack remainder = remainders.get(slot);
                if (remainder.isEmpty()) {
                    continue;
                }
                ItemStack current = crafting.getItem(slot);
                if (current.isEmpty()) {
                    crafting.setItem(slot, remainder);
                } else if (ItemStack.isSameItemSameTags(current, remainder)
                        && current.getCount() + remainder.getCount() <= current.getMaxStackSize()) {
                    current.grow(remainder.getCount());
                    crafting.setChanged();
                } else if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
            player.awardRecipes(java.util.List.of(recipe));
            player.triggerRecipeCrafted(recipe, recipeInputs);
            workbench.setChanged();
        } finally {
            completingCraft = false;
        }
        slotsChanged(crafting);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int menuSlot) {
        Slot slot = slots.get(menuSlot);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack live = slot.getItem();
        ItemStack original = live.copy();

        if (menuSlot == RESULT_MENU_SLOT) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (!canFitPlayerInventory(original)
                        || !completeCraft(serverPlayer, original)) {
                    return ItemStack.EMPTY;
                }
                ItemStack issued = original.copy();
                moveItemStackTo(
                        issued,
                        PLAYER_MENU_SLOT_START,
                        PLAYER_MENU_SLOT_END,
                        true
                );
                if (!issued.isEmpty()
                        && !serverPlayer.getInventory().add(issued)) {
                    serverPlayer.drop(issued, false);
                }
                return original;
            }
            if (!moveItemStackTo(
                    live,
                    PLAYER_MENU_SLOT_START,
                    PLAYER_MENU_SLOT_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(live, original);
        } else if (menuSlot >= GRID_MENU_SLOT_START && menuSlot <= WAND_MENU_SLOT) {
            if (!moveItemStackTo(live, PLAYER_MENU_SLOT_START, PLAYER_MENU_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (WandVisService.isCraftingTool(live)
                && !moveItemStackTo(live, WAND_MENU_SLOT, WAND_MENU_SLOT + 1, false)) {
            if (!moveItemStackTo(live, GRID_MENU_SLOT_START, GRID_MENU_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(live, GRID_MENU_SLOT_START, GRID_MENU_SLOT_END, false)) {
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
        slot.onTake(player, menuSlot == RESULT_MENU_SLOT ? original : live);
        return original;
    }

    private boolean canFitPlayerInventory(ItemStack output) {
        int remaining = output.getCount();
        for (int menuSlot = PLAYER_MENU_SLOT_START;
                menuSlot < PLAYER_MENU_SLOT_END;
                menuSlot++) {
            Slot target = slots.get(menuSlot);
            ItemStack current = target.getItem();
            int capacity;
            if (current.isEmpty()) {
                capacity = Math.min(
                        target.getMaxStackSize(output),
                        output.getMaxStackSize()
                );
            } else if (ItemStack.isSameItemSameTags(current, output)) {
                capacity = Math.max(
                        0,
                        Math.min(
                                target.getMaxStackSize(output),
                                output.getMaxStackSize()
                        ) - current.getCount()
                );
            } else {
                capacity = 0;
            }
            remaining -= capacity;
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ARCANE_WORKBENCH.get());
    }

    @Override
    public void removed(Player player) {
        crafting.removeListener(contentListener);
        wand.removeListener(contentListener);
        super.removed(player);
    }

    private final class ArcaneResultSlot extends Slot {
        private ArcaneResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                return canCompleteCraft(serverPlayer);
            }
            return !getItem().isEmpty();
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack preview = getItem().copy();
            if (preview.isEmpty() || amount < preview.getCount()) {
                return ItemStack.EMPTY;
            }
            if (!(owner instanceof ServerPlayer serverPlayer)) {
                return super.remove(amount);
            }
            return completeCraft(serverPlayer, preview)
                    ? preview
                    : ItemStack.EMPTY;
        }

        @Override
        public void onTake(Player player, ItemStack crafted) {
            super.onTake(player, crafted);
        }
    }
}
