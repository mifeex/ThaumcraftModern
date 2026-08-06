package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Modern render adapter for TC4 FXShieldRunes and its 15 original frames. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ClientRunicShieldEffect {
    private static final Map<Integer, Long> ACTIVE = new HashMap<>();
    private ClientRunicShieldEffect() { }

    public static void accept(int entityId, int sourceId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) ACTIVE.put(entityId,
                minecraft.level.getGameTime() + 20L);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || ACTIVE.isEmpty()) return;
        long time = minecraft.level.getGameTime();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        Iterator<Map.Entry<Integer, Long>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            var active = iterator.next();
            Entity entity = minecraft.level.getEntity(active.getKey());
            if (entity == null || active.getValue() <= time) { iterator.remove(); continue; }
            int frame = Math.max(1, Math.min(15,
                    15 - (int) ((active.getValue() - time) * 14 / 20)));
            ResourceLocation texture = new ResourceLocation(ThaumcraftModern.MOD_ID,
                    "textures/models/hemis" + frame + ".png");
            VertexConsumer vertices = buffers.getBuffer(
                    RenderType.entityTranslucentEmissive(texture));
            double x = entity.getX() - camera.x;
            double y = entity.getY() + entity.getBbHeight() * 0.5D - camera.y;
            double z = entity.getZ() - camera.z;
            float size = entity.getBbHeight() * 0.55F;
            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(event.getCamera().rotation());
            PoseStack.Pose p = pose.last();
            vertex(vertices, p, -size, -size, 0, 1);
            vertex(vertices, p, size, -size, 1, 1);
            vertex(vertices, p, size, size, 1, 0);
            vertex(vertices, p, -size, size, 0, 0);
            pose.popPose();
            buffers.endBatch(RenderType.entityTranslucentEmissive(texture));
        }
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose,
            float x, float y, float u, float v) {
        vertices.vertex(pose.pose(), x, y, 0).color(255, 255, 255, 220)
                .uv(u, v).overlayCoords(0).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 0, 1).endVertex();
    }
}
