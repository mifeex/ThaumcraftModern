package com.thaumcraftmodern.network.packet;

import com.thaumcraftmodern.client.ClientPacketHandlers;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Server-authoritative TC4 recipe list for the currently open Thaumatorium. */
public record ThaumatoriumRecipeSyncPacket(
        int containerId,
        List<CrucibleRecipeDefinition> recipes,
        List<ResourceLocation> craftableRecipes,
        List<ResourceLocation> selectedRecipes,
        int formulaCapacity,
        ResourceLocation displayedRecipe
) {
    public ThaumatoriumRecipeSyncPacket {
        recipes = List.copyOf(recipes);
        craftableRecipes = List.copyOf(craftableRecipes);
        selectedRecipes = List.copyOf(selectedRecipes);
        formulaCapacity = Math.max(1, formulaCapacity);
    }

    public static void encode(
            ThaumatoriumRecipeSyncPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(packet.containerId());
        buffer.writeVarInt(packet.recipes().size());
        for (CrucibleRecipeDefinition recipe : packet.recipes()) {
            buffer.writeResourceLocation(recipe.id());
            buffer.writeItem(recipe.output());
            buffer.writeVarInt(recipe.aspects().size());
            recipe.aspects().forEach((aspect, amount) -> {
                buffer.writeUtf(aspect);
                buffer.writeVarInt(amount);
            });
        }
        buffer.writeVarInt(packet.craftableRecipes().size());
        packet.craftableRecipes().forEach(buffer::writeResourceLocation);
        buffer.writeVarInt(packet.selectedRecipes().size());
        packet.selectedRecipes().forEach(buffer::writeResourceLocation);
        buffer.writeVarInt(packet.formulaCapacity());
        buffer.writeBoolean(packet.displayedRecipe() != null);
        if (packet.displayedRecipe() != null) {
            buffer.writeResourceLocation(packet.displayedRecipe());
        }
    }

    public static ThaumatoriumRecipeSyncPacket decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        int recipeCount = buffer.readVarInt();
        List<CrucibleRecipeDefinition> recipes = new ArrayList<>(recipeCount);
        for (int index = 0; index < recipeCount; index++) {
            ResourceLocation id = buffer.readResourceLocation();
            ItemStack output = buffer.readItem();
            int aspectCount = buffer.readVarInt();
            Map<String, Integer> aspects = new LinkedHashMap<>();
            for (int aspectIndex = 0; aspectIndex < aspectCount; aspectIndex++) {
                aspects.put(buffer.readUtf(), buffer.readVarInt());
            }
            recipes.add(new CrucibleRecipeDefinition(
                    id, "", Ingredient.EMPTY, output, aspects));
        }
        int craftableCount = buffer.readVarInt();
        List<ResourceLocation> craftable = new ArrayList<>(craftableCount);
        for (int index = 0; index < craftableCount; index++) {
            craftable.add(buffer.readResourceLocation());
        }
        int selectedCount = buffer.readVarInt();
        List<ResourceLocation> selected = new ArrayList<>(selectedCount);
        for (int index = 0; index < selectedCount; index++) {
            selected.add(buffer.readResourceLocation());
        }
        int capacity = buffer.readVarInt();
        ResourceLocation displayed = buffer.readBoolean()
                ? buffer.readResourceLocation() : null;
        return new ThaumatoriumRecipeSyncPacket(
                containerId, recipes, craftable, selected, capacity, displayed);
    }

    public static void handle(
            ThaumatoriumRecipeSyncPacket packet,
            Supplier<NetworkEvent.Context> context
    ) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleThaumatoriumRecipes(packet)
        );
        context.get().setPacketHandled(true);
    }
}
