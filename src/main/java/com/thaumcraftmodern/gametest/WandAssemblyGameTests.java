package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.arcane.ArcaneRecipe;
import com.thaumcraftmodern.arcane.ArcaneWandAssemblyRecipe;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.entity.ArcaneCraftingInventory;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WandAssemblyGameTests {
    private WandAssemblyGameTests() {
    }

    @GameTest(template = "empty", batch = "wandAssembly")
    public static void dynamicWandAndStaffRecipesPreserveChosenComponents(
            GameTestHelper helper) {
        ArcaneRecipe wand = recipe(helper, wandGrid(
                new ItemStack(ModItems.GOLD_WAND_CAP.get()),
                new ItemStack(ModItems.GREATWOOD_WAND_ROD.get())));
        helper.assertTrue(wand instanceof ArcaneWandAssemblyRecipe,
                "Greatwood/gold wand did not select the dynamic recipe");
        assertUniformCost(helper, wand.visCost(wandGrid(
                new ItemStack(ModItems.GOLD_WAND_CAP.get()),
                new ItemStack(ModItems.GREATWOOD_WAND_ROD.get()))), 9);
        ItemStack wandOutput = wand.assemble(wandGrid(
                        new ItemStack(ModItems.GOLD_WAND_CAP.get()),
                        new ItemStack(ModItems.GREATWOOD_WAND_ROD.get())),
                helper.getLevel().registryAccess());
        assertComposition(helper, wandOutput, "greatwood", "gold", WandForm.WAND);

        ArcaneCraftingInventory staffGrid = wandGrid(
                new ItemStack(ModItems.SILVER_WAND_CAP.get()),
                new ItemStack(ModItems.SILVERWOOD_STAFF_ROD.get()));
        ArcaneRecipe staff = recipe(helper, staffGrid);
        assertUniformCost(helper, staff.visCost(staffGrid), 96);
        assertComposition(helper,
                staff.assemble(staffGrid, helper.getLevel().registryAccess()),
                "silverwood_staff", "silver", WandForm.STAFF);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "wandAssembly")
    public static void dynamicSceptreRecipeUsesThreeCapsSalisAndTruncatedCost(
            GameTestHelper helper) {
        ArcaneCraftingInventory grid = new ArcaneCraftingInventory();
        grid.setItem(1, new ItemStack(ModItems.GOLD_WAND_CAP.get()));
        grid.setItem(2, new ItemStack(ModItems.SALIS_MUNDUS.get()));
        grid.setItem(4, new ItemStack(ModItems.GREATWOOD_WAND_ROD.get()));
        grid.setItem(5, new ItemStack(ModItems.GOLD_WAND_CAP.get()));
        grid.setItem(6, new ItemStack(ModItems.GOLD_WAND_CAP.get()));
        ArcaneRecipe recipe = recipe(helper, grid);
        assertUniformCost(helper, recipe.visCost(grid), 13);
        assertComposition(helper,
                recipe.assemble(grid, helper.getLevel().registryAccess()),
                "greatwood", "gold", WandForm.SCEPTRE);
        helper.succeed();
    }

    private static ArcaneCraftingInventory wandGrid(ItemStack cap, ItemStack rod) {
        ArcaneCraftingInventory grid = new ArcaneCraftingInventory();
        grid.setItem(2, cap.copy());
        grid.setItem(4, rod);
        grid.setItem(6, cap.copy());
        return grid;
    }

    private static ArcaneRecipe recipe(
            GameTestHelper helper, ArcaneCraftingInventory grid) {
        return helper.getLevel().getRecipeManager().getRecipeFor(
                ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get(),
                grid,
                helper.getLevel()).orElseThrow(() ->
                new IllegalStateException("No dynamic wand recipe matched"));
    }

    private static void assertUniformCost(
            GameTestHelper helper,
            com.thaumcraftmodern.arcane.ArcaneVisCost cost,
            int expected) {
        helper.assertTrue(cost.amounts().values().stream()
                        .allMatch(value -> value == expected),
                "Expected " + expected + " of every primal Vis, got "
                        + cost.amounts());
    }

    private static void assertComposition(
            GameTestHelper helper,
            ItemStack stack,
            String rod,
            String cap,
            WandForm form) {
        WandState state = WandVisService.state(stack).orElseThrow(() ->
                new IllegalStateException(
                        "Assembled casting tool has no wand NBT"));
        helper.assertTrue(state.rodId().equals(rod) && state.capId().equals(cap),
                "Assembled casting tool lost its chosen components");
        helper.assertTrue(stack.getItem() instanceof WandItem wand
                        && wand.form() == form,
                "Assembled casting tool has the wrong form");
    }
}
