package com.thaumcraftmodern.scan;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class CopperModernIdentityContentTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/scans/legacy"
    );

    @Test
    void copperFormsUseCurrentRegistryIdentities() throws IOException {
        assertScan(
                "object_032_nuggetcopper.json",
                "item",
                "thaumcraftmodern:copper_nugget"
        );
        assertScan(
                "object_033_ingotcopper.json",
                "item",
                "minecraft:copper_ingot"
        );
        assertScan(
                "object_035_raw_copper.json",
                "item",
                "minecraft:raw_copper"
        );
        assertScan(
                "object_035_orecopper.json",
                "block",
                "minecraft:copper_ore"
        );
        assertScan(
                "object_035_deepslate_copper_ore.json",
                "block",
                "minecraft:deepslate_copper_ore"
        );
    }

    @Test
    void copperCapRecipeUsesTheCurrentForgeNuggetTag() throws IOException {
        JsonObject recipe = json(ROOT.resolve(
                "src/main/resources/data/thaumcraftmodern/recipes/"
                        + "wand_cap_copper.json"
        ));
        assertEquals(
                "forge:nuggets/copper",
                recipe.getAsJsonObject("key")
                        .getAsJsonObject("N")
                        .get("tag")
                        .getAsString()
        );
    }

    private static void assertScan(String file, String type, String target)
            throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals(type, scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertFalse(scan.get("inactive").getAsBoolean());
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
