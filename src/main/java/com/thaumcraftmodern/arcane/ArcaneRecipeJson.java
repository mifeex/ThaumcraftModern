package com.thaumcraftmodern.arcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.Map;

final class ArcaneRecipeJson {
    private ArcaneRecipeJson() {
    }

    static String researchId(JsonObject json) {
        String value = GsonHelper.getAsString(json, "research");
        if (value.isBlank()) {
            throw new JsonSyntaxException("arcane recipe research id cannot be blank");
        }
        return value;
    }

    static ArcaneVisCost visCost(JsonObject json) {
        return json.has("vis")
                ? ArcaneVisCost.fromJson(GsonHelper.getAsJsonObject(json, "vis"))
                : ArcaneVisCost.EMPTY;
    }

    static String[] pattern(JsonObject json) {
        JsonArray array = GsonHelper.getAsJsonArray(json, "pattern");
        if (array.isEmpty() || array.size() > 3) {
            throw new JsonSyntaxException("arcane shaped pattern must contain 1 to 3 rows");
        }
        String[] rows = new String[array.size()];
        int width = -1;
        for (int row = 0; row < rows.length; row++) {
            rows[row] = GsonHelper.convertToString(array.get(row), "pattern[" + row + "]");
            if (rows[row].isEmpty() || rows[row].length() > 3) {
                throw new JsonSyntaxException("arcane shaped row must contain 1 to 3 columns");
            }
            if (width == -1) {
                width = rows[row].length();
            } else if (rows[row].length() != width) {
                throw new JsonSyntaxException("all arcane shaped pattern rows must have the same width");
            }
        }
        return rows;
    }

    static NonNullList<Ingredient> ingredients(String[] pattern, JsonObject keyJson) {
        Map<Character, Ingredient> key = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : keyJson.entrySet()) {
            String symbol = entry.getKey();
            if (symbol.length() != 1 || symbol.charAt(0) == ' ') {
                throw new JsonSyntaxException("arcane recipe key must be one non-space character");
            }
            key.put(symbol.charAt(0), Ingredient.fromJson(entry.getValue()));
        }

        NonNullList<Ingredient> ingredients =
                NonNullList.withSize(pattern.length * pattern[0].length(), Ingredient.EMPTY);
        for (int row = 0; row < pattern.length; row++) {
            for (int column = 0; column < pattern[row].length(); column++) {
                char symbol = pattern[row].charAt(column);
                Ingredient ingredient = symbol == ' ' ? Ingredient.EMPTY : key.get(symbol);
                if (ingredient == null) {
                    throw new JsonSyntaxException("pattern references undefined key: " + symbol);
                }
                ingredients.set(column + row * pattern[row].length(), ingredient);
            }
        }
        return ingredients;
    }

    static NonNullList<Ingredient> shapelessIngredients(JsonObject json) {
        JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
        if (array.isEmpty() || array.size() > 9) {
            throw new JsonSyntaxException("arcane shapeless recipe must contain 1 to 9 ingredients");
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (JsonElement element : array) {
            Ingredient ingredient = Ingredient.fromJson(element);
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            throw new JsonSyntaxException("arcane shapeless recipe has no usable ingredients");
        }
        return ingredients;
    }
}
