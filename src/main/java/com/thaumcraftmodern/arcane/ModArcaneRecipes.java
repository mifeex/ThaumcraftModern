package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModArcaneRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ThaumcraftModern.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<RecipeType<ArcaneRecipe>> ARCANE_CRAFTING_TYPE =
            RECIPE_TYPES.register(
                    "arcane_crafting",
                    () -> RecipeType.simple(
                            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_crafting")
                    )
            );
    public static final RegistryObject<RecipeSerializer<ArcaneShapedRecipe>>
            ARCANE_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_shaped",
                    ArcaneShapedRecipe.Serializer::new
            );
    public static final RegistryObject<RecipeSerializer<ArcaneShapelessRecipe>>
            ARCANE_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "arcane_shapeless",
                    ArcaneShapelessRecipe.Serializer::new
            );

    private ModArcaneRecipes() {
    }

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
