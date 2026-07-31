package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AspectReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AspectReloadListener() {
        super(GSON, "thaumcraft/aspects");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<AspectDefinition> definitions = new ArrayList<>();
        int inactiveCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "aspect definition");
                if (DefinitionActivation.isInactive(json)) {
                    inactiveCount++;
                    continue;
                }
                List<String> components = new ArrayList<>();
                for (JsonElement component : GsonHelper.getAsJsonArray(json, "components")) {
                    components.add(GsonHelper.convertToString(component, "component"));
                }
                definitions.add(new AspectDefinition(
                        GsonHelper.getAsString(json, "id"),
                        Integer.parseInt(GsonHelper.getAsString(json, "color"), 16),
                        GsonHelper.getAsString(json, "icon"),
                        components,
                        GsonHelper.getAsInt(json, "order", 0)
                ));
            } catch (RuntimeException ex) {
                ThaumcraftModern.LOGGER.error("Invalid aspect definition {}", file.getKey(), ex);
            }
        }
        definitions.sort(java.util.Comparator
                .comparingInt(AspectDefinition::order)
                .thenComparing(AspectDefinition::id));
        AspectRegistryRuntime.replace(definitions);
        ThaumcraftModern.LOGGER.info(
                "Loaded {} aspect definitions; skipped {} inactive definitions",
                definitions.size(),
                inactiveCount
        );
    }
}
