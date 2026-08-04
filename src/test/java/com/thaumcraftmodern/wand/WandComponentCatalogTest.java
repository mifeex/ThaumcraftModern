package com.thaumcraftmodern.wand;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WandComponentCatalogTest {
    @Test
    void classicMinimalCatalogKeepsExactProperties() {
        WandComponentCatalog catalog = fixtures();

        assertEquals(25, catalog.rod("wood").orElseThrow().capacityVis());
        assertEquals(100, catalog.rod("silverwood").orElseThrow().capacityVis());
        assertEquals(
                1.1F,
                catalog.cap("iron").orElseThrow().costModifier()
        );
    }

    @Test
    void duplicateAndInvalidDefinitionsAreRejected() {
        WandRodDefinition wood = rod("wood", 25);
        WandCapDefinition iron = cap("iron", 1.1F);

        assertThrows(
                IllegalArgumentException.class,
                () -> new WandComponentCatalog(
                        List.of(wood, rod("wood", 100)),
                        List.of(iron)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WandComponentCatalog(List.of(), List.of(iron))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WandRodDefinition("Wood", 25, "wand.wood")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WandCapDefinition("iron", 0.0F, "wand.iron")
        );
    }

    @Test
    void networkCatalogRoundTripPreservesDataDrivenProperties() {
        WandComponentRegistry.replace(
                fixtures().rods(),
                fixtures().caps()
        );

        WandComponentCatalog restored = WandComponentRegistry.deserialize(
                WandComponentRegistry.serialize()
        );

        assertEquals(25, restored.rod("wood").orElseThrow().capacityVis());
        assertEquals(
                100,
                restored.rod("silverwood").orElseThrow().capacityVis()
        );
        assertEquals(
                1.1F,
                restored.cap("iron").orElseThrow().costModifier()
        );
        assertEquals(1, restored.rod("wood").orElseThrow().craftCostVis());
        assertEquals("cap_iron",
                restored.cap("iron").orElseThrow().researchId());
    }

    static WandComponentCatalog fixtures() {
        return new WandComponentCatalog(
                List.of(rod("wood", 25), rod("silverwood", 100)),
                List.of(cap("iron", 1.1F))
        );
    }

    private static WandRodDefinition rod(String id, int capacity) {
        return new WandRodDefinition(id, capacity, "wand.rod." + id);
    }

    private static WandCapDefinition cap(String id, float modifier) {
        return new WandCapDefinition(id, modifier, "wand.cap." + id);
    }
}
