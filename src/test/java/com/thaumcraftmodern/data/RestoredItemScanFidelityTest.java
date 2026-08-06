package com.thaumcraftmodern.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RestoredItemScanFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/scans"
    );

    @Test
    void legacyDyeItemsRetainIndependentSensusScans() throws IOException {
        Set<String> targets = Set.of(
                "black_dye", "blue_dye", "brown_dye", "cyan_dye",
                "gray_dye", "green_dye", "light_blue_dye",
                "light_gray_dye", "lime_dye", "magenta_dye", "orange_dye",
                "pink_dye", "purple_dye", "red_dye", "white_dye",
                "yellow_dye", "lapis_lazuli", "ink_sac", "cocoa_beans",
                "bone_meal"
        );

        for (String target : targets) {
            JsonObject scan = json(SCANS.resolve(
                    "vanilla_dyes/" + target + ".json"
            ));
            assertEquals("item", scan.get("type").getAsString());
            assertEquals("minecraft:" + target, scan.get("target").getAsString());
            assertEquals(Map.of("sensus", 1), aspects(scan));
        }

        assertEquals(
                targets.size(),
                targets.stream().map(target -> "item:minecraft:" + target)
                        .distinct().count(),
                "every split 1.20.1 dye identity must have its own scan key"
        );
    }

    @Test
    void directlyRegisteredThaumcraftObjectsMatchOriginalAspects()
            throws IOException {
        assertScan("thaumometer.json", "item", "thaumcraftmodern:thaumometer",
                Map.of("sensus", 3, "metallum", 2, "vitreus", 1,
                        "praecantatio", 1));
        assertScan("scribing_tools.json", "item",
                "thaumcraftmodern:scribing_tools",
                Map.of("aqua", 1, "tenebrae", 1, "instrumentum", 1));
        assertScan("thaumium_block.json", "block",
                "thaumcraftmodern:thaumium_block",
                Map.of("metallum", 8, "praecantatio", 2));
        assertScan("flesh_block.json", "block",
                "thaumcraftmodern:flesh_block",
                Map.of("corpus", 4, "lux", 1, "praecantatio", 1));
    }

    @Test
    void newlyMappedLegacyResourcesStayActiveAndUnique() throws IOException {
        Map<String, JsonObject> active = new LinkedHashMap<>();
        try (var paths = Files.walk(SCANS)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json"))
                    .toList()) {
                JsonObject scan = json(path);
                if (scan.has("inactive") && scan.get("inactive").getAsBoolean()) {
                    continue;
                }
                String key = scan.get("type").getAsString() + ":"
                        + scan.get("target").getAsString();
                assertFalse(active.containsKey(key), "duplicate active scan: " + key);
                active.put(key, scan);
            }
        }

        List<String> restored = List.of(
                "block:thaumcraftmodern:tallow_candle",
                "item:thaumcraftmodern:quicksilver_nugget",
                "item:thaumcraftmodern:cultist_knight_helmet",
                "item:thaumcraftmodern:cultist_knight_chestplate",
                "item:thaumcraftmodern:cultist_knight_leggings",
                "item:thaumcraftmodern:cultist_cleric_hood",
                "item:thaumcraftmodern:cultist_cleric_robe",
                "item:thaumcraftmodern:cultist_cleric_leggings",
                "item:thaumcraftmodern:cultist_praetor_helmet",
                "item:thaumcraftmodern:cultist_praetor_chestplate",
                "item:thaumcraftmodern:cultist_praetor_leggings",
                "item:thaumcraftmodern:cultist_boots",
                "item:thaumcraftmodern:native_iron_cluster",
                "item:thaumcraftmodern:native_copper_cluster",
                "item:thaumcraftmodern:native_tin_cluster",
                "item:thaumcraftmodern:native_silver_cluster",
                "item:thaumcraftmodern:native_lead_cluster",
                "item:thaumcraftmodern:native_gold_cluster",
                "item:thaumcraftmodern:beef_nugget",
                "item:thaumcraftmodern:chicken_nugget",
                "item:thaumcraftmodern:pork_nugget",
                "item:thaumcraftmodern:fish_nugget",
                "item:thaumcraftmodern:apprentice_ring_aer",
                "block:thaumcraftmodern:arcane_ear",
                "entity:thaumcraftmodern:primal_orb",
                "entity:thaumcraftmodern:straw_golem",
                "item_tag:thaumcraftmodern:thaumcraft_banners",
                "block_tag:thaumcraftmodern:eldritch_structure_blocks"
        );
        restored.forEach(key -> assertTrue(active.containsKey(key),
                () -> "missing restored original scan: " + key));
    }

    private static void assertScan(
            String file,
            String type,
            String target,
            Map<String, Integer> expectedAspects
    ) throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals(type, scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertEquals(expectedAspects, aspects(scan));
    }

    private static Map<String, Integer> aspects(JsonObject scan) {
        Map<String, Integer> result = new LinkedHashMap<>();
        JsonArray aspects = scan.getAsJsonArray("aspects");
        aspects.forEach(element -> {
            JsonObject aspect = element.getAsJsonObject();
            result.merge(
                    aspect.get("id").getAsString(),
                    aspect.get("amount").getAsInt(),
                    Integer::sum
            );
        });
        return result;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
