package com.thaumcraftmodern.data;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

/**
 * Shared opt-out flag for definitions that are preserved in data packs before
 * their runtime dependencies have been implemented.
 */
public final class DefinitionActivation {
    private DefinitionActivation() {
    }

    public static boolean isInactive(JsonObject json) {
        return GsonHelper.getAsBoolean(json, "inactive", false);
    }
}
