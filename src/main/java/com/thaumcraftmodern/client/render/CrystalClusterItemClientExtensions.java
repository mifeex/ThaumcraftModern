package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class CrystalClusterItemClientExtensions {
    private CrystalClusterItemClientExtensions() {
    }

    public static IClientItemExtensions create() {
        return new Extensions();
    }

    private static final class Extensions implements IClientItemExtensions {
        private BlockEntityWithoutLevelRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) {
                renderer = new CrystalClusterItemRenderer();
            }
            return renderer;
        }
    }
}
