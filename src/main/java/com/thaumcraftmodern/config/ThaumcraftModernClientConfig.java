package com.thaumcraftmodern.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-only presentation settings.
 *
 * <p>The research debug switch deliberately affects only Thaumonomicon
 * rendering. It never completes research, bypasses server-side prerequisites
 * or mutates player knowledge.</p>
 */
public final class ThaumcraftModernClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue DEBUG_SHOW_ALL_RESEARCH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("research");
        DEBUG_SHOW_ALL_RESEARCH = builder
                .comment(
                        "Debug only: show every registered research node in the Thaumonomicon.",
                        "This does not unlock research or change saved player knowledge."
                )
                .define("debugShowAllResearch", false);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumcraftModernClientConfig() {
    }

    public static boolean debugShowAllResearch() {
        return DEBUG_SHOW_ALL_RESEARCH.get();
    }
}
