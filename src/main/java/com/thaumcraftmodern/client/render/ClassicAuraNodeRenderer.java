package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.NodeVisibilityService;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalAspectColors;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern buffer implementation of TC4 4.2.3.5's {@code TileNodeRenderer}.
 */
final class ClassicAuraNodeRenderer {
    /**
     * TC4 4.2.3.5 renders revealed world nodes and jarred nodes up to 64
     * blocks away.
     */
    static final int VIEW_DISTANCE = 64;
    /**
     * The original renderer narrows the range to 48 blocks when the held
     * Thaumometer is the only source revealing the node.
     */
    static final int THAUMOMETER_VIEW_DISTANCE = 48;

    private static final int FRAME_COUNT = 32;
    private static final int NORMAL_NODE_ROW = 1;
    private static final int CLASSIC_BRIGHTNESS = 220;
    private static final int SUBTLE_NODE_COLOR = 0xFFFFFF;
    /**
     * Barely-visible hint for a node when the player has neither revealing
     * goggles nor a held Thaumometer.
     */
    static final float SUBTLE_NODE_ALPHA = 0.10F;
    private static final float SUBTLE_NODE_SIZE = 0.5F;
    private static final float TAU = (float) (Math.PI * 2.0D);

    private ClassicAuraNodeRenderer() {
    }

    static void renderWorldNode(
            AuraNodeState state,
            BlockPos position,
            NodeVisibilityService.Visibility visibility,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        boolean revealed = visibility.revealed();
        Level level = Minecraft.getInstance().level;
        float hitFlash = level == null
                ? 0.0F
                : ClientAuraNodeHitFeedback.flashStrength(level, position);
        render(
                state,
                position,
                revealed,
                revealed,
                viewDistanceFor(visibility),
                1.0F,
                0.5D,
                partialTick,
                poseStack,
                buffers,
                hitFlash
        );
    }

    static int viewDistanceFor(
            NodeVisibilityService.Visibility visibility
    ) {
        return visibility
                == NodeVisibilityService.Visibility.REVEALED_BY_THAUMOMETER
                ? THAUMOMETER_VIEW_DISTANCE
                : VIEW_DISTANCE;
    }

    static void renderJarNode(
            AuraNodeState state,
            BlockPos position,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        render(
                state,
                position,
                true,
                false,
                VIEW_DISTANCE,
                0.7F,
                0.4D,
                partialTick,
                poseStack,
                buffers,
                0.0F
        );
    }

    /**
     * Exact fixed-plane item pass used by TC4's {@code ItemJarNodeRenderer}.
     * The original draws the same node three times, rotating the latter two
     * planes by 90 degrees around Y and then X.
     */
    static void renderJarItemNode(
            AuraNodeState state,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        AuraNodeState.Snapshot snapshot = state.snapshot();
        List<AspectLayer> aspects = activeAspects(snapshot);
        if (aspects.isEmpty()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        int tickCount = player == null ? 0 : player.tickCount;
        float alpha = 0.5F * modifierAlpha(
                snapshot.modifier(),
                tickCount
        );
        int frame = Math.floorMod(
                System.nanoTime() / 40_000_000L + 1L,
                FRAME_COUNT
        );

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.4D, 0.5D);
        renderItemNodePlane(
                snapshot,
                aspects,
                tickCount,
                frame,
                alpha,
                poseStack,
                buffers
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderItemNodePlane(
                snapshot,
                aspects,
                tickCount,
                frame,
                alpha,
                poseStack,
                buffers
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        renderItemNodePlane(
                snapshot,
                aspects,
                tickCount,
                frame,
                alpha,
                poseStack,
                buffers
        );
        poseStack.popPose();
    }

    private static void render(
            AuraNodeState state,
            BlockPos position,
            boolean revealed,
            boolean seeThrough,
            double range,
            float scale,
            double centerY,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            float hitFlash
    ) {
        AuraNodeState.Snapshot snapshot = state.snapshot();
        List<AspectLayer> aspects = activeAspects(snapshot);

        if (!revealed || aspects.isEmpty()) {
            renderSubtleHint(
                    position,
                    centerY,
                    hitFlash,
                    poseStack,
                    buffers
            );
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        double distance = Math.sqrt(player.distanceToSqr(
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D
        ));
        if (distance > range) {
            return;
        }

        float alpha = (float) ((range - distance) / range);
        alpha *= modifierAlpha(snapshot.modifier(), player.tickCount);

        long now = System.nanoTime();
        int frame = Math.floorMod(
                now / 40_000_000L + position.getX(),
                FRAME_COUNT
        );
        float ticks = player.tickCount;
        int aspectCount = aspects.size();
        int totalVis = 0;
        float lastRotation = 0.0F;

        for (int index = 0; index < aspectCount; index++) {
            AspectLayer aspect = aspects.get(index);
            totalVis += aspect.amount();

            float pulse = Mth.sin(ticks / (14.0F - index)) * 0.25F + 0.5F;
            float size = (
                    0.2F + pulse * (aspect.amount() / 50.0F)
            ) * scale;

            long period = 5_000L + 500L * index;
            lastRotation = (float) (
                    (now / 5_000_000L % period)
                            / (double) period
                            * TAU
            );

            float layerAlpha = alpha;
            if (!aspect.additive()) {
                layerAlpha *= 1.5F;
            }
            layerAlpha /= Math.max(1.0F, aspectCount / 2.0F);

            renderSprite(
                    poseStack,
                    buffers,
                    0.5D,
                    centerY,
                    0.5D,
                    lastRotation,
                    size,
                    layerAlpha,
                    0,
                    frame,
                    aspect.color(),
                    aspect.additive(),
                    seeThrough
            );
        }

        float averageVis = totalVis / (float) aspectCount;
        float coreSize = (0.1F + averageVis / 150.0F) * scale;
        if (snapshot.type() == com.thaumcraftmodern.aura.AuraNodeType.HUNGRY) {
            coreSize *= 0.75F;
        }
        renderSprite(
                poseStack,
                buffers,
                0.5D,
                centerY,
                0.5D,
                lastRotation,
                coreSize,
                alpha,
                typeRow(snapshot.type()),
                frame,
                0xFFFFFF,
                typeAdditive(snapshot.type()),
                seeThrough
        );
        if (hitFlash > 0.0F) {
            renderSprite(
                    poseStack,
                    buffers,
                    0.5D,
                    centerY,
                    0.5D,
                    lastRotation,
                    Math.max(0.28F, coreSize * 1.45F),
                    0.48F * hitFlash,
                    NORMAL_NODE_ROW,
                    frame,
                    0xFFFFFF,
                    true,
                    seeThrough
            );
        }
    }

    private static void renderItemNodePlane(
            AuraNodeState.Snapshot snapshot,
            List<AspectLayer> aspects,
            int tickCount,
            int frame,
            float alpha,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        int aspectCount = aspects.size();
        int totalVis = 0;

        for (int index = 0; index < aspectCount; index++) {
            AspectLayer aspect = aspects.get(index);
            totalVis += aspect.amount();

            float pulse = Mth.sin(
                    tickCount / (14.0F - index)
            ) * 0.25F + 0.5F;
            float diameter =
                    0.2F + pulse * (aspect.amount() / 50.0F);
            float layerAlpha = alpha;
            if (!aspect.additive()) {
                layerAlpha *= 1.5F;
            }

            renderFixedSprite(
                    poseStack,
                    buffers,
                    diameter * 0.5F,
                    layerAlpha / aspectCount,
                    0,
                    frame,
                    aspect.color(),
                    aspect.additive()
            );
        }

        float averageVis = totalVis / (float) aspectCount;
        float diameter = 0.1F + averageVis / 150.0F;
        if (snapshot.type() == com.thaumcraftmodern.aura.AuraNodeType.HUNGRY) {
            diameter *= 0.75F;
        }
        renderFixedSprite(
                poseStack,
                buffers,
                diameter * 0.5F,
                alpha,
                typeRow(snapshot.type()),
                frame,
                0xFFFFFF,
                typeAdditive(snapshot.type())
        );
    }

    private static void renderSubtleHint(
            BlockPos position,
            double centerY,
            float hitFlash,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        int frame = Math.floorMod(
                System.nanoTime() / 40_000_000L + position.getX(),
                FRAME_COUNT
        );
        renderSprite(
                poseStack,
                buffers,
                0.5D,
                centerY,
                0.5D,
                0.0F,
                SUBTLE_NODE_SIZE,
                SUBTLE_NODE_ALPHA,
                NORMAL_NODE_ROW,
                frame,
                SUBTLE_NODE_COLOR,
                true,
                false
        );
        if (hitFlash > 0.0F) {
            renderSprite(
                    poseStack,
                    buffers,
                    0.5D,
                    centerY,
                    0.5D,
                    0.0F,
                    SUBTLE_NODE_SIZE + hitFlash * 0.16F,
                    0.28F * hitFlash,
                    NORMAL_NODE_ROW,
                    frame,
                    0xFFFFFF,
                    true,
                    false
            );
        }
    }

    private static List<AspectLayer> activeAspects(
            AuraNodeState.Snapshot snapshot
    ) {
        List<AspectLayer> result =
                new ArrayList<>(snapshot.aspectsCurrent().size());
        for (var entry : snapshot.aspectsCurrent().entrySet()) {
            int amount = entry.getValue();
            if (amount > 0) {
                result.add(new AspectLayer(
                        amount,
                        AspectRegistryRuntime.find(entry.getKey())
                                .map(definition -> definition.color())
                                .orElse(0xFFFFFF),
                        !"perditio".equals(entry.getKey())
                ));
            }
        }
        return result;
    }

    private static int typeRow(
            com.thaumcraftmodern.aura.AuraNodeType type
    ) {
        return switch (type) {
            case NORMAL -> 1;
            case DARK -> 2;
            case HUNGRY -> 3;
            case PURE -> 4;
            case TAINTED -> 5;
            case UNSTABLE -> 6;
        };
    }

    private static boolean typeAdditive(
            com.thaumcraftmodern.aura.AuraNodeType type
    ) {
        return type != com.thaumcraftmodern.aura.AuraNodeType.DARK
                && type != com.thaumcraftmodern.aura.AuraNodeType.TAINTED;
    }

    static int aspectColor(PrimalAspect aspect) {
        return PrimalAspectColors.color(aspect);
    }

    private static float modifierAlpha(
            AuraNodeModifier modifier,
            int tickCount
    ) {
        return switch (modifier) {
            case BRIGHT -> 1.5F;
            case PALE -> 0.66F;
            case FADING -> Mth.sin(tickCount / 3.0F) * 0.25F
                    + 0.33F;
            case NORMAL -> 1.0F;
        };
    }

    private static void renderSprite(
            PoseStack poseStack,
            MultiBufferSource buffers,
            double x,
            double y,
            double z,
            float rotation,
            float radius,
            float alpha,
            int row,
            int frame,
            int color,
            boolean additive,
            boolean seeThrough
    ) {
        if (radius <= 0.0F || alpha <= 0.0F) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(
                Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .cameraOrientation()
        );
        poseStack.mulPose(Axis.ZP.rotation(rotation));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer consumer = buffers.getBuffer(
                ClassicNodeRenderTypes.node(additive, seeThrough)
        );

        float u0 = frame / (float) FRAME_COUNT;
        float u1 = (frame + 1) / (float) FRAME_COUNT;
        float v0 = row / (float) FRAME_COUNT;
        float v1 = (row + 1) / (float) FRAME_COUNT;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int alphaByte = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);

        vertex(
                consumer,
                matrix,
                normal,
                -radius,
                -radius,
                u1,
                v1,
                red,
                green,
                blue,
                alphaByte
        );
        vertex(
                consumer,
                matrix,
                normal,
                -radius,
                radius,
                u1,
                v0,
                red,
                green,
                blue,
                alphaByte
        );
        vertex(
                consumer,
                matrix,
                normal,
                radius,
                radius,
                u0,
                v0,
                red,
                green,
                blue,
                alphaByte
        );
        vertex(
                consumer,
                matrix,
                normal,
                radius,
                -radius,
                u0,
                v1,
                red,
                green,
                blue,
                alphaByte
        );
        poseStack.popPose();
    }

    private static void renderFixedSprite(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float radius,
            float alpha,
            int row,
            int frame,
            int color,
            boolean additive
    ) {
        if (radius <= 0.0F || alpha <= 0.0F) {
            return;
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer consumer = buffers.getBuffer(
                ClassicNodeRenderTypes.node(additive, false)
        );

        float u0 = frame / (float) FRAME_COUNT;
        float u1 = (frame + 1) / (float) FRAME_COUNT;
        float v0 = row / (float) FRAME_COUNT;
        float v1 = (row + 1) / (float) FRAME_COUNT;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int alphaByte = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);

        fixedVertex(
                consumer, matrix, normal,
                -radius, radius, u0, v1,
                red, green, blue, alphaByte
        );
        fixedVertex(
                consumer, matrix, normal,
                radius, radius, u1, v1,
                red, green, blue, alphaByte
        );
        fixedVertex(
                consumer, matrix, normal,
                radius, -radius, u1, v0,
                red, green, blue, alphaByte
        );
        fixedVertex(
                consumer, matrix, normal,
                -radius, -radius, u0, v0,
                red, green, blue, alphaByte
        );
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(CLASSIC_BRIGHTNESS)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    private static void fixedVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(CLASSIC_BRIGHTNESS)
                .normal(normal, 0.0F, 0.0F, -1.0F)
                .endVertex();
    }

    private record AspectLayer(int amount, int color, boolean additive) {
    }
}
