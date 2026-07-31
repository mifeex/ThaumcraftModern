package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalVis;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WandVisServiceTest {
    private static final WandCapDefinition IRON =
            new WandCapDefinition("iron", 1.1F, "wand.cap.iron");

    @Test
    void ironCapAppliesClassicOnePointOneModifierInCentivis() {
        Map<String, Integer> adjusted =
                WandVisService.adjustedCostCentivis(
                        IRON,
                        Map.of("terra", 1, "ignis", 70)
                );

        assertEquals(110, adjusted.get("terra"));
        assertEquals(7700, adjusted.get("ignis"));
        assertEquals(0, adjusted.get("aer"));
        assertEquals(6, adjusted.size());
    }

    @Test
    void wornGogglesReduceIronCapFromOnePointOneToOnePointZeroFive() {
        Map<String, Integer> adjusted =
                WandVisService.adjustedCostCentivis(
                        IRON,
                        Map.of("terra", 1, "ignis", 70),
                        5
                );

        assertEquals(105, adjusted.get("terra"));
        assertEquals(7350, adjusted.get("ignis"));
    }

    @Test
    void signedEffectPenaltyCanOutweighGogglesAndRaiseCost() {
        Map<String, Integer> adjusted =
                WandVisService.adjustedCostCentivis(
                        IRON,
                        Map.of("ignis", 70),
                        -5
                );

        assertEquals(8050, adjusted.get("ignis"));
    }

    @Test
    void classicFormsKeepSceptreCapacityDiscountAndStaffRestrictions() {
        assertEquals(150, WandForm.SCEPTRE.applyCapacity(100));
        assertEquals(10, WandForm.SCEPTRE.inherentDiscountPercent());
        assertTrue(WandForm.SCEPTRE.isCraftingTool());
        assertFalse(WandForm.SCEPTRE.acceptsFocus());

        assertEquals(250, WandForm.STAFF.applyCapacity(250));
        assertFalse(WandForm.STAFF.isCraftingTool());
        assertTrue(WandForm.STAFF.acceptsFocus());
    }

    @Test
    void copperAndSilverCapsUseClassicAspectSpecificModifiers() {
        WandCapDefinition copper = new WandCapDefinition(
                "copper",
                1.1F,
                "wand.cap.copper",
                List.of("ordo", "perditio"),
                1.0F
        );
        WandCapDefinition silver = new WandCapDefinition(
                "silver",
                1.0F,
                "wand.cap.silver",
                List.of("aer", "terra", "ignis", "aqua"),
                0.95F
        );

        assertEquals(
                100,
                WandVisService.adjustedCostCentivis(
                        copper,
                        Map.of("ordo", 1)
                ).get("ordo")
        );
        assertEquals(
                110,
                WandVisService.adjustedCostCentivis(
                        copper,
                        Map.of("aer", 1)
                ).get("aer")
        );
        assertEquals(
                95,
                WandVisService.adjustedCostCentivis(
                        silver,
                        Map.of("ignis", 1)
                ).get("ignis")
        );
    }

    @Test
    void nodeJarCostCannotFitWoodButCanFitChargedSilverwood() {
        Map<String, Integer> nodeJar = uniformCost(70);
        WandState wooden = uniformState("wood", 2500);
        WandState silverwood = uniformState("silverwood", 7700);

        assertTrue(
                WandVisService.consumeState(wooden, IRON, nodeJar).isEmpty()
        );
        WandState paid = WandVisService.consumeState(
                silverwood,
                IRON,
                nodeJar
        ).orElseThrow();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            assertEquals(0, paid.visCentivis(aspect));
        }
    }

    @Test
    void failedMultiAspectPaymentIsAtomic() {
        EnumMap<PrimalAspect, Integer> values =
                new EnumMap<>(PrimalAspect.class);
        values.putAll(PrimalVis.uniform(7350));
        values.put(PrimalAspect.PERDITIO, 7349);
        WandState before = new WandState(
                WandStateCodec.SERIAL_VERSION,
                "silverwood",
                "iron",
                values
        );

        Optional<WandState> result = WandVisService.consumeState(
                before,
                IRON,
                uniformCost(70),
                5
        );

        assertTrue(result.isEmpty());
        assertEquals(7350, before.visCentivis(PrimalAspect.AER));
        assertEquals(7349, before.visCentivis(PrimalAspect.PERDITIO));
    }

    @Test
    void negativeAndNonPrimalCostsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WandVisService.adjustedCostCentivis(
                        IRON,
                        Map.of("aer", -1)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WandVisService.adjustedCostCentivis(
                        IRON,
                        Map.of("lux", 1)
                )
        );
    }

    private static WandState uniformState(String rodId, int centivis) {
        return new WandState(
                WandStateCodec.SERIAL_VERSION,
                rodId,
                "iron",
                PrimalVis.uniform(centivis)
        );
    }

    private static Map<String, Integer> uniformCost(int wholeVis) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect.id(), wholeVis);
        }
        return result;
    }
}
