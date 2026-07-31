package com.thaumcraftmodern.wand;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisConsumptionRoutingTest {
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void everyImplementedVisExpenseUsesPlayerAwareSharedService()
            throws Exception {
        String workbench = source(
                "com/thaumcraftmodern/world/menu/ArcaneWorkbenchMenu.java"
        );
        String structures = source(
                "com/thaumcraftmodern/construction/"
                        + "ClassicStructureConstructionEvents.java"
        );
        String nodeJar = source(
                "com/thaumcraftmodern/nodejar/HeldCastingToolPayment.java"
        );

        assertTrue(workbench.contains(
                "WandVisService.consume(player, wandStack()"
        ));
        assertTrue(structures.contains(
                "WandVisService.consume(player, wand, cost)"
        ));
        assertTrue(nodeJar.contains(
                "WandVisService.consume(player, expectedStack, costById)"
        ));
        assertFalse(nodeJar.contains("SILVERWOOD_ROD_ID"));
    }

    @Test
    void modifierAggregatorIncludesGearEffectsAndExtensionEvent()
            throws Exception {
        String service = source(
                "com/thaumcraftmodern/wand/VisDiscountService.java"
        );

        assertTrue(service.contains("player.getArmorSlots()"));
        assertTrue(service.contains("player.getActiveEffects()"));
        assertTrue(service.contains("new VisDiscountEvent("));
        assertTrue(service.contains("MinecraftForge.EVENT_BUS.post(event)"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
