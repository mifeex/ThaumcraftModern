package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.research.HexResearchPuzzle;
import com.thaumcraftmodern.research.ResearchPuzzleRegistry;
import com.thaumcraftmodern.research.ResearchColorResolver;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class ResearchNotesItem extends Item {
    public static final String FIRST_DISCOVERY = "first_discovery";
    private static final String RESEARCH_KEY = "Research";
    private static final String PLACEMENTS_KEY = "Placements";
    private static final String CELLS_KEY = "Cells";

    public ResearchNotesItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String researchId) {
        return create(researchId, RandomSource.create(researchId.hashCode()));
    }

    public static ItemStack create(String researchId, RandomSource random) {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
        ItemStack stack = new ItemStack(ModItems.RESEARCH_NOTES.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(RESEARCH_KEY, researchId);
        writeLayout(stack, researchId, random);
        tag.put(PLACEMENTS_KEY, new ListTag());
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
            tag.put(PLACEMENTS_KEY, new ListTag());
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

    /** TC4 colours tint layer 1 with the first aspect of the research tags. */
    public static int color(ItemStack stack) {
        return ResearchColorResolver.color(researchId(stack));
    }

    public static HexResearchPuzzle loadPuzzle(
            ItemStack stack,
            AspectCatalog catalog,
            PlayerThaumKnowledge knowledge
    ) {
        ensureInitialized(stack);
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(CELLS_KEY, Tag.TAG_LIST)) {
            String researchId = researchId(stack);
            ResearchRegistry.find(researchId)
                    .filter(research -> !research.researchCost().isEmpty())
                    .ifPresent(research -> writeLayout(
                            stack,
                            researchId,
                            RandomSource.create(researchId.hashCode())
                    ));
        }
        HexResearchPuzzle puzzle;
        if (root.contains(CELLS_KEY, Tag.TAG_LIST)) {
            LinkedHashSet<HexResearchPuzzle.Cell> cells = new LinkedHashSet<>();
            LinkedHashMap<HexResearchPuzzle.Cell, String> anchors = new LinkedHashMap<>();
            ListTag layout = root.getList(CELLS_KEY, Tag.TAG_COMPOUND);
            for (int index = 0; index < layout.size(); index++) {
                CompoundTag cellTag = layout.getCompound(index);
                HexResearchPuzzle.Cell cell = new HexResearchPuzzle.Cell(
                        cellTag.getInt("Q"), cellTag.getInt("R")
                );
                cells.add(cell);
                if (cellTag.contains("Anchor", Tag.TAG_STRING)) {
                    anchors.put(cell, cellTag.getString("Anchor"));
                }
            }
            puzzle = new HexResearchPuzzle(catalog, cells, anchors);
        } else {
            puzzle = new HexResearchPuzzle(catalog);
        }

        if (root.contains(PLACEMENTS_KEY, Tag.TAG_LIST)) {
            ListTag placements = root.getList(PLACEMENTS_KEY, Tag.TAG_COMPOUND);
            for (int index = 0; index < placements.size(); index++) {
                CompoundTag placement = placements.getCompound(index);
                puzzle.restorePlacement(
                        new HexResearchPuzzle.Cell(
                                placement.getInt("Q"), placement.getInt("R")
                        ),
                        placement.getString("Aspect")
                );
            }
        } else if (root.contains(PLACEMENTS_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag placements = root.getCompound(PLACEMENTS_KEY);
            for (int q = -1; q <= 1; q++) {
                String key = Integer.toString(q);
                if (placements.contains(key)) {
                    puzzle.restorePlacement(
                            new HexResearchPuzzle.Cell(q, 0),
                            placements.getString(key)
                    );
                }
            }
        }
        return puzzle;
    }

    public static void savePuzzle(ItemStack stack, HexResearchPuzzle puzzle) {
        ensureInitialized(stack);
        ListTag placements = new ListTag();
        for (Map.Entry<HexResearchPuzzle.Cell, String> placement
                : puzzle.placements().entrySet()) {
            if (!puzzle.isAnchor(placement.getKey())) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Q", placement.getKey().q());
                entry.putInt("R", placement.getKey().r());
                entry.putString("Aspect", placement.getValue());
                placements.add(entry);
            }
        }
        stack.getOrCreateTag().put(PLACEMENTS_KEY, placements);
    }

    private static void writeLayout(
            ItemStack stack,
            String researchId,
            RandomSource random
    ) {
        var research = ResearchRegistry.find(researchId).orElse(null);
        HexResearchPuzzle.Layout layout = research == null
                ? HexResearchPuzzle.classicLayout(1, List.of(), random)
                : HexResearchPuzzle.classicLayout(
                        ResearchPuzzleRegistry.complexity(researchId),
                        research.researchCost(),
                        random
                );
        ListTag cells = new ListTag();
        for (HexResearchPuzzle.Cell cell : layout.cells()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Q", cell.q());
            entry.putInt("R", cell.r());
            String anchor = layout.anchors().get(cell);
            if (anchor != null) entry.putString("Anchor", anchor);
            cells.add(entry);
        }
        stack.getOrCreateTag().put(CELLS_KEY, cells);
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
