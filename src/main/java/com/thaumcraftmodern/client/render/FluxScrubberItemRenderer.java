package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class FluxScrubberItemRenderer extends BlockEntityWithoutLevelRenderer {
    FluxScrubberItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        float ticks=Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount;
        float bob=net.minecraft.util.Mth.sin(ticks/8f)*.075f+.075f;
        // TC4's worldless TileFluxScrubber starts with facing index 0 (DOWN).
        // Using NORTH exposes the square cap face in GUI as a black X.
        FluxScrubberBlockEntityRenderer.renderModel(poses,buffers,light,overlay,Direction.DOWN,bob);
    }
}
