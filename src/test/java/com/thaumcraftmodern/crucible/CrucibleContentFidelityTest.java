package com.thaumcraftmodern.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleContentFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void requiredResearchActivationMatchesImplementedVertical()
            throws IOException {
        assertFalse(research("crucible").get("inactive").getAsBoolean());
        assertFalse(research("nitor").get("inactive").getAsBoolean());
        assertFalse(research("alumentum").get("inactive").getAsBoolean());
        assertFalse(research("distilessentia").get("inactive").getAsBoolean());
        assertFalse(research("tubes").get("inactive").getAsBoolean());
        assertFalse(research("tubefilter").get("inactive").getAsBoolean());
        assertFalse(research("jarlabel").get("inactive").getAsBoolean());
    }

    @Test
    void classicRecipeCostsAndOutputsRemainDataDriven()
            throws IOException {
        JsonObject alumentum = crucibleRecipe("alumentum");
        assertEquals(
                "thaumcraftmodern:alumentum",
                alumentum.getAsJsonObject("output").get("item").getAsString()
        );
        assertEquals(
                3,
                alumentum.getAsJsonObject("aspects")
                        .get("perditio").getAsInt()
        );
        JsonObject nitor = crucibleRecipe("nitor");
        assertEquals(
                3,
                nitor.getAsJsonObject("aspects").get("lux").getAsInt()
        );
        assertTrue(Files.isRegularFile(ROOT.resolve(
                "data/thaumcraftmodern/recipes/salis_mundus.json"
        )));
    }

    @Test
    void originalCrucibleOutputAssetsArePresent() {
        for (String asset : new String[]{
                "textures/item/alumentum.png",
                "textures/item/nitor.png",
                "textures/item/shard_balanced.png",
                "textures/item/salis_mundus.png",
                "sounds/spill.ogg",
                "sounds/bubble1.ogg"
        }) {
            assertTrue(Files.isRegularFile(
                    ROOT.resolve("assets/thaumcraftmodern").resolve(asset)
            ), asset);
        }
    }

    private static JsonObject research(String id) throws IOException {
        return json(ROOT.resolve(
                "data/thaumcraftmodern/thaumcraft/research/legacy/"
                        + id + ".json"
        ));
    }

    private static JsonObject crucibleRecipe(String id) throws IOException {
        return json(ROOT.resolve(
                "data/thaumcraftmodern/thaumcraft/crucible_recipes/"
                        + id + ".json"
        ));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
