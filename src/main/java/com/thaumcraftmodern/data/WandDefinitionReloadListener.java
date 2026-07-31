package com.thaumcraftmodern.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.wand.WandCapDefinition;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.wand.WandRodDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads the minimal rod/cap catalog used by actual wand stacks.
 */
public final class WandDefinitionReloadListener
        extends SimpleJsonResourceReloadListener {
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    public WandDefinitionReloadListener() {
        super(GSON, "thaumcraft/wands");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        List<WandRodDefinition> rods = new ArrayList<>();
        List<WandCapDefinition> caps = new ArrayList<>();
        boolean invalid = false;
        for (Map.Entry<ResourceLocation, JsonElement> file : objects.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(
                        file.getValue(),
                        "wand component definition"
                );
                if (DefinitionActivation.isInactive(json)) {
                    continue;
                }
                String kind = GsonHelper.getAsString(json, "kind");
                String id = GsonHelper.getAsString(json, "id");
                String translationKey = GsonHelper.getAsString(
                        json,
                        "translation_key"
                );
                switch (kind) {
                    case "rod" -> rods.add(readRod(
                            json,
                            id,
                            translationKey
                    ));
                    case "cap" -> caps.add(readCap(
                            json,
                            id,
                            translationKey
                    ));
                    default -> throw new IllegalArgumentException(
                            "unknown wand component kind: " + kind
                    );
                }
            } catch (RuntimeException exception) {
                invalid = true;
                ThaumcraftModern.LOGGER.error(
                        "Invalid wand component definition {}",
                        file.getKey(),
                        exception
                );
            }
        }
        if (invalid) {
            ThaumcraftModern.LOGGER.error(
                    "Rejected wand component reload because one or more definitions are invalid"
            );
            return;
        }
        try {
            WandComponentRegistry.replace(rods, caps);
            ThaumcraftModern.LOGGER.info(
                    "Loaded {} wand rods and {} wand caps",
                    rods.size(),
                    caps.size()
            );
        } catch (RuntimeException exception) {
            ThaumcraftModern.LOGGER.error(
                    "Rejected wand component reload; previous catalog remains active",
                    exception
            );
        }
    }

    private static WandRodDefinition readRod(
            JsonObject json,
            String id,
            String translationKey
    ) {
        List<String> rechargeAspects = new ArrayList<>();
        if (json.has("recharge_aspects")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(
                    json,
                    "recharge_aspects"
            )) {
                rechargeAspects.add(GsonHelper.convertToString(
                        element,
                        "wand rod recharge aspect"
                ));
            }
        }
        return new WandRodDefinition(
                id,
                GsonHelper.getAsInt(json, "capacity_vis"),
                translationKey,
                rechargeAspects,
                GsonHelper.getAsInt(json, "recharge_interval_ticks", 0),
                GsonHelper.getAsInt(json, "recharge_centivis", 0),
                GsonHelper.getAsBoolean(json, "staff", false),
                GsonHelper.getAsBoolean(json, "runes", false)
        );
    }

    private static WandCapDefinition readCap(
            JsonObject json,
            String id,
            String translationKey
    ) {
        List<String> specialAspects = new ArrayList<>();
        if (json.has("special_aspects")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(
                    json,
                    "special_aspects"
            )) {
                specialAspects.add(GsonHelper.convertToString(
                        element,
                        "wand cap special aspect"
                ));
            }
        }
        float baseModifier = GsonHelper.getAsFloat(json, "cost_modifier");
        return new WandCapDefinition(
                id,
                baseModifier,
                translationKey,
                specialAspects,
                GsonHelper.getAsFloat(
                        json,
                        "special_cost_modifier",
                        baseModifier
                )
        );
    }
}
