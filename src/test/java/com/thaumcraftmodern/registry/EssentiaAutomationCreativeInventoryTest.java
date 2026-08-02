package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EssentiaAutomationCreativeInventoryTest {
    @Test
    void completeAutomationVerticalIsDiscoverableInCreativeInventory()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        for (String item : List.of(
                "ESSENTIA_BUFFER",
                "VOID_JAR",
                "ESSENTIA_CENTRIFUGE",
                "ESSENTIA_CRYSTALLIZER",
                "MNEMONIC_MATRIX"
        )) {
            assertTrue(
                    source.contains("output.accept(ModItems." + item + ".get());"),
                    item + " is missing from the Thaumcraft creative tab"
            );
        }
        assertFalse(source.contains("ModItems.ARCANE_RECIPE_COMPONENTS.forEach("));
        assertFalse(source.contains("AspectRegistryRuntime.catalog()"));
        assertFalse(source.contains("EssentiaCrystalItem.create("));
        assertTrue(source.contains(
                "output.accept(ModItems.ESSENTIA_CRYSTAL.get());"
        ));
        assertFalse(source.contains("output.accept(ModItems.RESEARCH_TABLE.get());"));
        assertFalse(source.contains("output.accept(ModItems.LOOT_URN.get());"));
        assertFalse(source.contains("output.accept(ModItems.LOOT_CRATE.get());"));
        assertFalse(source.contains("output.accept(ModItems.TAINTED_LEAVES.get());"));
    }
}
