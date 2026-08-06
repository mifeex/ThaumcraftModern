package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class BrainJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ClassicBrainJarModel model;
    BrainJarItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());model=new ClassicBrainJarModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClassicBrainJarModel.LAYER));}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack poses,MultiBufferSource buffers,int light,int overlay){float ticks=Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount;BrainJarBlockEntityRenderer.renderAll(model,0,net.minecraft.util.Mth.sin(ticks/14f)*.03f+.03f,poses,buffers,light,overlay);}
}
