package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyMobHurtMergeTest {
    @Test
    void oneHurtOverrideRetainsEveryLegacyBranch() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/"
                        + "LegacyThaumcraftMob.java"
        ));
        Matcher declarations = Pattern.compile(
                "public\\s+boolean\\s+hurt\\s*\\(DamageSource\\s+source,"
        ).matcher(source);
        int count = 0;
        while (declarations.find()) {
            count++;
        }
        assertEquals(1, count, "LegacyThaumcraftMob must have one hurt override");
        for (String behavior : new String[]{
                "ELDRITCH_CONSTRUCT",
                "ELDRITCH_CRAB",
                "FIREBAT",
                "FURIOUS_ZOMBIE",
                "ELDRITCH_GUARDIAN",
                "WISP",
                "PechBehavior.ANGER_HORIZONTAL_RANGE",
                "stopCrimsonRitual",
                "alertNearbyCrimsonCultists"
        }) {
            assertTrue(source.contains(behavior), behavior);
        }
    }
}
