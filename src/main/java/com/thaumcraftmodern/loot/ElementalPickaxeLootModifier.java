package com.thaumcraftmodern.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thaumcraftmodern.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class ElementalPickaxeLootModifier extends LootModifier {
    public static final Codec<ElementalPickaxeLootModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance).apply(instance, ElementalPickaxeLootModifier::new));
    public static final float BASE_CHANCE = 0.20F;
    public static final float FORTUNE_BONUS = 0.075F;

    public ElementalPickaxeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || !(tool.getItem() instanceof com.thaumcraftmodern.item.ElementalPickaxeItem)) return loot;
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        float chance = BASE_CHANCE + fortune * FORTUNE_BONUS;
        for (int index = 0; index < loot.size(); index++) {
            ItemStack original = loot.get(index);
            ItemStack replacement = replacement(original);
            if (!replacement.isEmpty() && context.getRandom().nextFloat() <= chance) {
                replacement.setCount(replacement.getCount() * original.getCount());
                loot.set(index, replacement);
            }
        }
        return loot;
    }

    static ItemStack replacement(ItemStack stack) {
        if (stack.is(Items.RAW_IRON)) return new ItemStack(ModItems.NATIVE_IRON_CLUSTER.get());
        if (stack.is(Items.RAW_GOLD)) return new ItemStack(ModItems.NATIVE_GOLD_CLUSTER.get());
        if (stack.is(Items.RAW_COPPER)) return new ItemStack(ModItems.NATIVE_COPPER_CLUSTER.get());
        return ItemStack.EMPTY;
    }

    @Override public Codec<? extends net.minecraftforge.common.loot.IGlobalLootModifier> codec() { return CODEC; }
}
