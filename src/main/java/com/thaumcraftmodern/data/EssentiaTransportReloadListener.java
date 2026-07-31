package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.essentia.EssentiaTransportDefinition;
import com.thaumcraftmodern.essentia.EssentiaTransportRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EssentiaTransportReloadListener
        extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public EssentiaTransportReloadListener() {
        super(GSON, "thaumcraft/essentia_transports");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager, ProfilerFiller profiler) {
        List<EssentiaTransportDefinition> definitions = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(
                        file.getValue(), "essentia transport definition");
                ResourceLocation block = ResourceLocation.tryParse(
                        GsonHelper.getAsString(json, "block"));
                if (block == null) throw new IllegalArgumentException("Invalid block id");
                definitions.add(new EssentiaTransportDefinition(block,
                        GsonHelper.getAsBoolean(json, "canReturnEssentia", true)));
            } catch (RuntimeException ex) {
                ThaumcraftModern.LOGGER.error(
                        "Invalid essentia transport definition {}", file.getKey(), ex);
            }
        }
        EssentiaTransportRegistry.replace(definitions);
        ThaumcraftModern.LOGGER.info("Loaded {} essentia transport definitions",
                definitions.size());
    }
}
