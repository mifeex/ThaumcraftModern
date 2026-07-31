package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WandClientItemExtensionsTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void activeRightHandUsesStableVanillaRestingTranslation() {
        PoseStack poseStack = new PoseStack();

        WandClientItemExtensions.applyStableHandTransform(
                poseStack,
                HumanoidArm.RIGHT
        );

        Vector3f translation = poseStack.last()
                .pose()
                .getTranslation(new Vector3f());
        assertEquals(0.56F, translation.x, EPSILON);
        assertEquals(-0.52F, translation.y, EPSILON);
        assertEquals(-0.72F, translation.z, EPSILON);
    }

    @Test
    void activeLeftHandMirrorsOnlyHorizontalTranslation() {
        PoseStack poseStack = new PoseStack();

        WandClientItemExtensions.applyStableHandTransform(
                poseStack,
                HumanoidArm.LEFT
        );

        Vector3f translation = poseStack.last()
                .pose()
                .getTranslation(new Vector3f());
        assertEquals(-0.56F, translation.x, EPSILON);
        assertEquals(-0.52F, translation.y, EPSILON);
        assertEquals(-0.72F, translation.z, EPSILON);
    }

    @Test
    void wandNbtUpdatesCannotRestartOrDropTheUsePose()
            throws IOException {
        String wandItem = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/WandItem.java"
        ));
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ClassicWandItemRenderer.java"
        ));

        assertTrue(wandItem.contains(
                "shouldCauseReequipAnimation("
        ));
        assertTrue(wandItem.contains(
                "oldStack.getItem() != newStack.getItem()"
        ));
        assertTrue(renderer.contains(
                "stack.getItem()\n"
                        + "                == minecraft.player"
                        + ".getUseItem().getItem()"
        ));
    }
}
