package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.network.packet.NodeZapPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
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

/** Continuous textured equivalent of TC4 PacketFXBlockZap/nodeBolt. */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientNodeZapRenderer {
    static final int LIFETIME_TICKS = 10;
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

    private ClientNodeZapRenderer() {
    }

    public static void accept(NodeZapPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        BOLTS.add(new Bolt(
                Vec3.atCenterOf(packet.from()),
                Vec3.atCenterOf(packet.to()),
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
        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        var direction = camera.getLookVector();
        Vec3 cameraLook = new Vec3(direction.x(), direction.y(), direction.z());
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();
        RenderType largeType = ClassicBoltRenderTypes.bolt(LARGE, false);
        RenderType smallType = ClassicBoltRenderTypes.bolt(SMALL, true);

        pose.pushPose();
        pose.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        Iterator<Bolt> iterator = BOLTS.iterator();
        while (iterator.hasNext()) {
            Bolt bolt = iterator.next();
            long age = now - bolt.createdTick;
            if (age < 0L || age >= LIFETIME_TICKS) {
                iterator.remove();
                continue;
            }
            Vec3[] points = path(bolt.from, bolt.to, bolt.seed + age * 31L);
            float fade = Math.max(
                    0.0F,
                    1.0F - (age + event.getPartialTick()) / LIFETIME_TICKS
            );
            draw(buffers.getBuffer(largeType), pose.last(), points,
                    cameraLook, 0.03F, fade * 0.35F,
                    0.75F, 1.0F, 1.0F);
            draw(buffers.getBuffer(smallType), pose.last(), points,
                    cameraLook, 0.018F, fade * 0.8F,
                    0.75F, 1.0F, 1.0F);
        }
        buffers.endBatch(largeType);
        buffers.endBatch(smallType);
        pose.popPose();
    }

    private static Vec3[] path(Vec3 start, Vec3 end, long seed) {
        Vec3[] points = new Vec3[SEGMENTS + 1];
        Vec3 delta = end.subtract(start);
        Random random = new Random(seed);
        points[0] = start;
        points[SEGMENTS] = end;
        for (int index = 1; index < SEGMENTS; index++) {
            double progress = index / (double) SEGMENTS;
            double envelope = Math.sin(Math.PI * progress) * 0.28D;
            points[index] = start.add(delta.scale(progress)).add(
                    new Vec3(
                            random.nextDouble() - 0.5D,
                            random.nextDouble() - 0.5D,
                            random.nextDouble() - 0.5D
                    ).scale(envelope)
            );
        }
        return points;
    }

    private static void draw(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3[] points,
            Vec3 cameraLook,
            float width,
            float alpha,
            float red,
            float green,
            float blue
    ) {
        Vec3[] offsets = new Vec3[points.length];
        for (int index = 0; index < points.length; index++) {
            Vec3 tangent = index == 0
                    ? points[1].subtract(points[0])
                    : index == points.length - 1
                    ? points[index].subtract(points[index - 1])
                    : points[index + 1].subtract(points[index - 1]);
            Vec3 side = tangent.cross(cameraLook);
            if (side.lengthSqr() < 1.0E-8D) {
                side = tangent.cross(new Vec3(0.0D, 1.0D, 0.0D));
            }
            offsets[index] = side.lengthSqr() < 1.0E-8D
                    ? new Vec3(width, 0.0D, 0.0D)
                    : side.normalize().scale(width);
        }
        for (int index = 0; index < points.length - 1; index++) {
            Vec3 start = points[index];
            Vec3 end = points[index + 1];
            Vec3 startOffset = offsets[index];
            Vec3 endOffset = offsets[index + 1];
            vertex(out, pose, end.subtract(endOffset), 0.5F, 0,
                    red, green, blue, alpha);
            vertex(out, pose, start.subtract(startOffset), 0.5F, 0,
                    red, green, blue, alpha);
            vertex(out, pose, start.add(startOffset), 0.5F, 1,
                    red, green, blue, alpha);
            vertex(out, pose, end.add(endOffset), 0.5F, 1,
                    red, green, blue, alpha);
        }
    }

    private static void vertex(
            VertexConsumer out,
            PoseStack.Pose pose,
            Vec3 point,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        out.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private record Bolt(Vec3 from, Vec3 to, long seed, long createdTick) {
    }
}
