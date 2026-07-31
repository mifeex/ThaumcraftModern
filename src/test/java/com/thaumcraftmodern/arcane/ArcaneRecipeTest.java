package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.aspect.AspectCost;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArcaneRecipeTest {
    @Test
    void parsesClassicArcaneStoneMetadataAndLayout() {
        JsonObject json = JsonParser.parseString("""
                {
                  "research": "arcanestone",
                  "vis": {"terra": 1, "ignis": 1},
                  "pattern": ["SSS", "SCS", "SSS"],
                  "key": {
                    "S": {"item": "minecraft:stone"},
                    "C": {"item": "minecraft:quartz"}
                  },
                  "result": {"item": "minecraft:diamond", "count": 9}
                }
                """).getAsJsonObject();

        assertEquals("arcanestone", ArcaneRecipeJson.researchId(json));
        assertEquals(1, ArcaneRecipeJson.visCost(json).amount("terra"));
        assertEquals(1, ArcaneRecipeJson.visCost(json).amount("ignis"));
        assertEquals(0, ArcaneRecipeJson.visCost(json).amount("aer"));
        assertEquals(
                java.util.List.of("SSS", "SCS", "SSS"),
                java.util.List.of(ArcaneRecipeJson.pattern(json))
        );
        assertEquals(
                "minecraft:quartz",
                json.getAsJsonObject("key")
                        .getAsJsonObject("C")
                        .get("item")
                        .getAsString()
        );
        assertEquals(9, json.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void validatesSixPrimalCostDomain() {
        ArcaneVisCost cost = new ArcaneVisCost(Map.of(
                "aer", 5,
                "ordo", 3
        ));

        assertEquals(5, cost.amount("aer"));
        assertEquals(3, cost.amount("ordo"));
        assertEquals(0, cost.amount("ignis"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ArcaneVisCost(Map.of("praecantatio", 1))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ArcaneVisCost(Map.of("aer", -1))
        );
    }

    @Test
    void rejectsUnknownPrimalFromJson() {
        JsonObject json = JsonParser.parseString("""
                {"aer": 1, "praecantatio": 1}
                """).getAsJsonObject();

        assertThrows(JsonSyntaxException.class, () -> ArcaneVisCost.fromJson(json));
    }

    @Test
    void exposesNonZeroVisAsTheCommonThaumonomiconAspectCostContract() {
        ArcaneVisCost cost = new ArcaneVisCost(Map.of(
                "aer", 5,
                "ordo", 3
        ));

        assertEquals(
                java.util.List.of(
                        new AspectCost("aer", 5),
                        new AspectCost("ordo", 3)
                ),
                cost.aspectCosts()
        );
    }
}
