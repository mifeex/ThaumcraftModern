package com.thaumcraftmodern.client.screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Exact TC4 placement contract for the contents drawn inside the Crucible.
 */
final class ThaumonomiconCrucibleRecipeLayout {
    static final int WIDTH = 112;
    static final int CONTENT_OFFSET_Y = -10;
    static final int OUTPUT_TOP = 28;
    static final int CATALYST_X = 26;
    static final int CATALYST_Y = 72;
    static final int ARROW_X = 42;
    static final int ARROW_Y = 76;
    static final int CAULDRON_TOP = 92;
    static final int ASPECT_ORIGIN_X = 28;
    static final int ASPECT_ORIGIN_Y = 128;
    static final int ASPECTS_PER_ROW = 3;
    static final int ASPECT_STEP = 20;

    private ThaumonomiconCrucibleRecipeLayout() {
    }

    static int left(int pageLeft, int pageWidth) {
        return pageLeft + (pageWidth - WIDTH) / 2;
    }

    /**
     * Ports TC4 {@code drawAspectGrid}: rows straddle the vertical anchor.
     * Its legacy centering condition centers one/two total aspects and the
     * incomplete final row of grids with at least three rows, while retaining
     * the original left alignment for four/five-aspect grids.
     */
    static List<Slot> aspectSlots(int count) {
        if (count <= 0) {
            return List.of();
        }
        int rowsBeforeLast = (count - 1) / ASPECTS_PER_ROW;
        int finalRowShift =
                (ASPECTS_PER_ROW - count % ASPECTS_PER_ROW) * 10;
        int firstY = ASPECT_ORIGIN_Y - 10 * rowsBeforeLast;
        List<Slot> slots = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int row = index / ASPECTS_PER_ROW;
            int column = index % ASPECTS_PER_ROW;
            boolean centerFinalRow = row >= rowsBeforeLast
                    && (rowsBeforeLast > 1 || count < ASPECTS_PER_ROW);
            slots.add(new Slot(
                    index,
                    ASPECT_ORIGIN_X
                            + column * ASPECT_STEP
                            + (centerFinalRow ? finalRowShift : 0),
                    firstY + row * ASPECT_STEP
            ));
        }
        return List.copyOf(slots);
    }

    record Slot(int index, int x, int y) {
    }
}
