package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.WardedJarItemClientExtensions;
import com.thaumcraftmodern.essentia.WardedJarContents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** TC4 filled warded jar item: exact contents, label and in-GUI liquid. */
public class WardedJarItem extends BlockItem {
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    public WardedJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(WardedJarItemClientExtensions.create());
    }

    public static ItemStack withContents(WardedJarItem item, CompoundTag payload) {
        ItemStack stack = new ItemStack(item);
        stack.addTagElement(BLOCK_ENTITY_TAG, payload.copy());
        return stack;
    }

    public static Optional<WardedJarContents> contents(ItemStack stack) {
        if (!(stack.getItem() instanceof WardedJarItem) || !stack.hasTag()) {
            return Optional.empty();
        }
        CompoundTag payload = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (payload == null) return Optional.empty();
        return WardedJarContents.read(payload);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        contents(stack).ifPresent(contents -> {
            if (contents.amount() > 0 && contents.aspect() != null) {
                // ItemJarFilled.addInformation: "<aspect name> x<amount>".
                tooltip.add(Component.translatable("tc.aspect." + contents.aspect())
                        .append(" x" + contents.amount()));
            }
            if (contents.filter() != null) {
                tooltip.add(Component.translatable("tc.aspect." + contents.filter())
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
        super.appendHoverText(stack, level, tooltip, flag);
    }

}
