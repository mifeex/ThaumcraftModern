package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Buffer implementation of the charging stream from TC4's
 * {@code TileNodeRenderer} and {@code UtilsFX.drawFloatyLine}.
 */
final class ClassicNodeDrainRenderer {
    private static final int CLASSIC_LINK_QUALITY = 16;
    private static final float WIDTH = 0.15F;
    private static final float TEXTURE_SPEED = -0.02F;
    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    private final Map<UUID, DrainTint> tints = new HashMap<>();
    private ClientLevel tintLevel;

    void render(
            AuraNodeBlockEntity node,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        if (!(node.getLevel() instanceof ClientLevel level)) {
            return;
        }
        renderRitualistStreams(node, level, partialTick, poseStack, buffers);

        PrimalAspect aspect = node.drainAspect();
        Entity entity = level.getEntity(node.drainEntityId());
        if (aspect == null
                || !(entity instanceof Player player)
                || !player.isUsingItem()) {
            return;
        }

        float growth = Math.min(player.getTicksUsingItem(), 10) / 10.0F;
        if (growth <= 0.0F) {
            return;
        }

        int color = drainColor(
                level,
                node.scanIdentity().nodeId(),
                aspect
        );
        Vec3 tipDelta;
        Vec3 phaseOrigin;
        if (player == Minecraft.getInstance().player) {
            Vec3 localTip = FirstPersonWandTipTracker.resolveInNodeSpace(
                    level,
                    poseStack
            );
            if (localTip == null) {
                return;
            }
            tipDelta = localTip.subtract(0.5D, 0.5D, 0.5D);
            phaseOrigin = Vec3.atLowerCornerOf(node.getBlockPos())
                    .add(localTip);
        } else {
            Vec3 start = castingToolTipPosition(player, partialTick);
            Vec3 end = Vec3.atCenterOf(node.getBlockPos());
            tipDelta = start.subtract(end);
            phaseOrigin = start;
        }
        renderFloatyLineFromDelta(
                tipDelta,
                phaseOrigin,
                color,
                growth,
                poseStack,
                buffers
        );
    }

    private void renderRitualistStreams(
            AuraNodeBlockEntity node,
            ClientLevel level,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        BlockPos altar = node.getBlockPos().below();
        AABB area = new AABB(node.getBlockPos()).inflate(4.5D);
        for (LegacyThaumcraftMob cleric : level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                mob -> mob.kind() == LegacyMobKind.CRIMSON_CLERIC
                        && mob.isCrimsonRitualist()
                        && mob.crimsonAltarPosition()
                                .map(altar::equals)
                                .orElse(false)
        )) {
            PrimalAspect aspect = ritualAspect(level, node.getBlockPos());
            Vec3 hood = cleric.getEyePosition(partialTick)
                    .add(0.0D, 0.18D, 0.0D);
            Vec3 nodeCenter = Vec3.atCenterOf(node.getBlockPos());
            renderFloatyLineFromDelta(
                    hood.subtract(nodeCenter),
                    hood,
                    drainColor(
                            level,
                            node.scanIdentity().nodeId(),
                            aspect
                    ),
                    1.0F,
                    poseStack,
                    buffers
            );
        }
    }

    private static PrimalAspect ritualAspect(
            ClientLevel level,
            BlockPos nodePosition
    ) {
        int index = Math.floorMod(
                (int) (level.getGameTime() / 20L)
                        + nodePosition.getX()
                        + nodePosition.getZ(),
                PrimalAspect.ordered().size()
        );
        return PrimalAspect.ordered().get(index);
    }

    /**
     * TC4 4.2.3.5 {@code TileNodeRenderer} source point. The original
     * right-hand vector is preserved exactly; the lateral component is
     * mirrored for the modern off hand.
     */
    private static Vec3 castingToolTipPosition(
            Player player,
            float partialTick
    ) {
        float pitch = Mth.lerp(
                partialTick,
                player.xRotO,
                player.getXRot()
        );
        float yaw = Mth.lerp(
                partialTick,
                player.yRotO,
                player.getYRot()
        );
        float swing = Mth.sin(player.getTicksUsingItem() / 10.0F) * 10.0F;
        InteractionHand usedHand = player.getUsedItemHand();
        HumanoidArm usedArm = usedHand == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        double lateral = usedArm == HumanoidArm.RIGHT ? -0.1D : 0.1D;

        Vec3 offset = new Vec3(lateral, -0.1D, 0.5D)
                .xRot(-pitch * DEG_TO_RAD)
                .yRot(-yaw * DEG_TO_RAD)
                .yRot(-swing * 0.01F)
                .xRot(-swing * 0.015F);
        double x = Mth.lerp(partialTick, player.xo, player.getX())
                + offset.x;
        double y = Mth.lerp(partialTick, player.yo, player.getY())
                + offset.y;
        double z = Mth.lerp(partialTick, player.zo, player.getZ())
                + offset.z;

        if (player != Minecraft.getInstance().player) {
            y += player.getEyeHeight();
        }
        return new Vec3(x, y, z);
    }

    private static void renderFloatyLineFromDelta(
            Vec3 tipDelta,
            Vec3 phaseOrigin,
            int color,
            float growth,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        double deltaX = tipDelta.x;
        double deltaY = tipDelta.y;
        double deltaZ = tipDelta.z;
        float distance = (float) Math.sqrt(
                deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
        );
        float length = Math.round(distance)
                * (CLASSIC_LINK_QUALITY / 2.0F);
        int lastSample = (int) Math.floor(length * growth);
        if (length <= 0.0F || lastSample < 1) {
            return;
        }

        float time = (float) (
                System.nanoTime() / 30_000_000L % 32_767L
        );
        float phase = time / 5.0F;
        Sample[] samples = new Sample[lastSample + 1];
        for (int index = 0; index <= lastSample; index++) {
            float progress = index / length;
            float alpha = 1.0F - Math.abs(index - length / 2.0F)
                    / (length / 2.0F);
            double waveX = deltaX + Mth.sin((float) (
                    (phaseOrigin.z % 16.0D
                            + distance * (1.0F - progress)
                            * CLASSIC_LINK_QUALITY / 2.0F
                            - phase) / 4.0D
            )) * 0.5F * alpha;
            double waveY = deltaY + Mth.sin((float) (
                    (phaseOrigin.x % 16.0D
                            + distance * (1.0F - progress)
                            * CLASSIC_LINK_QUALITY / 2.0F
                            - phase) / 3.0D
            )) * 0.5F * alpha;
            double waveZ = deltaZ + Mth.sin((float) (
                    (phaseOrigin.y % 16.0D
                            + distance * (1.0F - progress)
                            * CLASSIC_LINK_QUALITY / 2.0F
                            - phase) / 2.0D
            )) * 0.5F * alpha;
            float textureU = (1.0F - progress) * distance
                    - time * TEXTURE_SPEED;
            samples[index] = new Sample(
                    0.5F + (float) (waveX * progress),
                    0.5F + (float) (waveY * progress),
                    0.5F + (float) (waveZ * progress),
                    textureU,
                    alpha
            );
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = buffers.getBuffer(
                ClassicNodeRenderTypes.drainStream()
        );
        for (int index = 0; index < lastSample; index++) {
            Sample first = samples[index];
            Sample second = samples[index + 1];
            ribbonY(consumer, matrix, first, second, color);
            ribbonX(consumer, matrix, first, second, color);
        }
    }

    private static void ribbonY(
            VertexConsumer consumer,
            Matrix4f matrix,
            Sample first,
            Sample second,
            int color
    ) {
        streamVertex(
                consumer, matrix,
                first.x(), first.y() - WIDTH, first.z(),
                first.textureU(), 1.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                first.x(), first.y() + WIDTH, first.z(),
                first.textureU(), 0.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x(), second.y() - WIDTH, second.z(),
                second.textureU(), 1.0F, color, second.alpha()
        );

        streamVertex(
                consumer, matrix,
                first.x(), first.y() + WIDTH, first.z(),
                first.textureU(), 0.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x(), second.y() - WIDTH, second.z(),
                second.textureU(), 1.0F, color, second.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x(), second.y() + WIDTH, second.z(),
                second.textureU(), 0.0F, color, second.alpha()
        );
    }

    private static void ribbonX(
            VertexConsumer consumer,
            Matrix4f matrix,
            Sample first,
            Sample second,
            int color
    ) {
        streamVertex(
                consumer, matrix,
                first.x() - WIDTH, first.y(), first.z(),
                first.textureU(), 1.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                first.x() + WIDTH, first.y(), first.z(),
                first.textureU(), 0.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x() - WIDTH, second.y(), second.z(),
                second.textureU(), 1.0F, color, second.alpha()
        );

        streamVertex(
                consumer, matrix,
                first.x() + WIDTH, first.y(), first.z(),
                first.textureU(), 0.0F, color, first.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x() - WIDTH, second.y(), second.z(),
                second.textureU(), 1.0F, color, second.alpha()
        );
        streamVertex(
                consumer, matrix,
                second.x() + WIDTH, second.y(), second.z(),
                second.textureU(), 0.0F, color, second.alpha()
        );
    }

    private static void streamVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            float alpha
    ) {
        consumer.vertex(matrix, x, y, z)
                .color(
                        color >> 16 & 0xFF,
                        color >> 8 & 0xFF,
                        color & 0xFF,
                        (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F)
                )
                .uv(u, v)
                .endVertex();
    }

    private int drainColor(
            ClientLevel level,
            UUID nodeId,
            PrimalAspect aspect
    ) {
        if (tintLevel != level) {
            tintLevel = level;
            tints.clear();
        }

        long gameTime = level.getGameTime();
        DrainTint tint = tints.get(nodeId);
        if (tint == null) {
            tint = new DrainTint(
                    0xFF,
                    0xFF,
                    0xFF,
                    aspect,
                    gameTime - 1L
            );
            tints.put(nodeId, tint);
        }

        long elapsed = Math.max(0L, gameTime - tint.lastGameTime);
        if (tint.target != aspect) {
            advanceTint(tint, Math.max(0L, elapsed - 1L));
            tint.target = aspect;
            advanceTint(tint, Math.min(1L, elapsed));
        } else {
            advanceTint(tint, elapsed);
        }
        tint.lastGameTime = gameTime;
        return tint.red << 16 | tint.green << 8 | tint.blue;
    }

    private static void advanceTint(DrainTint tint, long ticks) {
        int target = ClassicAuraNodeRenderer.aspectColor(tint.target);
        int targetRed = target >> 16 & 0xFF;
        int targetGreen = target >> 8 & 0xFF;
        int targetBlue = target & 0xFF;
        for (long tick = 0; tick < ticks; tick++) {
            tint.red = (targetRed + tint.red * 4) / 5;
            tint.green = (targetGreen + tint.green * 4) / 5;
            tint.blue = (targetBlue + tint.blue * 4) / 5;
        }
    }

    private record Sample(
            float x,
            float y,
            float z,
            float textureU,
            float alpha
    ) {
    }

    private static final class DrainTint {
        private int red;
        private int green;
        private int blue;
        private PrimalAspect target;
        private long lastGameTime;

        private DrainTint(
                int red,
                int green,
                int blue,
                PrimalAspect target,
                long lastGameTime
        ) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.target = target;
            this.lastGameTime = lastGameTime;
        }
    }
}
