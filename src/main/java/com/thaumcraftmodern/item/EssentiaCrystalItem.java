package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** One TC4 crystal essence carrying exactly one server-authored aspect. */
public final class EssentiaCrystalItem extends Item {
    private static final String ASPECT_KEY = "Aspect";

    public EssentiaCrystalItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(EssentiaCrystalItem item, String aspect) {
        if (AspectRegistryRuntime.find(aspect).isEmpty()) {
            throw new IllegalArgumentException("Unknown essentia crystal aspect: " + aspect);
        }
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString(ASPECT_KEY, aspect);
        return stack;
    }

    public static Optional<String> aspect(ItemStack stack) {
        if (!(stack.getItem() instanceof EssentiaCrystalItem)) return Optional.empty();
        CompoundTag tag = stack.getTag();
        if (tag == null) return Optional.empty();
        String aspect = tag.getString(ASPECT_KEY);
        return AspectRegistryRuntime.find(aspect).isPresent()
                ? Optional.of(aspect) : Optional.empty();
    }

    public static int color(ItemStack stack) {
        Optional<String> stored = aspect(stack);
        if (stored.isPresent()) {
            return AspectRegistryRuntime.find(stored.get())
                    .map(definition -> definition.color()).orElse(0xFFFFFF);
        }
        if (AspectRegistryRuntime.find("aer").isEmpty()) return 0xFFFFFF;
        List<com.thaumcraftmodern.aspect.AspectDefinition> aspects =
                AspectRegistryRuntime.catalog().definitions();
        if (aspects.isEmpty()) return 0xFFFFFF;
        int index = (int) ((System.currentTimeMillis() / 500L) % aspects.size());
        return aspects.get(index).color();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity,
            int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && aspect(stack).isEmpty()) {
            assignRandomAspect(stack, level.random);
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide && aspect(stack).isEmpty()) {
            assignRandomAspect(stack, level.random);
        }
    }

    private static void assignRandomAspect(ItemStack stack, RandomSource random) {
        if (AspectRegistryRuntime.find("aer").isEmpty()) return;
        List<com.thaumcraftmodern.aspect.AspectDefinition> aspects =
                AspectRegistryRuntime.catalog().definitions();
        if (!aspects.isEmpty()) {
            stack.getOrCreateTag().putString(
                    ASPECT_KEY,
                    aspects.get(random.nextInt(aspects.size())).id()
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        aspect(stack).ifPresent(aspect -> tooltip.add(
                Component.translatable("tc.aspect." + aspect)
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
