package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.network.packet.WispZapPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Modern rendition of TC4's four-tick FXLightningBolt used by Wisp attacks.
 * It uses the original p_large/p_small textures in two full-bright passes.
 */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientWispZapRenderer {
    static final int LIFETIME_TICKS = 4;
    static final int SEGMENTS = 16;
    private static final ResourceLocation LARGE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/p_large.png"
    );
    private static final ResourceLocation SMALL = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/misc/p_small.png"
    );
    private static final List<Bolt> BOLTS = new ArrayList<>();

    private ClientWispZapRenderer() {
    }

    public static void accept(WispZapPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity source = minecraft.level.getEntity(packet.sourceId());
        Entity target = minecraft.level.getEntity(packet.targetId());
        if (source == null || target == null) {
            return;
        }
        BOLTS.add(new Bolt(
                packet.sourceId(),
                packet.targetId(),
                packet.seed(),
                minecraft.level.getGameTime()
        ));
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || BOLTS.isEmpty()) {
            return;
        }
        long now = minecraft.level.getGameTime();
        PoseStack pose = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        var cameraDirection = camera.getLookVector();
        Vec3 cameraLook = new Vec3(
                cameraDirection.x(),
                cameraDirection.y(),
                cameraDirection.z()
        );
        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();
        RenderType largeType = RenderType.entityTranslucentEmissive(LARGE);
        RenderType smallType = RenderType.entityTranslucentEmissive(SMALL);

        pose.pushPose();
        pose.translate(
                -cameraPosition.x,
                -cameraPosition.y,
                -cameraPosition.z
        );
        Iterator<Bolt> iterator = BOLTS.iterator();
        while (iterator.hasNext()) {
            Bolt bolt = iterator.next();
            long age = now - bolt.createdTick();
            if (age < 0L || age >= LIFETIME_TICKS) {
                iterator.remove();
                continue;
            }
            Entity source = minecraft.level.getEntity(bolt.sourceId());
            Entity target = minecraft.level.getEntity(bolt.targetId());
            if (source == null || target == null) {
                iterator.remove();
                continue;
            }
            float partialTick = event.getPartialTick();
            Vec3 start = interpolatedCenter(source, partialTick);
            Vec3 end = interpolatedCenter(target, partialTick);
            Vec3[] points = path(
                    start,
                    end,
                    bolt.seed() + age * 31L
            );
            float fade = 1.0F - (age + partialTick)
                    / (float) LIFETIME_TICKS;
            draw(
                    buffers.getBuffer(largeType),
                    pose.last(),
                    points,
                    cameraLook,
                    0.075F,
                    1.0F,
                    1.0F,
                    1.0F,
                    Math.max(0.08F, fade * 0.45F)
            );
            draw(
                    buffers.getBuffer(smallType),
                    pose.last(),
                    points,
                    cameraLook,
                    0.038F,
                    0.75F,
                    1.0F,
                    1.0F,
                    Math.max(0.12F, fade)
            );
        }
        buffers.endBatch(largeType);
        buffers.endBatch(smallType);
        pose.popPose();
    }

    private static Vec3 interpolatedCenter(Entity entity, float partialTick) {
        return new Vec3(
                entity.xOld + (entity.getX() - entity.xOld) * partialTick,
                entity.yOld + (entity.getY() - entity.yOld) * partialTick
                        + entity.getBbHeight() * 0.5D,
                entity.zOld + (entity.getZ() - entity.zOld) * partialTick
        );
    }

    private static Vec3[] path(Vec3 start, Vec3 end, long seed) {
        Vec3[] points = new Vec3[SEGMENTS + 1];
        Vec3 delta = end.subtract(start);
        double amplitude = Math.min(0.65D, delta.length() * 0.075D);
        Random random = new Random(seed);
        points[0] = start;
        points[SEGMENTS] = end;
        for (int index = 1; index < SEGMENTS; index++) {
            double progress = index / (double) SEGMENTS;
            double envelope = Math.sin(Math.PI * progress) * amplitude;
            Vec3 jitter = new Vec3(
                    random.nextDouble() - 0.5D,
                    random.nextDouble() - 0.5D,
                    random.nextDouble() - 0.5D
            ).scale(envelope);
            points[index] = start.add(delta.scale(progress)).add(jitter);
        }
        return points;
    }

    private static void draw(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            Vec3[] points,
            Vec3 cameraLook,
            float width,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        for (int index = 0; index < points.length - 1; index++) {
            Vec3 start = points[index];
            Vec3 end = points[index + 1];
            Vec3 direction = end.subtract(start);
            Vec3 side = direction.cross(cameraLook);
            if (side.lengthSqr() < 1.0E-8D) {
                side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            }
            if (side.lengthSqr() < 1.0E-8D) {
                continue;
            }
            side = side.normalize().scale(width);
            vertex(
                    consumer,
                    pose.pose(),
                    pose.normal(),
                    end.subtract(side),
                    1.0F,
                    0.0F,
                    red,
                    green,
                    blue,
                    alpha
            );
            vertex(
                    consumer,
                    pose.pose(),
                    pose.normal(),
                    start.subtract(side),
                    0.0F,
                    0.0F,
                    red,
                    green,
                    blue,
                    alpha
            );
            vertex(
                    consumer,
                    pose.pose(),
                    pose.normal(),
                    start.add(side),
                    0.0F,
                    1.0F,
                    red,
                    green,
                    blue,
                    alpha
            );
            vertex(
                    consumer,
                    pose.pose(),
                    pose.normal(),
                    end.add(side),
                    1.0F,
                    1.0F,
                    red,
                    green,
                    blue,
                    alpha
            );
        }
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f pose,
            Matrix3f normal,
            Vec3 point,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        consumer.vertex(
                        pose,
                        (float) point.x,
                        (float) point.y,
                        (float) point.z
                )
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private record Bolt(
            int sourceId,
            int targetId,
            long seed,
            long createdTick
    ) {
    }
}
