package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VisualAcceptanceFollowupGameTests {
    private static final BlockPos TABLE = new BlockPos(1, 1, 1);

    private VisualAcceptanceFollowupGameTests() {
    }

    @GameTest(template = "empty", batch = "visualAcceptanceFollowup")
    public static void arcaneWorkbenchPreservesEverySlotAcrossNbtReload(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE, ModBlocks.ARCANE_WORKBENCH.get());
        ArcaneWorkbenchBlockEntity source =
                (ArcaneWorkbenchBlockEntity) helper.getBlockEntity(TABLE);
        source.crafting().setItem(0, new ItemStack(Items.DIAMOND, 2));
        source.crafting().setItem(4, new ItemStack(Items.GOLD_INGOT, 3));
        source.crafting().setItem(8, new ItemStack(Items.REDSTONE, 4));
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        wand.getOrCreateTag().putString("reload_probe", "preserved");
        source.wand().setItem(0, wand);

        CompoundTag saved = source.saveWithFullMetadata();
        ArcaneWorkbenchBlockEntity restored = new ArcaneWorkbenchBlockEntity(
                helper.absolutePos(TABLE),
                ModBlocks.ARCANE_WORKBENCH.get().defaultBlockState()
        );
        restored.load(saved);

        helper.assertTrue(restored.crafting().getItem(0).is(Items.DIAMOND)
                        && restored.crafting().getItem(0).getCount() == 2,
                "Slot 0 was not restored exactly");
        helper.assertTrue(restored.crafting().getItem(4).is(Items.GOLD_INGOT)
                        && restored.crafting().getItem(4).getCount() == 3,
                "Slot 4 was not restored exactly");
        helper.assertTrue(restored.crafting().getItem(8).is(Items.REDSTONE)
                        && restored.crafting().getItem(8).getCount() == 4,
                "Slot 8 was not restored exactly");
        helper.assertTrue(restored.crafting().getItems().stream()
                        .mapToInt(ItemStack::getCount).sum() == 9,
                "Reload duplicated or lost crafting-grid items");
        helper.assertTrue(restored.wand().getItem(0).is(ModItems.BASIC_WAND.get())
                        && "preserved".equals(restored.wand().getItem(0)
                                .getOrCreateTag().getString("reload_probe")),
                "Wand slot or its NBT was not restored");
        helper.succeed();
    }
}
