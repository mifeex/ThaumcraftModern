package com.thaumcraftmodern.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** TC4 jar label, optionally pre-tuned to one essentia aspect. */
public final class JarLabelItem extends Item {
    private static final String ASPECT_KEY = "Aspect";

    public JarLabelItem(Properties properties) {
        super(properties);
    }

    public static Optional<String> aspect(ItemStack stack) {
        if (!(stack.getItem() instanceof JarLabelItem) || !stack.hasTag()) {
            return Optional.empty();
        }
        String aspect = stack.getTag().getString(ASPECT_KEY);
        return aspect.isBlank() ? Optional.empty() : Optional.of(aspect);
    }

    public static ItemStack tuned(Item item, String aspect) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ASPECT_KEY, aspect);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        aspect(stack).ifPresent(aspect -> tooltip.add(Component.translatable(
                "tc.aspect." + aspect).withStyle(ChatFormatting.DARK_AQUA)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
