package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisRelayPresentationTest {
    @Test
    void dominantAspectTracksTheLargestCurrentAmount() {
        EnumMap<PrimalAspect, Integer> amounts =
                new EnumMap<>(PrimalAspect.class);
        amounts.put(PrimalAspect.AER, 4);
        amounts.put(PrimalAspect.TERRA, 11);
        amounts.put(PrimalAspect.IGNIS, 7);

        assertEquals(PrimalAspect.TERRA,
                VisNetworkNodeBlockEntity.dominantAspect(
                        aspect -> amounts.getOrDefault(aspect, 0)));

        amounts.put(PrimalAspect.TERRA, 3);
        amounts.put(PrimalAspect.IGNIS, 12);
        assertEquals(PrimalAspect.IGNIS,
                VisNetworkNodeBlockEntity.dominantAspect(
                        aspect -> amounts.getOrDefault(aspect, 0)));
    }

    @Test
    void dominantAspectUsesStablePrimalOrderForTies() {
        assertEquals(PrimalAspect.AER,
                VisNetworkNodeBlockEntity.dominantAspect(aspect -> 8));
        assertNull(VisNetworkNodeBlockEntity.dominantAspect(aspect -> 0));
    }

    @Test
    void beamBandsUseOnlyPresentPrimalsAndGiveLargestPoolMostBands() {
        EnumMap<PrimalAspect, Integer> amounts =
                new EnumMap<>(PrimalAspect.class);
        amounts.put(PrimalAspect.AER, 2);
        amounts.put(PrimalAspect.TERRA, 9);
        amounts.put(PrimalAspect.IGNIS, 4);

        List<PrimalAspect> bands = VisNetworkNodeBlockEntity.beamAspectBands(
                aspect -> amounts.getOrDefault(aspect, 0), 30);

        assertEquals(30, bands.size());
        assertEquals(18, bands.stream()
                .filter(aspect -> aspect == PrimalAspect.TERRA).count());
        assertEquals(8, bands.stream()
                .filter(aspect -> aspect == PrimalAspect.IGNIS).count());
        assertEquals(4, bands.stream()
                .filter(aspect -> aspect == PrimalAspect.AER).count());
    }

    @Test
    void emptySourceProducesNoColourBands() {
        assertEquals(List.of(), VisNetworkNodeBlockEntity.beamAspectBands(
                aspect -> 0, 24));
    }

    @Test
    void attunedBeamUsesOnlyItsAspectAndDisappearsWhenItIsEmpty()
            throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "VisRelayBlockEntityRenderer.java"
        ));

        assertTrue(renderer.contains("palette = List.of(attunedAspect)"));
        assertTrue(renderer.contains(
                "if (tile.availableVis(attunedAspect) <= 0)"));
        assertTrue(renderer.contains("boolean fixedAttunement ="));
    }
}
