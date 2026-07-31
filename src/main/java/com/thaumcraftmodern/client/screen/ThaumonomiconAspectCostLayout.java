package com.thaumcraftmodern.client.screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic centered wrapping for Thaumonomicon aspect-cost strips.
 */
final class ThaumonomiconAspectCostLayout {
    static final int ICON_SIZE = 16;
    static final int COLUMN_STEP = 18;
    static final int ROW_STEP = 18;

    private ThaumonomiconAspectCostLayout() {
    }

    static List<Slot> arrange(int count, int width, int bottomY) {
        if (count <= 0 || width < ICON_SIZE) {
            return List.of();
        }
        int maxColumns = Math.max(
                1,
                (width - ICON_SIZE) / COLUMN_STEP + 1
        );
        int columns = Math.min(count, maxColumns);
        int rows = (count + columns - 1) / columns;
        int firstY = bottomY - ICON_SIZE - (rows - 1) * ROW_STEP;
        List<Slot> result = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int row = index / columns;
            int column = index % columns;
            int rowCount = Math.min(columns, count - row * columns);
            int rowWidth = ICON_SIZE + (rowCount - 1) * COLUMN_STEP;
            int firstX = (width - rowWidth) / 2;
            result.add(new Slot(
                    index,
                    firstX + column * COLUMN_STEP,
                    firstY + row * ROW_STEP
            ));
        }
        return List.copyOf(result);
    }

    static int requiredHeight(int count, int width) {
        List<Slot> slots = arrange(count, width, 0);
        if (slots.isEmpty()) {
            return 0;
        }
        int minimumY = slots.stream().mapToInt(Slot::y).min().orElse(0);
        return -minimumY;
    }

    record Slot(int index, int x, int y) {
    }
}
