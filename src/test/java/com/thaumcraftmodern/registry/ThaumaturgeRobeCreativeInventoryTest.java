package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumaturgeRobeCreativeInventoryTest {
    @Test
    void everyCraftableRobePieceIsListedInThaumcraftTab() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));
        for (String item : new String[]{
                "THAUMATURGE_ROBE",
                "THAUMATURGE_LEGGINGS",
                "THAUMATURGE_BOOTS"
        }) {
            assertTrue(source.contains("output.accept(ModItems."
                    + item + ".get());"));
        }
    }
}
