package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumaturgeRobeSleeveClosureTest {
    private static final Path ROOT = Path.of("src/main");

    @Test
    void robeOnlyNarrowsTheFullLengthSleeves() throws Exception {
        String item = Files.readString(ROOT.resolve(
                "java/com/thaumcraftmodern/item/ThaumaturgeRobeItem.java"
        ));
        assertTrue(item.contains("initializeClient"));
        assertFalse(item.contains("getHumanoidArmorModel"));
        Path modelPath = ROOT.resolve(
                "java/com/thaumcraftmodern/client/render/ThaumaturgeRobeArmorModel.java"
        );
        String model = Files.readString(modelPath);
        assertTrue(model.contains("new CubeDeformation(0.5F)"));
        assertTrue(model.contains("4.0F, 12.0F, 4.0F"));
        assertFalse(model.contains("connector"));
        assertFalse(model.contains("cuff"));
    }
}
