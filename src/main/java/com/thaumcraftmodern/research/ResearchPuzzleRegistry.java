package com.thaumcraftmodern.research;

import java.util.LinkedHashMap;
import java.util.Map;

/** Reloadable original TC4 research complexity index used to generate notes. */
public final class ResearchPuzzleRegistry {
    private static volatile Map<String, Integer> complexities = Map.of();

    private ResearchPuzzleRegistry() {
    }

    public static void replace(Map<String, Integer> values) {
        complexities = Map.copyOf(new LinkedHashMap<>(values));
    }

    public static int complexity(String researchId) {
        return complexities.getOrDefault(researchId, 1);
    }
}
