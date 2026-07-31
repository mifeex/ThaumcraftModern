package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.io.BufferedReader;

/**
 * Reloadable client-resource layout for the first-person Thaumometer model and
 * the independent 2D readout drawn over it.
 */
public final class ThaumometerHudLayout {
    private static final ResourceLocation RESOURCE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "config/thaumometer_hud.json"
    );
    private static final Layout DEFAULT = new Layout(
            0.0F,
            false,
            false,
            0,
            0,
            0.62F,
            -0.36F,
            -0.32F,
            -0.16F,
            0.0F,
            0.0F,
            0.0F,
            -0.2F
    );
    private static volatile Layout current = DEFAULT;

    private ThaumometerHudLayout() {
    }

    public static Layout current() {
        return current;
    }

    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Layout>() {
            @Override
            protected Layout prepare(
                    ResourceManager resourceManager,
                    ProfilerFiller profiler
            ) {
                try {
                    Resource resource = resourceManager.getResource(RESOURCE).orElseThrow(() ->
                            new IllegalStateException("Missing client resource " + RESOURCE)
                    );
                    try (BufferedReader reader = resource.openAsReader()) {
                        return parse(JsonParser.parseReader(reader).getAsJsonObject());
                    }
                } catch (Exception exception) {
                    ThaumcraftModern.LOGGER.error(
                            "Rejected Thaumometer HUD layout reload; keeping previous layout",
                            exception
                    );
                    return current;
                }
            }

            @Override
            protected void apply(
                    Layout prepared,
                    ResourceManager resourceManager,
                    ProfilerFiller profiler
            ) {
                current = prepared;
                ThaumcraftModern.LOGGER.info(
                        "Loaded Thaumometer layout: HUD rotation={} mirrorX={} mirrorY={} "
                                + "offset={},{}; model scale={} offset={},{},{} "
                                + "rotation={},{},{}; hands offset Z={}",
                        prepared.rotationDegrees,
                        prepared.mirrorX,
                        prepared.mirrorY,
                        prepared.offsetX,
                        prepared.offsetY,
                        prepared.modelScale,
                        prepared.modelOffsetX,
                        prepared.modelOffsetY,
                        prepared.modelOffsetZ,
                        prepared.modelRotationXDegrees,
                        prepared.modelRotationYDegrees,
                        prepared.modelRotationZDegrees,
                        prepared.handsOffsetZ
                );
            }
        });
    }

    static Layout parse(JsonObject json) {
        float rotationDegrees = GsonHelper.getAsFloat(json, "rotation_degrees", 0.0F);
        int offsetX = GsonHelper.getAsInt(json, "offset_x", 0);
        int offsetY = GsonHelper.getAsInt(json, "offset_y", 0);
        float modelScale = GsonHelper.getAsFloat(json, "model_scale", 0.62F);
        float modelOffsetX = GsonHelper.getAsFloat(json, "model_offset_x", -0.36F);
        float modelOffsetY = GsonHelper.getAsFloat(json, "model_offset_y", -0.32F);
        float modelOffsetZ = GsonHelper.getAsFloat(json, "model_offset_z", -0.16F);
        float modelRotationX = GsonHelper.getAsFloat(
                json,
                "model_rotation_x_degrees",
                0.0F
        );
        float modelRotationY = GsonHelper.getAsFloat(
                json,
                "model_rotation_y_degrees",
                0.0F
        );
        float modelRotationZ = GsonHelper.getAsFloat(
                json,
                "model_rotation_z_degrees",
                0.0F
        );
        float handsOffsetZ = GsonHelper.getAsFloat(json, "hands_offset_z", -0.2F);
        requireFinite(
                rotationDegrees,
                modelScale,
                modelOffsetX,
                modelOffsetY,
                modelOffsetZ,
                modelRotationX,
                modelRotationY,
                modelRotationZ,
                handsOffsetZ
        );
        if (Math.abs(offsetX) > 2_048 || Math.abs(offsetY) > 2_048) {
            throw new IllegalArgumentException("HUD offsets must be in -2048..2048");
        }
        if (modelScale < 0.01F || modelScale > 4.0F) {
            throw new IllegalArgumentException("model_scale must be in 0.01..4.0");
        }
        if (Math.abs(modelOffsetX) > 10.0F
                || Math.abs(modelOffsetY) > 10.0F
                || Math.abs(modelOffsetZ) > 10.0F) {
            throw new IllegalArgumentException("model offsets must be in -10..10");
        }
        if (Math.abs(handsOffsetZ) > 10.0F) {
            throw new IllegalArgumentException("hands_offset_z must be in -10..10");
        }
        return new Layout(
                rotationDegrees,
                GsonHelper.getAsBoolean(json, "mirror_x", false),
                GsonHelper.getAsBoolean(json, "mirror_y", false),
                offsetX,
                offsetY,
                modelScale,
                modelOffsetX,
                modelOffsetY,
                modelOffsetZ,
                modelRotationX,
                modelRotationY,
                modelRotationZ,
                handsOffsetZ
        );
    }

    private static void requireFinite(float... values) {
        for (float value : values) {
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("Thaumometer layout values must be finite");
            }
        }
    }

    public record Layout(
            float rotationDegrees,
            boolean mirrorX,
            boolean mirrorY,
            int offsetX,
            int offsetY,
            float modelScale,
            float modelOffsetX,
            float modelOffsetY,
            float modelOffsetZ,
            float modelRotationXDegrees,
            float modelRotationYDegrees,
            float modelRotationZDegrees,
            float handsOffsetZ
    ) {
    }
}
