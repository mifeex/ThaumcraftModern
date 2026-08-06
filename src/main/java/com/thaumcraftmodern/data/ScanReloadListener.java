package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.scan.ScanDefinition;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanTargetType;
import com.thaumcraftmodern.scan.RuntimeRecipeScanGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ScanReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ScanReloadListener() {
        super(GSON, "thaumcraft/scans");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<ScanDefinition> definitions = new ArrayList<>();
        int inactiveCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = LegacyScanMappings.map(
                        GsonHelper.convertToJsonObject(
                                file.getValue(),
                                "scan definition"
                        )
                );
                if (DefinitionActivation.isInactive(json)) {
                    inactiveCount++;
                    continue;
                }
                List<AspectReward> aspects = new ArrayList<>();
                if (json.has("aspects")) {
                    for (JsonElement aspect : GsonHelper.getAsJsonArray(json, "aspects")) {
                        if (aspect.isJsonPrimitive()) {
                            aspects.add(new AspectReward(
                                    GsonHelper.convertToString(aspect, "aspect"),
                                    1
                            ));
                        } else {
                            JsonObject reward = GsonHelper.convertToJsonObject(aspect, "aspect reward");
                            aspects.add(new AspectReward(
                                    GsonHelper.getAsString(reward, "id"),
                                    GsonHelper.getAsInt(reward, "amount", 1)
                            ));
                        }
                    }
                }
                definitions.add(new ScanDefinition(
                        ScanTargetType.valueOf(
                                GsonHelper.getAsString(json, "type").toUpperCase(Locale.ROOT)
                        ),
                        GsonHelper.getAsString(json, "target"),
                        GsonHelper.getAsString(json, "display", ""),
                        aspects,
                        json.has("knowledge_key")
                                ? GsonHelper.getAsString(json, "knowledge_key")
                                : null
                ));
            } catch (RuntimeException ex) {
                ThaumcraftModern.LOGGER.error("Invalid scan definition {}", file.getKey(), ex);
            }
        }
        ScanRegistry.replace(definitions);
        RuntimeRecipeScanGenerator.invalidate();
        ThaumcraftModern.LOGGER.info(
                "Loaded {} explicit scan definitions; skipped {} inactive definitions; "
                        + "automatic fallback is {} (saved inferred scan keys remain readable)",
                definitions.size(),
                inactiveCount,
                ThaumcraftModernServerConfig.automaticScanFallback()
                        ? "enabled"
                        : "disabled"
        );
    }
}
