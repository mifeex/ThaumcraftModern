package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.mirror.MirrorLink;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.MagicMirrorBlock;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EssentiaMirrorGameTests {
    private EssentiaMirrorGameTests() {
    }

    @GameTest(template = "infusion_empty", batch = "essentiaMirror",
            timeoutTicks = 40)
    public static void linkedMirrorDrainsNonAdjacentAirSourceAndPublishesFx(
            GameTestHelper helper
    ) {
        BlockPos localPos = new BlockPos(4, 3, 4);
        BlockPos remotePos = new BlockPos(12, 3, 4);
        BlockPos sourcePos = new BlockPos(17, 5, 6);

        helper.setBlock(localPos.west(), Blocks.STONE);
        helper.setBlock(remotePos.west(), Blocks.STONE);
        helper.setBlock(localPos, ModBlocks.ESSENTIA_MIRROR.get()
                .defaultBlockState().setValue(
                        MagicMirrorBlock.FACING,
                        Direction.EAST
                ));
        helper.setBlock(remotePos, ModBlocks.ESSENTIA_MIRROR.get()
                .defaultBlockState().setValue(
                        MagicMirrorBlock.FACING,
                        Direction.EAST
                ));
        helper.setBlock(sourcePos, ModBlocks.WARDED_JAR.get());

        EssentiaMirrorBlockEntity local = (EssentiaMirrorBlockEntity)
                helper.getBlockEntity(localPos);
        EssentiaMirrorBlockEntity remote = (EssentiaMirrorBlockEntity)
                helper.getBlockEntity(remotePos);
        EssentiaJarBlockEntity source = (EssentiaJarBlockEntity)
                helper.getBlockEntity(sourcePos);
        ServerLevel level = helper.getLevel();

        local.setDestination(MirrorLink.of(
                level,
                helper.absolutePos(remotePos)
        ));
        remote.setDestination(MirrorLink.of(
                level,
                helper.absolutePos(localPos)
        ));
        helper.assertTrue(source.addEssentia("aer", 1, Direction.UP) == 1,
                "Could not fill remote source jar");

        helper.assertTrue(local.takeFromAir("aer"),
                "Linked mirror did not draw essentia through air");
        helper.assertTrue(source.amount() == 0,
                "Remote source jar was not drained exactly once");
        helper.assertTrue(remote.effectSource() != null
                        && remote.effectSource().equals(
                                helper.absolutePos(sourcePos)),
                "Remote mirror did not publish the source-to-mirror FX");
        helper.assertTrue(!local.isConnectable(Direction.EAST),
                "Essentia mirror incorrectly exposed a pipe connection");
        helper.succeed();
    }
}
