package com.thaumcraftmodern.arcane;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;

public final class ArcaneShapelessRecipe implements ArcaneRecipe {
    private final ResourceLocation id;
    private final String researchId;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final ArcaneVisCost visCost;
    private final boolean simple;

    public ArcaneShapelessRecipe(
            ResourceLocation id,
            String researchId,
            NonNullList<Ingredient> ingredients,
            ItemStack result,
            ArcaneVisCost visCost
    ) {
        this.id = id;
        this.researchId = researchId;
        this.ingredients = ingredients;
        this.result = result.copy();
        this.visCost = visCost;
        this.simple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int occupied = 0;
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                occupied++;
                stacks.add(stack);
            }
        }
        if (occupied != ingredients.size()) {
            return false;
        }
        if (simple) {
            return RecipeMatcher.findMatches(stacks, ingredients) != null;
        }
        return RecipeMatcher.findMatches(stacks, ingredients) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModArcaneRecipes.ARCANE_SHAPELESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get();
    }

    @Override
    public String researchId() {
        return researchId;
    }

    @Override
    public ArcaneVisCost visCost() {
        return visCost;
    }

    public static final class Serializer implements RecipeSerializer<ArcaneShapelessRecipe> {
        @Override
        public ArcaneShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new ArcaneShapelessRecipe(
                    id,
                    ArcaneRecipeJson.researchId(json),
                    ArcaneRecipeJson.shapelessIngredients(json),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    ArcaneRecipeJson.visCost(json)
            );
        }

        @Override
        public ArcaneShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            String researchId = buffer.readUtf();
            ArcaneVisCost visCost = ArcaneVisCost.fromNetwork(buffer);
            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            return new ArcaneShapelessRecipe(
                    id,
                    researchId,
                    ingredients,
                    buffer.readItem(),
                    visCost
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ArcaneShapelessRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            buffer.writeUtf(recipe.researchId);
            recipe.visCost.toNetwork(buffer);
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }
}
