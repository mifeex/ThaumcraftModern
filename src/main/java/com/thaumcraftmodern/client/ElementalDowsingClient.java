package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.render.ElementalOreSphereRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ElementalDowsingClient {
    private static final int WISP_ATLAS_FRAMES = 16;
    /** Half-size of the textured world-space marker in block units. */
    static final float ORE_MARKER_RADIUS = 0.5F;
    private static final List<BlockPos> READINGS = new ArrayList<>();
    private static long expiresAt;

    private ElementalDowsingClient() {}

    public static void start(BlockPos center, int radius, long durationMillis) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        READINGS.clear();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) for (int y = -radius; y <= radius; y++)
            for (int z = -radius; z <= radius; z++) {
                cursor.setWithOffset(center, x, y, z);
                if (!minecraft.level.hasChunkAt(cursor)) continue;
                BlockState state = minecraft.level.getBlockState(cursor);
                if (isOre(state)) READINGS.add(cursor.immutable());
        }
        expiresAt = System.currentTimeMillis() + durationMillis;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES
                || READINGS.isEmpty()) return;
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0L) {
            READINGS.clear();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int animationFrame = minecraft.level == null
                ? 0
                : Math.floorMod(
                        minecraft.level.getGameTime(),
                        WISP_ATLAS_FRAMES
                );
        var buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(
                ElementalOreSphereRenderType.markers(animationFrame));
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        for (BlockPos orePosition : READINGS) {
            pose.pushPose();
            pose.translate(
                    orePosition.getX() + 0.5D,
                    orePosition.getY() + 0.5D,
                    orePosition.getZ() + 0.5D
            );
            pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            renderMarker(
                    pose.last(),
                    vertices,
                    ORE_MARKER_RADIUS
            );
            pose.popPose();
        }
        pose.popPose();
        buffers.endBatch(ElementalOreSphereRenderType.markers(animationFrame));
    }

    static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES)
                || state.getTags().anyMatch(tag -> {
                    String path = tag.location().getPath();
                    return path.equals("ores") || path.startsWith("ores/");
                });
    }

    static void renderMarker(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float radius
    ) {
        renderPulseLayer(pose, vertices, radius,
                0.0F, 1.0F, 0.0F, 1.0F,
                0xAA1122);
        renderPulseLayer(pose, vertices, radius * 0.4F,
                0.0F, 1.0F, 0.0F, 1.0F,
                0xAAAA11);
    }

    private static void renderPulseLayer(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float radius,
            float u0,
            float u1,
            float v0,
            float v1,
            int rgb
    ) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        markerVertex(vertices, matrix, normal, -radius, -radius,
                u1, v1, red, green, blue);
        markerVertex(vertices, matrix, normal, -radius, radius,
                u1, v0, red, green, blue);
        markerVertex(vertices, matrix, normal, radius, radius,
                u0, v0, red, green, blue);
        markerVertex(vertices, matrix, normal, radius, -radius,
                u0, v1, red, green, blue);
    }

    private static void markerVertex(
            VertexConsumer vertices,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v,
            int red,
            int green,
            int blue
    ) {
        vertices.vertex(matrix, x, y, 0)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }
}
