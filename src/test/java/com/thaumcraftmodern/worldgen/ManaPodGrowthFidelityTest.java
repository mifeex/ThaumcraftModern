package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ManaPodGrowthFidelityTest {
    private static final Path ROOT = Path.of("src/main");

    @Test
    void naturalGrowthMatchesTc4AndDoesNotRequireCropLight()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/world/block/ManaPodBlock.java"
        ));
        assertTrue(source.contains("extends CropBlock"));
        assertTrue(source.contains("random.nextInt(30) == 0"));
        assertTrue(source.contains("getStateForAge(age + 1)"));
        assertFalse(source.contains("getRawBrightness"));
    }

    @Test
    void wildPodsUseEveryTc4PostGrowthStage() throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVegetationFeature.java"
        ));
        assertTrue(source.contains("3 + random.nextInt(5)"));
    }

    @Test
    void allEightAgesHaveModels() throws IOException {
        JsonObject variants = read(
                "resources/assets/thaumcraftmodern/blockstates/mana_pod.json"
        ).getAsJsonObject("variants");
        assertEquals(8, variants.size());
        for (int age = 0; age <= 7; age++) {
            assertTrue(variants.has("age=" + age));
        }
    }

    @Test
    void youngPodsDropNothingAndRipePodsDropOneOrTwoBeans()
            throws IOException {
        String loot = Files.readString(ROOT.resolve(
                "resources/data/thaumcraftmodern/loot_tables/blocks/"
                        + "mana_pod.json"
        ));
        assertFalse(loot.contains("\"age\": \"0\""));
        assertFalse(loot.contains("\"age\": \"1\""));
        for (int age = 2; age <= 7; age++) {
            assertTrue(loot.contains("\"age\": \"" + age + "\""));
        }
        assertTrue(loot.contains("\"chance\": 0.67"));
        assertFalse(loot.contains("\"max\": 2"));
    }

    @Test
    void manaBeanReplantsBelowWoodInMagicalBiomes()
            throws IOException {
        String source = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/item/ManaBeanItem.java"
        ));
        assertTrue(source.contains("Direction.DOWN"));
        assertTrue(source.contains("\"forge\", \"is_magical\""));
        assertTrue(source.contains("pod.canSurvive(level, position)"));
        assertTrue(source.contains("context.getItemInHand().shrink(1)"));
    }

    private static JsonObject read(String relative) throws IOException {
        return JsonParser.parseString(
                Files.readString(ROOT.resolve(relative))
        ).getAsJsonObject();
    }
}
