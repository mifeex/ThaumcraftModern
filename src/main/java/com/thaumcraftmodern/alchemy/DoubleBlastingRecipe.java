package com.thaumcraftmodern.alchemy;

import com.google.gson.JsonObject;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class DoubleBlastingRecipe extends BlastingRecipe {
    public DoubleBlastingRecipe(
            ResourceLocation id,
            String group,
            CookingBookCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookingTime
    ) {
        super(id, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModArcaneRecipes.DOUBLE_BLASTING_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<DoubleBlastingRecipe> {
        @Override
        public DoubleBlastingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", "misc"),
                    CookingBookCategory.MISC
            );
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            ResourceLocation resultId = new ResourceLocation(GsonHelper.getAsString(json, "result"));
            Item resultItem = BuiltInRegistries.ITEM.getOptional(resultId)
                    .orElseThrow(() -> new IllegalStateException("Unknown blasting result " + resultId));
            ItemStack result = new ItemStack(resultItem, GsonHelper.getAsInt(json, "count", 2));
            return new DoubleBlastingRecipe(
                    id, group, category, ingredient, result,
                    GsonHelper.getAsFloat(json, "experience", 0.0F),
                    GsonHelper.getAsInt(json, "cookingtime", 100)
            );
        }

        @Override
        public DoubleBlastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new DoubleBlastingRecipe(
                    id,
                    buffer.readUtf(),
                    buffer.readEnum(CookingBookCategory.class),
                    Ingredient.fromNetwork(buffer),
                    buffer.readItem(),
                    buffer.readFloat(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DoubleBlastingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            recipe.getIngredients().get(0).toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem(null));
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}
