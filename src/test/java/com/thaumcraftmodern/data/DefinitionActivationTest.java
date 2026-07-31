package com.thaumcraftmodern.data;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionActivationTest {
    @Test
    void absentAndFalseFlagsRemainActive() {
        assertFalse(DefinitionActivation.isInactive(new JsonObject()));

        JsonObject explicit = new JsonObject();
        explicit.addProperty("inactive", false);
        assertFalse(DefinitionActivation.isInactive(explicit));
    }

    @Test
    void trueFlagSkipsDefinition() {
        JsonObject json = new JsonObject();
        json.addProperty("inactive", true);

        assertTrue(DefinitionActivation.isInactive(json));
    }
}
