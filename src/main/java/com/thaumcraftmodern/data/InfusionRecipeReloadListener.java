package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.infusion.InfusionRecipeDefinition;
import com.thaumcraftmodern.infusion.InfusionRecipeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InfusionRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public InfusionRecipeReloadListener() {
        super(GSON, "thaumcraft/infusion_recipes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
            ResourceManager resources, ProfilerFiller profiler) {
        List<InfusionRecipeDefinition> recipes = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "infusion recipe");
                List<Ingredient> components = new ArrayList<>();
                for (JsonElement element : GsonHelper.getAsJsonArray(json, "components")) {
                    components.add(Ingredient.fromJson(element));
                }
                LinkedHashMap<String, Integer> essentia = new LinkedHashMap<>();
                JsonObject costs = GsonHelper.getAsJsonObject(json, "essentia");
                for (Map.Entry<String, JsonElement> cost : costs.entrySet()) {
                    essentia.put(cost.getKey(), GsonHelper.convertToInt(cost.getValue(), cost.getKey()));
                }
                recipes.add(new InfusionRecipeDefinition(
                        file.getKey(),
                        GsonHelper.getAsString(json, "research", ""),
                        GsonHelper.getAsInt(json, "instability", 0),
                        Ingredient.fromJson(json.get("central")),
                        components,
                        readStack(GsonHelper.getAsJsonObject(json, "result")),
                        essentia
                ));
            } catch (RuntimeException exception) {
                ThaumcraftModern.LOGGER.error("Invalid infusion recipe {}", file.getKey(), exception);
            }
        }
        InfusionRecipeRegistry.replace(recipes);
        ThaumcraftModern.LOGGER.info("Loaded {} infusion recipes", recipes.size());
    }

    private static ItemStack readStack(JsonObject json) {
        ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(json, "item"));
        if (id == null) {
            throw new IllegalArgumentException("Invalid infusion result id");
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == net.minecraft.world.item.Items.AIR) {
            throw new IllegalArgumentException("Unknown infusion result " + id);
        }
        return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
    }
}
