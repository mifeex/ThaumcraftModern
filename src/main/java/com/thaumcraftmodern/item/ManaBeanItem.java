package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Replants a mana bean as a young pod below valid wood in magical biomes.
 */
public final class ManaBeanItem extends Item {
    public static final String ASPECT_KEY = "Aspect";
    private static final TagKey<Biome> MAGICAL_BIOMES = TagKey.create(
            Registries.BIOME,
            new ResourceLocation("forge", "is_magical")
    );

    public ManaBeanItem(Properties properties) {
        super(properties);
    }

    public static Optional<String> aspect(ItemStack stack) {
        if (!(stack.getItem() instanceof ManaBeanItem)
                || stack.getTag() == null) {
            return Optional.empty();
        }
        String stored = stack.getTag().getString(ASPECT_KEY);
        return AspectRegistryRuntime.find(stored).isPresent()
                ? Optional.of(stored) : Optional.empty();
    }

    public static void setAspect(ItemStack stack, String aspectId) {
        if (!(stack.getItem() instanceof ManaBeanItem)
                || AspectRegistryRuntime.find(aspectId).isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putString(ASPECT_KEY, aspectId);
    }

    public static int color(ItemStack stack) {
        Optional<String> stored = aspect(stack);
        if (stored.isPresent()) {
            return AspectRegistryRuntime.find(stored.get())
                    .map(AspectDefinition::color).orElse(0xFFFFFF);
        }
        List<AspectDefinition> definitions = definitions();
        return definitions.isEmpty() ? 0xFFFFFF
                : definitions.get((int) ((System.currentTimeMillis() / 500L)
                % definitions.size())).color();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.DOWN) {
            return InteractionResult.PASS;
        }
        var level = context.getLevel();
        BlockPos position = context.getClickedPos().below();
        if (!level.isEmptyBlock(position)
                || !level.getBiome(position).is(MAGICAL_BIOMES)) {
            return InteractionResult.PASS;
        }
        BlockState pod = ModBlocks.MANA_POD.get().defaultBlockState();
        if (!pod.canSurvive(level, position)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(position, pod, 3);
            if (level.getBlockEntity(position)
                    instanceof ManaPodBlockEntity manaPod) {
                manaPod.setAspect(aspect(context.getItemInHand()).orElse(null));
            }
            if (context.getPlayer() == null
                    || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slot,
            boolean selected
    ) {
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

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        aspect(stack).ifPresent(aspectId -> tooltip.add(
                Component.translatable("tc.aspect." + aspectId)
                        .withStyle(ChatFormatting.DARK_PURPLE)
        ));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static void assignRandomAspect(
            ItemStack stack,
            RandomSource random
    ) {
        List<AspectDefinition> definitions = definitions();
        if (!definitions.isEmpty()) {
            setAspect(
                    stack,
                    definitions.get(random.nextInt(definitions.size())).id()
            );
        }
    }

    private static List<AspectDefinition> definitions() {
        return AspectRegistryRuntime.find("aer").isEmpty()
                ? List.of()
                : AspectRegistryRuntime.catalog().definitions();
    }
}
