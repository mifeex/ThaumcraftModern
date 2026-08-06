package com.thaumcraftmodern.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class BrainJarItemClientExtensions {
    private BrainJarItemClientExtensions(){}
    public static IClientItemExtensions create(){return new IClientItemExtensions(){private BlockEntityWithoutLevelRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new BrainJarItemRenderer();return renderer;}};}
}
