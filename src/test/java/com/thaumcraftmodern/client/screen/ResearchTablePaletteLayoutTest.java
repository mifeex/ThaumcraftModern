package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResearchTablePaletteLayoutTest {
    @Test
    void unknownAspectsDoNotReserveVisualSlots() {
        List<String> completePalette = List.of(
                "aer",
                "terra",
                "ignis",
                "lux",
                "motus",
                "humanus"
        );

        assertEquals(
                List.of(0, 2, 5),
                ResearchTablePaletteLayout.visibleIndices(
                        completePalette,
                        Set.of("aer", "ignis", "humanus")
                )
        );
    }

    @Test
    void paletteIsClippedToFiveByFiveAndMovesOneColumnPerPage() {
        List<String> palette = java.util.stream.IntStream.range(0, 32)
                .mapToObj(index -> "aspect_" + index)
                .toList();
        Set<String> known = Set.copyOf(palette);

        assertEquals(2, ResearchTablePaletteLayout.maxPage(32));
        assertEquals(
                java.util.stream.IntStream.range(0, 25).boxed().toList(),
                ResearchTablePaletteLayout.visibleIndices(palette, known, 0)
        );
        assertEquals(
                java.util.stream.IntStream.range(5, 30).boxed().toList(),
                ResearchTablePaletteLayout.visibleIndices(palette, known, 1)
        );
        assertEquals(
                java.util.stream.IntStream.range(10, 32).boxed().toList(),
                ResearchTablePaletteLayout.visibleIndices(palette, known, 2)
        );
    }

    @Test
    void arrowsAreNotNeededWhileAllAspectsFitInsideTheFrame() {
        assertEquals(0, ResearchTablePaletteLayout.maxPage(25));
        assertEquals(1, ResearchTablePaletteLayout.maxPage(26));
    }
}
