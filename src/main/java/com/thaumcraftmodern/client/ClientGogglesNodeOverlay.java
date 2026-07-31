package com.thaumcraftmodern.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.Optional;

/**
 * Shared TC4-style aspect-container HUD used by aura nodes and crucibles while
 * the Goggles of Revealing occupy the vanilla head slot.
 */
public final class ClientGogglesNodeOverlay {
    private static final NodeHudFade FADE = new NodeHudFade();
    private static ClientAspectContainerReadout.Target lastTarget;

    private ClientGogglesNodeOverlay() {
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientAspectContainerReadout.wearingGoggles(minecraft)
                || ClientThaumometerTarget.isHoldingThaumometer(minecraft)) {
            lastTarget = null;
            FADE.reset();
            return;
        }

        Optional<ClientAspectContainerReadout.Target> current =
                ClientAspectContainerReadout.find(minecraft, partialTick);
        current.ifPresent(target -> lastTarget = target);
        NodeHudFade.Frame frame = FADE.update(current.isPresent(), Util.getMillis());
        if (!frame.visible() || lastTarget == null) {
            if (!frame.visible()) {
                lastTarget = null;
            }
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double fov = minecraft.options.fov().get();
        NodeHudProjection.worldAnchored(
                camera,
                lastTarget.anchor(),
                fov,
                screenWidth,
                screenHeight
        ).ifPresent(point -> ClientScanOverlay.renderNodeAspects(
                graphics,
                minecraft,
                lastTarget.aspects(),
                point.x(),
                point.y() + Math.round((1.0F - frame.alpha()) * 5.0F),
                frame.alpha(),
                frame.scale()
        ));
    }
}
