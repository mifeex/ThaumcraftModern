package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ObsidianBaseCreativeInventoryTest {
    @Test
    void obsidianBaseHasBlockItemAndCreativeTabEntry() throws Exception {
        String items = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModItems.java"
        ));
        String creativeTab = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertTrue(items.contains(
                "blockItem(\"obsidian_tile\", ModBlocks.OBSIDIAN_TILE)"
        ));
        assertTrue(creativeTab.contains(
                "output.accept(ModItems.OBSIDIAN_TILE.get())"
        ));
    }
}
