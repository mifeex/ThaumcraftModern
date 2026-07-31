package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.construction.ConstructionDefinition;
import com.thaumcraftmodern.construction.ConstructionRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ConstructionReloadListener
        extends SimpleJsonResourceReloadListener {
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    public ConstructionReloadListener() {
        super(GSON, "thaumcraft/constructions");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        List<ConstructionDefinition> definitions = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                definitions.add(parse(file.getValue()));
            } catch (RuntimeException exception) {
                ThaumcraftModern.LOGGER.error(
                        "Invalid construction definition {}",
                        file.getKey(),
                        exception
                );
            }
        }
        try {
            ConstructionRegistry.replace(definitions);
            ThaumcraftModern.LOGGER.info(
                    "Loaded {} classic construction definitions",
                    definitions.size()
            );
        } catch (RuntimeException exception) {
            ThaumcraftModern.LOGGER.error(
                    "Rejected classic construction registry",
                    exception
            );
            ConstructionRegistry.replace(List.of());
        }
    }

    private static ConstructionDefinition parse(JsonElement element) {
        JsonObject json = GsonHelper.convertToJsonObject(
                element,
                "construction definition"
        );
        JsonObject triggerJson = GsonHelper.getAsJsonObject(json, "trigger");
        ConstructionDefinition.TriggerType triggerType =
                ConstructionDefinition.TriggerType.valueOf(
                        GsonHelper.getAsString(triggerJson, "type")
                                .toUpperCase(Locale.ROOT)
                );
        ResourceLocation triggerItem = null;
        if (triggerType == ConstructionDefinition.TriggerType.ITEM) {
            String itemId = GsonHelper.getAsString(triggerJson, "item");
            triggerItem = ResourceLocation.tryParse(itemId);
            if (triggerItem == null) {
                throw new IllegalArgumentException(
                        "invalid trigger item id: " + itemId
                );
            }
        }
        int consume = GsonHelper.getAsInt(triggerJson, "consume", 0);

        LinkedHashMap<String, Integer> vis = new LinkedHashMap<>();
        if (json.has("vis")) {
            JsonObject visJson = GsonHelper.getAsJsonObject(json, "vis");
            for (Map.Entry<String, JsonElement> entry
                    : visJson.entrySet()) {
                vis.put(
                        entry.getKey(),
                        GsonHelper.convertToInt(
                                entry.getValue(),
                                "vis amount"
                        )
                );
            }
        }
        return new ConstructionDefinition(
                GsonHelper.getAsString(json, "id"),
                ConstructionDefinition.Handler.valueOf(
                        GsonHelper.getAsString(json, "handler")
                                .toUpperCase(Locale.ROOT)
                ),
                new ConstructionDefinition.Trigger(
                        triggerType,
                        triggerItem,
                        consume
                ),
                GsonHelper.getAsString(json, "research", ""),
                vis
        );
    }
}
