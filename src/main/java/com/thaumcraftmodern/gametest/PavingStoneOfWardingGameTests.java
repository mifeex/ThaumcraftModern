package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.PavingStoneOfWardingBlockEntity;
import com.thaumcraftmodern.world.block.entity.WardingAuraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PavingStoneOfWardingGameTests {
    private static final BlockPos STONE = new BlockPos(2, 1, 2);
    private static final BlockPos LOWER_AURA = STONE.above();
    private static final BlockPos UPPER_AURA = STONE.above(2);

    private PavingStoneOfWardingGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "pavingStoneWarding",
            timeoutTicks = 40
    )
    public static void projectsEntitySpecificRedstoneControlledBarrier(
            GameTestHelper helper
    ) {
        helper.setBlock(STONE, Blocks.AIR);
        helper.setBlock(LOWER_AURA, Blocks.AIR);
        helper.setBlock(UPPER_AURA, Blocks.AIR);
        helper.setBlock(STONE.east(), Blocks.AIR);
        helper.setBlock(STONE, ModBlocks.PAVING_STONE_OF_WARDING.get());
        PavingStoneOfWardingBlockEntity stone =
                (PavingStoneOfWardingBlockEntity) helper.getBlockEntity(STONE);
        for (int tick = 0; tick < 200; tick++) {
            PavingStoneOfWardingBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(STONE),
                    helper.getBlockState(STONE),
                    stone
            );
        }
        helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), LOWER_AURA);
        helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), UPPER_AURA);

        BlockState aura = helper.getBlockState(LOWER_AURA);
        BlockPos absoluteAura = helper.absolutePos(LOWER_AURA);
        ArmorStand creature = helper.spawn(
                EntityType.ARMOR_STAND,
                LOWER_AURA
        );
        Player player = helper.makeMockPlayer();
        helper.assertFalse(
                aura.getCollisionShape(
                        helper.getLevel(),
                        absoluteAura,
                        CollisionContext.of(creature)
                ).isEmpty(),
                "Non-player living entity was not blocked"
        );
        helper.assertTrue(
                aura.getCollisionShape(
                        helper.getLevel(),
                        absoluteAura,
                        CollisionContext.of(player)
                ).isEmpty(),
                "Player must be able to cross the ward"
        );

        helper.setBlock(STONE.east(), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(
                aura.getCollisionShape(
                        helper.getLevel(),
                        absoluteAura,
                        CollisionContext.of(creature)
                ).isEmpty(),
                "Redstone signal did not disable the ward"
        );
        helper.setBlock(STONE.east(), Blocks.AIR);
        helper.assertFalse(
                aura.getCollisionShape(
                        helper.getLevel(),
                        absoluteAura,
                        CollisionContext.of(creature)
                ).isEmpty(),
                "Ward did not reactivate after redstone was removed"
        );
        helper.assertTrue(
                ModItems.ARCANE_RECIPE_COMPONENTS
                        .get("paving_stone_of_warding").get()
                        instanceof BlockItem,
                "Recipe output is still a placeholder item"
        );

        WardingAuraBlockEntity lower =
                (WardingAuraBlockEntity) helper.getBlockEntity(LOWER_AURA);
        WardingAuraBlockEntity upper =
                (WardingAuraBlockEntity) helper.getBlockEntity(UPPER_AURA);
        helper.setBlock(STONE, Blocks.AIR);
        tickAuraForCleanup(helper, LOWER_AURA, lower);
        tickAuraForCleanup(helper, UPPER_AURA, upper);
        helper.assertBlockNotPresent(ModBlocks.WARDING_AURA.get(), LOWER_AURA);
        helper.assertBlockNotPresent(ModBlocks.WARDING_AURA.get(), UPPER_AURA);
        helper.succeed();
    }

    private static void tickAuraForCleanup(
            GameTestHelper helper,
            BlockPos position,
            WardingAuraBlockEntity aura
    ) {
        for (int tick = 0; tick < 200; tick++) {
            WardingAuraBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(position),
                    helper.getBlockState(position),
                    aura
            );
            if (!helper.getBlockState(position).is(
                    ModBlocks.WARDING_AURA.get()
            )) {
                return;
            }
        }
    }
}
