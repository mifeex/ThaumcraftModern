package com.thaumcraftmodern.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Maps dense visual slots to stable server palette indices.
 */
public final class ResearchTablePaletteLayout {
    public static final int ROWS = 5;
    public static final int COLUMNS = 5;
    public static final int CAPACITY = ROWS * COLUMNS;
    public static final int PAGE_STEP = ROWS;

    private ResearchTablePaletteLayout() {
    }

    public static List<Integer> visibleIndices(
            List<String> completePalette,
            Set<String> knownAspects
    ) {
        List<Integer> result = new ArrayList<>();
        for (int index = 0; index < completePalette.size(); index++) {
            if (knownAspects.contains(completePalette.get(index))) {
                result.add(index);
            }
        }
        return List.copyOf(result);
    }

    public static List<Integer> visibleIndices(
            List<String> completePalette,
            Set<String> knownAspects,
            int page
    ) {
        List<Integer> known = visibleIndices(completePalette, knownAspects);
        int safePage = Math.max(0, Math.min(page, maxPage(known.size())));
        int start = safePage * PAGE_STEP;
        int end = Math.min(start + CAPACITY, known.size());
        return List.copyOf(known.subList(start, end));
    }

    public static int maxPage(int knownAspectCount) {
        if (knownAspectCount <= CAPACITY) {
            return 0;
        }
        return (knownAspectCount - CAPACITY + PAGE_STEP - 1) / PAGE_STEP;
    }
}
