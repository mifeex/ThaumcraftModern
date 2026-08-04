package com.thaumcraftmodern.crystal;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrystalClusterGameTests {
    private static final BlockPos SUPPORT = new BlockPos(2, 1, 2);
    private static final BlockPos CLUSTER = SUPPORT.above();

    private CrystalClusterGameTests() {
    }

    @GameTest(template = "empty", batch = "crystalClusters")
    public static void recipesDropsAndSupportMatchTc4(GameTestHelper helper) {
        for (String recipe : List.of(
                "air", "fire", "water", "earth", "order", "entropy",
                "balanced"
        )) {
            helper.assertTrue(
                    helper.getLevel().getRecipeManager().byKey(
                            new ResourceLocation(
                                    ThaumcraftModern.MOD_ID,
                                    recipe + "_crystal_cluster"
                            )
                    ).isPresent(),
                    "Missing live cluster recipe: " + recipe
            );
        }

        helper.setBlock(SUPPORT, Blocks.STONE);
        helper.setBlock(
                CLUSTER,
                ModBlocks.BALANCED_CRYSTAL_CLUSTER.get()
                        .defaultBlockState()
                        .setValue(CrystalClusterBlock.FACING, Direction.UP)
        );
        helper.assertTrue(
                helper.getBlockEntity(CLUSTER)
                        instanceof CrystalClusterBlockEntity,
                "Crystal cluster block entity was not created"
        );
        List<ItemStack> balancedDrops = Block.getDrops(
                helper.getBlockState(CLUSTER),
                helper.getLevel(),
                helper.absolutePos(CLUSTER),
                helper.getBlockEntity(CLUSTER)
        );
        for (Item shard : List.of(
                ModItems.AIR_SHARD.get(),
                ModItems.FIRE_SHARD.get(),
                ModItems.WATER_SHARD.get(),
                ModItems.EARTH_SHARD.get(),
                ModItems.ORDER_SHARD.get(),
                ModItems.ENTROPY_SHARD.get()
        )) {
            helper.assertTrue(
                    balancedDrops.stream().anyMatch(stack ->
                            stack.is(shard) && stack.getCount() == 1),
                    "Mixed cluster did not return every component shard"
            );
        }

        List<ItemStack> airDrops = Block.getDrops(
                ModBlocks.AIR_CRYSTAL_CLUSTER.get().defaultBlockState(),
                helper.getLevel(),
                helper.absolutePos(CLUSTER),
                null
        );
        helper.assertTrue(
                airDrops.size() == 1
                        && airDrops.get(0).is(ModItems.AIR_SHARD.get())
                        && airDrops.get(0).getCount() == 6,
                "Single-aspect cluster did not return six matching shards"
        );

        helper.setBlock(SUPPORT, Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(
                    helper.getBlockState(CLUSTER).isAir(),
                    "Unsupported cluster did not break"
            );
            helper.succeed();
        });
    }
}
