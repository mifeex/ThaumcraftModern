package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CrucibleRecipeReloadListener
        extends SimpleJsonResourceReloadListener {
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    public CrucibleRecipeReloadListener() {
        super(GSON, "thaumcraft/crucible_recipes");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        List<CrucibleRecipeDefinition> recipes = new ArrayList<>();
        objects.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(file -> {
                    try {
                        JsonObject json = GsonHelper.convertToJsonObject(
                                file.getValue(),
                                "crucible recipe"
                        );
                        if (DefinitionActivation.isInactive(json)) {
                            return;
                        }
                        recipes.add(new CrucibleRecipeDefinition(
                                file.getKey(),
                                GsonHelper.getAsString(json, "research", ""),
                                Ingredient.fromJson(json.get("catalyst")),
                                json.getAsJsonObject("catalyst").has("aspect")
                                        ? GsonHelper.getAsString(json.getAsJsonObject("catalyst"), "aspect")
                                        : "",
                                ShapedRecipe.itemStackFromJson(
                                        GsonHelper.getAsJsonObject(json, "output")
                                ),
                                readAspects(json)
                        ));
                    } catch (RuntimeException exception) {
                        ThaumcraftModern.LOGGER.error(
                                "Invalid Crucible recipe {}",
                                file.getKey(),
                                exception
                        );
                    }
                });
        CrucibleRecipeRegistry.replace(recipes);
        ThaumcraftModern.LOGGER.info(
                "Loaded {} active Crucible recipes",
                recipes.size()
        );
    }

    static Map<String, Integer> readAspects(JsonObject json) {
        JsonObject raw = GsonHelper.getAsJsonObject(json, "aspects");
        LinkedHashMap<String, Integer> aspects = new LinkedHashMap<>();
        raw.entrySet().forEach(entry -> aspects.put(
                entry.getKey(),
                GsonHelper.convertToInt(entry.getValue(), entry.getKey())
        ));
        return aspects;
    }
}
