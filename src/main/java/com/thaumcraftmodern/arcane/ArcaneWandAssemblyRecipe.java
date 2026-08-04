package com.thaumcraftmodern.arcane;

import com.google.gson.JsonObject;
import com.thaumcraftmodern.item.WandComponentItem;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.wand.WandCapDefinition;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.wand.WandRodDefinition;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** TC4's dynamic ArcaneWandRecipe and ArcaneSceptreRecipe. */
public final class ArcaneWandAssemblyRecipe implements ArcaneRecipe {
    private final ResourceLocation id;
    private final boolean sceptre;

    public ArcaneWandAssemblyRecipe(ResourceLocation id, boolean sceptre) {
        this.id = id;
        this.sceptre = sceptre;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return components(container).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        Components components = components(container).orElse(null);
        if (components == null) return ItemStack.EMPTY;
        WandItem output = sceptre
                ? (WandItem) ModItems.CRAFTING_SCEPTRE.get()
                : components.rod().staff()
                        ? (WandItem) ModItems.GREATWOOD_STAFF.get()
                        : (WandItem) ModItems.CASTING_WAND.get();
        return output.create(components.rod().id(), components.cap().id());
    }

    @Override
    public ArcaneVisCost visCost(CraftingContainer container) {
        Components components = components(container).orElse(null);
        if (components == null) return ArcaneVisCost.EMPTY;
        int cost = classicCraftCost(
                components.cap().craftCostVis(),
                components.rod().craftCostVis(),
                sceptre);
        LinkedHashMap<String, Integer> amounts = new LinkedHashMap<>();
        for (String primal : ArcaneVisCost.PRIMALS) amounts.put(primal, cost);
        return new ArcaneVisCost(amounts);
    }

    public static int classicCraftCost(
            int capCraftCost,
            int rodCraftCost,
            boolean sceptre
    ) {
        if (capCraftCost <= 0 || rodCraftCost <= 0) {
            throw new IllegalArgumentException(
                    "wand component craft costs must be positive");
        }
        int base = Math.multiplyExact(capCraftCost, rodCraftCost);
        return sceptre ? (int) (base * 1.5F) : base;
    }

    @Override
    public List<String> requiredResearchIds(CraftingContainer container) {
        Components components = components(container).orElse(null);
        if (components == null) return List.of();
        ArrayList<String> required = new ArrayList<>();
        if (sceptre) required.add("sceptre");
        required.add(components.cap().researchId());
        required.add(components.rod().researchId());
        return List.copyOf(required);
    }

    private Optional<Components> components(CraftingContainer container) {
        if (container.getWidth() != 3 || container.getHeight() != 3) {
            return Optional.empty();
        }
        int[] capSlots = sceptre ? new int[]{1, 5, 6} : new int[]{2, 6};
        int[] occupied = sceptre ? new int[]{1, 2, 4, 5, 6} : new int[]{2, 4, 6};
        for (int slot = 0; slot < 9; slot++) {
            boolean expected = false;
            for (int candidate : occupied) expected |= slot == candidate;
            if (container.getItem(slot).isEmpty() == expected) {
                return Optional.empty();
            }
        }
        if (sceptre && !container.getItem(2).is(ModItems.SALIS_MUNDUS.get())) {
            return Optional.empty();
        }
        String capId = capId(container.getItem(capSlots[0])).orElse(null);
        if (capId == null) return Optional.empty();
        for (int slot : capSlots) {
            if (!capId(container.getItem(slot)).filter(capId::equals).isPresent()) {
                return Optional.empty();
            }
        }
        String rodId = rodId(container.getItem(4)).orElse(null);
        if (rodId == null) return Optional.empty();
        WandCapDefinition cap = WandComponentRegistry.cap(capId).orElse(null);
        WandRodDefinition rod = WandComponentRegistry.rod(rodId).orElse(null);
        if (cap == null || rod == null || sceptre && rod.staff()) {
            return Optional.empty();
        }
        if (!sceptre && "iron".equals(cap.id()) && "wood".equals(rod.id())) {
            return Optional.empty();
        }
        return Optional.of(new Components(cap, rod));
    }

    private static Optional<String> capId(ItemStack stack) {
        return componentId(stack, WandComponentItem.Kind.CAP);
    }

    private static Optional<String> rodId(ItemStack stack) {
        if (stack.is(Items.STICK)) return Optional.of("wood");
        return componentId(stack, WandComponentItem.Kind.ROD);
    }

    private static Optional<String> componentId(
            ItemStack stack,
            WandComponentItem.Kind kind
    ) {
        return stack.getItem() instanceof WandComponentItem component
                && component.kind() == kind
                ? Optional.of(component.componentId()) : Optional.empty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return sceptre
                ? ModItems.CRAFTING_SCEPTRE.get().getDefaultInstance()
                : ModItems.CASTING_WAND.get().getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(sceptre ? 5 : 3, Ingredient.EMPTY);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return sceptre
                ? ModArcaneRecipes.ARCANE_SCEPTRE_ASSEMBLY_SERIALIZER.get()
                : ModArcaneRecipes.ARCANE_WAND_ASSEMBLY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get();
    }

    @Override
    public String researchId() {
        return "";
    }

    @Override
    public ArcaneVisCost visCost() {
        return ArcaneVisCost.EMPTY;
    }

    private record Components(WandCapDefinition cap, WandRodDefinition rod) {
    }

    public static final class Serializer
            implements RecipeSerializer<ArcaneWandAssemblyRecipe> {
        private final boolean sceptre;

        public Serializer(boolean sceptre) {
            this.sceptre = sceptre;
        }

        @Override
        public ArcaneWandAssemblyRecipe fromJson(
                ResourceLocation id, JsonObject json) {
            return new ArcaneWandAssemblyRecipe(id, sceptre);
        }

        @Override
        public ArcaneWandAssemblyRecipe fromNetwork(
                ResourceLocation id, FriendlyByteBuf buffer) {
            return new ArcaneWandAssemblyRecipe(id, sceptre);
        }

        @Override
        public void toNetwork(
                FriendlyByteBuf buffer, ArcaneWandAssemblyRecipe recipe) {
        }
    }
}
