package com.thaumcraftmodern.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ElementalDowsingClient {
    static final float ORE_RED = 1.0F;
    static final float ORE_GREEN = 0.34F;
    static final float ORE_BLUE = 0.02F;
    static final int PARTICLES_PER_ORE = 5;
    private static final List<Reading> READINGS = new ArrayList<>();
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
                Kind kind = isOre(state) ? Kind.ORE
                        : state.getFluidState().is(FluidTags.WATER) ? Kind.WATER
                        : state.getFluidState().is(FluidTags.LAVA) ? Kind.LAVA : null;
                if (kind != null) READINGS.add(new Reading(cursor.immutable(), kind));
            }
        expiresAt = System.currentTimeMillis() + durationMillis;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || READINGS.isEmpty()) return;
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0L) {
            READINGS.clear();
            return;
        }
        float fade = Mth.clamp(remaining / 750.0F, 0.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        var buffers = minecraft.renderBuffers().bufferSource();
        var vertices = buffers.getBuffer(RenderType.lines());
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        for (Reading reading : READINGS) {
            if (reading.kind == Kind.ORE) {
                renderOreParticles(pose, vertices, reading.position, fade, remaining);
            } else {
                float red = reading.kind == Kind.LAVA ? 1.0F : 0.24F;
                float green = reading.kind == Kind.LAVA ? 0.33F : 0.83F;
                float blue = reading.kind == Kind.LAVA ? 0.02F : 0.74F;
                LevelRenderer.renderLineBox(pose, vertices,
                        new AABB(reading.position).inflate(0.002D),
                        red, green, blue, fade * 0.85F);
            }
        }
        buffers.endBatch(RenderType.lines());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pose.popPose();
    }

    static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES)
                || state.getTags().anyMatch(tag -> {
                    String path = tag.location().getPath();
                    return path.equals("ores") || path.startsWith("ores/");
                });
    }

    private static void renderOreParticles(
            PoseStack pose,
            com.mojang.blaze3d.vertex.VertexConsumer vertices,
            BlockPos position,
            float fade,
            long remaining
    ) {
        long animationStep = remaining / 90L;
        for (int index = 0; index < PARTICLES_PER_ORE; index++) {
            long seed = position.asLong() * 31L + index * 0x9E3779B9L + animationStep;
            double x = position.getX() + 0.16D + unit(seed) * 0.68D;
            double y = position.getY() + 0.16D + unit(seed * 17L + 3L) * 0.68D;
            double z = position.getZ() + 0.16D + unit(seed * 37L + 7L) * 0.68D;
            double size = 0.025D + unit(seed * 53L + 11L) * 0.025D;
            LevelRenderer.renderLineBox(
                    pose,
                    vertices,
                    new AABB(x - size, y - size, z - size,
                            x + size, y + size, z + size),
                    ORE_RED,
                    ORE_GREEN,
                    ORE_BLUE,
                    fade
            );
        }
    }

    private static double unit(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return (value & 0xFFFFL) / 65535.0D;
    }

    private enum Kind { ORE, WATER, LAVA }
    private record Reading(BlockPos position, Kind kind) {}
}
