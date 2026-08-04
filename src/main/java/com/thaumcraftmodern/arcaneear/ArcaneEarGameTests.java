package com.thaumcraftmodern.arcaneear;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.ArcaneEarBlock;
import com.thaumcraftmodern.world.block.entity.ArcaneEarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArcaneEarGameTests {
    private static final BlockPos SUPPORT = new BlockPos(2, 1, 2);
    private static final BlockPos EAR = SUPPORT.above();

    private ArcaneEarGameTests() {
    }

    @GameTest(template = "empty", batch = "arcaneEar", timeoutTicks = 40)
    public static void hearsMatchingClassicNoteForTenTicks(
            GameTestHelper helper
    ) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        helper.setBlock(EAR, ModBlocks.ARCANE_EAR.get());
        ArcaneEarBlockEntity ear = ear(helper);
        ear.updateTone();

        helper.assertTrue(ear.tone() == 1 && ear.note() == 0,
                "Stone support did not select TC4 bass-drum tone");
        ArcaneEarNoteEvents.dispatch(
                helper.getLevel(), helper.absolutePos(EAR.east()), 1, 1
        );
        helper.assertTrue(!ear.powered(), "A mismatched note activated the ear");

        ArcaneEarNoteEvents.dispatch(
                helper.getLevel(), helper.absolutePos(EAR.east()), 1, 0
        );
        helper.assertTrue(ear.powered(), "The matching note was not detected");
        helper.assertTrue(
                helper.getBlockState(EAR).getValue(ArcaneEarBlock.POWERED),
                "The active model state was not synchronized"
        );
        helper.assertTrue(
                helper.getBlockState(EAR).getSignal(
                        helper.getLevel(), helper.absolutePos(EAR), Direction.UP
                ) == 15,
                "The ear did not output redstone level 15"
        );

        helper.runAfterDelay(11, () -> {
            helper.assertTrue(!ear.powered(),
                    "The TC4 ten-tick pulse did not expire");
            helper.assertTrue(
                    !helper.getBlockState(EAR).getValue(ArcaneEarBlock.POWERED),
                    "The inactive model state was not restored"
            );
            helper.succeed();
        });
    }

    private static ArcaneEarBlockEntity ear(GameTestHelper helper) {
        if (helper.getBlockEntity(EAR) instanceof ArcaneEarBlockEntity ear) {
            return ear;
        }
        throw new IllegalStateException("Arcane Ear block entity was not created");
    }
}
