package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.ManaPodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManaPodGameTests {
    private ManaPodGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "manaPodGrowth",
            timeoutTicks = 100
    )
    public static void hangingPodNaturallyGrowsAndAcceptsBonemeal(
            GameTestHelper helper
    ) {
        BlockPos podPosition = helper.absolutePos(new BlockPos(8, 8, 8));
        helper.getLevel().setBlock(
                podPosition.above(),
                Blocks.OAK_LOG.defaultBlockState(),
                2
        );
        helper.getLevel().setBlock(
                podPosition,
                ModBlocks.MANA_POD.get().defaultBlockState(),
                2
        );

        ManaPodBlock pod = (ManaPodBlock) ModBlocks.MANA_POD.get();
        RandomSource random = RandomSource.create(14298543L);
        for (int attempt = 0;
                attempt < 2_000
                        && helper.getLevel().getBlockState(podPosition)
                                .getValue(ManaPodBlock.AGE) == 0;
                attempt++) {
            pod.randomTick(
                    helper.getLevel().getBlockState(podPosition),
                    helper.getLevel(),
                    podPosition,
                    random
            );
        }
        int naturallyGrownAge = helper.getLevel()
                .getBlockState(podPosition)
                .getValue(ManaPodBlock.AGE);
        helper.assertTrue(
                naturallyGrownAge == 1,
                "Mana Pod did not naturally advance exactly one stage"
        );

        pod.performBonemeal(
                helper.getLevel(),
                RandomSource.create(14298544L),
                podPosition,
                helper.getLevel().getBlockState(podPosition)
        );
        int fertilizedAge = helper.getLevel()
                .getBlockState(podPosition)
                .getValue(ManaPodBlock.AGE);
        helper.assertTrue(
                fertilizedAge > naturallyGrownAge,
                "Mana Pod ignored bone meal"
        );
        helper.succeed();
    }
}
