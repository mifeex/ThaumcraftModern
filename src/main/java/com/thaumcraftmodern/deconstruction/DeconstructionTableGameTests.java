package com.thaumcraftmodern.deconstruction;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.DeconstructionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DeconstructionTableGameTests {
    private static final BlockPos TABLE = new BlockPos(2, 1, 2);

    private DeconstructionTableGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "deconstructionTable",
            timeoutTicks = 100
    )
    public static void processesPersistsAndHonorsSidedAccess(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE, ModBlocks.DECONSTRUCTION_TABLE.get());
        DeconstructionTableBlockEntity table = table(helper);

        ItemStack star = new ItemStack(Items.NETHER_STAR, 2);
        helper.assertTrue(
                table.canPlaceItem(0, star),
                "Explicit scan aspects did not make the item valid"
        );
        helper.assertTrue(
                table.getSlotsForFace(Direction.UP).length == 0,
                "Top face exposed the input slot"
        );
        helper.assertTrue(
                table.getSlotsForFace(Direction.NORTH).length == 1
                        && table.canPlaceItemThroughFace(
                                0,
                                star,
                                Direction.NORTH
                        )
                        && table.canTakeItemThroughFace(
                                0,
                                star,
                                Direction.NORTH
                        ),
                "Non-top sided automation differs from TC4"
        );
        table.setItem(0, star);

        helper.runAfterDelay(42, () -> {
            helper.assertTrue(
                    table.getItem(0).getCount() == 1,
                    "The table did not consume exactly one item after 40 ticks"
            );
            helper.assertTrue(
                    table.aspectId() != null,
                    "A >80-primal input did not produce its guaranteed result"
            );
            String result = table.aspectId();

            CompoundTag saved = table.saveWithFullMetadata();
            DeconstructionTableBlockEntity restored =
                    new DeconstructionTableBlockEntity(
                            helper.absolutePos(TABLE),
                            ModBlocks.DECONSTRUCTION_TABLE.get()
                                    .defaultBlockState()
                    );
            restored.load(saved);
            helper.assertTrue(
                    result.equals(restored.aspectId())
                            && restored.getItem(0).getCount() == 1
                            && restored.breakTime() == 0,
                    "Inventory/result persistence or progress reset differs from TC4"
            );

            helper.runAfterDelay(5, () -> {
                helper.assertTrue(
                        table.getItem(0).getCount() == 1,
                        "A pending aspect did not block the next item"
                );
                helper.assertTrue(
                        table.clearAspect(result),
                        "The result could not be claimed"
                );
                helper.runAfterDelay(42, () -> {
                    helper.assertTrue(
                            table.getItem(0).isEmpty(),
                            "Processing did not resume after claiming the result"
                    );
                    helper.succeed();
                });
            });
        });
    }

    private static DeconstructionTableBlockEntity table(
            GameTestHelper helper
    ) {
        if (helper.getBlockEntity(TABLE)
                instanceof DeconstructionTableBlockEntity table) {
            return table;
        }
        throw new IllegalStateException("Deconstruction table was not created");
    }
}
