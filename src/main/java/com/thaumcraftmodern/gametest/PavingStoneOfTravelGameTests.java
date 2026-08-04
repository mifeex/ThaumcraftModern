package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.PavingStoneOfTravelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PavingStoneOfTravelGameTests {
    private static final BlockPos STONE = new BlockPos(2, 1, 2);

    private PavingStoneOfTravelGameTests() {
    }

    @GameTest(template = "empty", batch = "pavingStoneTravel")
    public static void appliesExactTc4TravelEffects(GameTestHelper helper) {
        helper.setBlock(STONE, ModBlocks.PAVING_STONE_OF_TRAVEL.get());
        BlockState state = helper.getBlockState(STONE);
        BlockPos absolute = helper.absolutePos(STONE);
        ArmorStand traveler = new ArmorStand(
                helper.getLevel(),
                absolute.getX() + 0.5D,
                absolute.getY() + 1.0D,
                absolute.getZ() + 0.5D
        );

        state.getBlock().stepOn(helper.getLevel(), absolute, state, traveler);

        assertEffect(helper, traveler.getEffect(MobEffects.MOVEMENT_SPEED),
                40, 1, "Speed II");
        assertEffect(helper, traveler.getEffect(MobEffects.JUMP),
                40, 0, "Jump Boost I");
        helper.assertTrue(state.getLightEmission() == 9,
                "Travel stone light level differs from TC4");
        helper.assertTrue(
                ModItems.ARCANE_RECIPE_COMPONENTS
                        .get("paving_stone_of_travel").get()
                        instanceof BlockItem,
                "Recipe output is still a placeholder item"
        );
        helper.succeed();
    }

    private static void assertEffect(
            GameTestHelper helper,
            MobEffectInstance effect,
            int duration,
            int amplifier,
            String name
    ) {
        helper.assertTrue(
                effect != null
                        && effect.getDuration() == duration
                        && effect.getAmplifier() == amplifier
                        && !effect.isAmbient()
                        && !effect.isVisible(),
                name + " does not match TC4"
        );
    }
}
