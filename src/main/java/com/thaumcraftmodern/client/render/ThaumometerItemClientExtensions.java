package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Keeps the Thaumometer out of Forge's OBJ bake path. Old OptiFine versions
 * mutate the block vertex format while shaders are reloaded, which makes the
 * Forge 1.20.1 OBJ baker produce a missing model until the game is restarted.
 */
public final class ThaumometerItemClientExtensions {
    private ThaumometerItemClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private BlockEntityWithoutLevelRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) {
                renderer = new ClassicThaumometerItemRenderer();
            }
            return renderer;
        }
    }
}
