package com.thaumcraftmodern.essentia;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HopperAutomationFidelityTest {
    @Test
    void alchemicalFurnaceKeepsOriginalSidedHopperSlots() throws Exception {
        String source = read("AlchemicalFurnaceBlockEntity.java");

        assertTrue(source.contains("private static final int[] BOTTOM_SLOTS = {FUEL_SLOT}"));
        assertTrue(source.contains("private static final int[] SIDE_SLOTS = {INPUT_SLOT}"));
        assertTrue(source.contains("private static final int[] TOP_SLOTS = {}"));
        assertTrue(source.contains("implements WorldlyContainer"));
        assertTrue(source.contains("side != Direction.DOWN && canPlaceItem(slot, stack)"));
        assertTrue(source.contains("stack.is(Items.BUCKET)"));
    }

    @Test
    void bothThaumatoriumHalvesExposeTheControllerCatalystToHoppers()
            throws Exception {
        String source = read("ThaumatoriumBlockEntity.java");

        assertTrue(source.contains("implements EssentiaTransport, WorldlyContainer"));
        assertTrue(source.contains("private static final int[] AUTOMATION_SLOTS = {0}"));
        assertTrue(source.contains("return AUTOMATION_SLOTS"));
        assertTrue(source.contains("controller.setItem(slot, stack)"));
        assertTrue(source.contains("controller.removeItem(slot, amount)"));
        assertTrue(source.contains("return slot == 0"));
    }

    private static String read(String file) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/" + file
        ));
    }
}
