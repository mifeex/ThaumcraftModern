package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Client-only bridge for the classic jar-and-node inventory renderer.
 */
public final class JarredAuraNodeItemClientExtensions {
    private JarredAuraNodeItemClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private BlockEntityWithoutLevelRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) {
                renderer = new JarredAuraNodeItemRenderer();
            }
            return renderer;
        }
    }
}
