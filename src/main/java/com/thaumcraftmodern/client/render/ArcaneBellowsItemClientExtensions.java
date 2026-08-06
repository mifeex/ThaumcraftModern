package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Lazily creates the same five-part model for inventory and book rendering. */
public final class ArcaneBellowsItemClientExtensions {
    private ArcaneBellowsItemClientExtensions() { }
    public static IClientItemExtensions create() {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new ArcaneBellowsItemRenderer();
                return renderer;
            }
        };
    }
}
