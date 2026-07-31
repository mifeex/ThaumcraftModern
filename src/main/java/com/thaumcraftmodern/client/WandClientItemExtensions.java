package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.client.render.ClassicWandItemRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Supplies the NBT-aware renderer used by assembled casting tools.
 *
 * <p>The custom renderer owns the casting motion. While a wand is actively
 * used, this extension restores the same stable arm translation that vanilla
 * applies to an idle item. Vanilla applies no base hand transform at all for
 * {@code UseAnim.NONE}; without this bridge the model jumps to a different
 * matrix basis on the first use frame.</p>
 */
public final class WandClientItemExtensions implements IClientItemExtensions {
    public static final WandClientItemExtensions INSTANCE =
            new WandClientItemExtensions();

    private BlockEntityWithoutLevelRenderer renderer;

    private WandClientItemExtensions() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new ClassicWandItemRenderer();
        }
        return renderer;
    }

    @Override
    public boolean applyForgeHandTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            ItemStack itemInHand,
            float partialTick,
            float equipProcess,
            float swingProcess
    ) {
        if (!isUsedArm(player, arm)) {
            return false;
        }
        applyStableHandTransform(poseStack, arm);
        return true;
    }

    static void applyStableHandTransform(
            PoseStack poseStack,
            HumanoidArm arm
    ) {
        float handSign = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(
                handSign * 0.56F,
                -0.52F,
                -0.72F
        );
    }

    private static boolean isUsedArm(
            LocalPlayer player,
            HumanoidArm renderedArm
    ) {
        if (!player.isUsingItem()) {
            return false;
        }
        InteractionHand usedHand = player.getUsedItemHand();
        HumanoidArm usedArm = usedHand == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        return usedArm == renderedArm;
    }
}
