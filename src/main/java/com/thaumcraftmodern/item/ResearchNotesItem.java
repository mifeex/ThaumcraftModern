package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.research.HexResearchPuzzle;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ResearchNotesItem extends Item {
    public static final String FIRST_DISCOVERY = "first_discovery";
    private static final String RESEARCH_KEY = "Research";
    private static final String PLACEMENTS_KEY = "Placements";

    public ResearchNotesItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String researchId) {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
        ItemStack stack = new ItemStack(ModItems.RESEARCH_NOTES.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(RESEARCH_KEY, researchId);
        tag.put(PLACEMENTS_KEY, new CompoundTag());
        return stack;
    }

    public static void ensureInitialized(ItemStack stack) {
        if (!(stack.getItem() instanceof ResearchNotesItem)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(RESEARCH_KEY)) {
            tag.putString(RESEARCH_KEY, FIRST_DISCOVERY);
        }
        if (!tag.contains(PLACEMENTS_KEY)) {
            tag.put(PLACEMENTS_KEY, new CompoundTag());
        }
    }

    public static String researchId(ItemStack stack) {
        ensureInitialized(stack);
        return stack.getOrCreateTag().getString(RESEARCH_KEY);
    }

    public static boolean matchesResearch(ItemStack stack, String researchId) {
        return stack.getItem() instanceof ResearchNotesItem
                && researchId != null
                && researchId.equals(researchId(stack));
    }

    public static HexResearchPuzzle loadPuzzle(
            ItemStack stack,
            AspectCatalog catalog,
            PlayerThaumKnowledge knowledge
    ) {
        ensureInitialized(stack);
        HexResearchPuzzle puzzle = new HexResearchPuzzle(catalog);
        CompoundTag placements = stack.getOrCreateTag().getCompound(PLACEMENTS_KEY);
        for (int q = HexResearchPuzzle.MIN_Q + 1; q < HexResearchPuzzle.MAX_Q; q++) {
            String key = Integer.toString(q);
            if (placements.contains(key)) {
                puzzle.restorePlacement(q, placements.getString(key));
            }
        }
        return puzzle;
    }

    public static void savePuzzle(ItemStack stack, HexResearchPuzzle puzzle) {
        ensureInitialized(stack);
        CompoundTag placements = new CompoundTag();
        for (Map.Entry<Integer, String> placement : puzzle.placements().entrySet()) {
            if (!puzzle.isAnchor(placement.getKey())) {
                placements.putString(Integer.toString(placement.getKey()), placement.getValue());
            }
        }
        stack.getOrCreateTag().put(PLACEMENTS_KEY, placements);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.thaumcraftmodern.research_notes")
                .withStyle(ChatFormatting.DARK_PURPLE));
        ResearchRegistry.find(researchId(stack)).ifPresent(research ->
                tooltip.add(
                        Component.translatable(research.titleKey())
                                .withStyle(ChatFormatting.GRAY)
                )
        );
    }
}
