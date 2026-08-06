package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.focus.WandFocusService;
import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WandFocusGameTests {
    private WandFocusGameTests() {}

    @GameTest(template = "empty", batch = "wandFocus")
    public static void focusRoundTripsInsideWandNbtAndSceptreRejectsIt(
            GameTestHelper helper) {
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        ItemStack fire = new ItemStack(
                ModItems.ARCANE_RECIPE_COMPONENTS.get("focus_fire").get());
        helper.assertTrue(WandFocusService.setFocus(wand, fire),
                "A casting wand rejected a base focus");
        helper.assertTrue(WandFocusService.type(wand).orElse(null) == WandFocusType.FIRE,
                "The installed focus did not survive its ItemStack NBT round trip");
        ItemStack copied = wand.copy();
        helper.assertTrue(WandFocusService.type(copied).orElse(null) == WandFocusType.FIRE,
                "Copying the wand lost its installed focus");
        WandFocusService.clearFocus(copied);
        helper.assertTrue(WandFocusService.type(copied).isEmpty(),
                "Removing the focus left stale focus NBT");

        ItemStack sceptre = ModItems.CRAFTING_SCEPTRE.get().getDefaultInstance();
        helper.assertTrue(!WandFocusService.setFocus(sceptre, fire),
                "A TC4 sceptre accepted a wand focus");
        helper.succeed();
    }
}
