package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvertedVillagerFidelityTest {
    @Test
    void convertedVillagerIsNonTradingAndUsesLowerVillagerVoice() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"));
        assertTrue(source.contains("kind == LegacyMobKind.CONVERTED_VILLAGER"));
        assertTrue(source.contains("SoundEvents.VILLAGER_AMBIENT"));
        assertTrue(source.contains("case CONVERTED_VILLAGER -> 0.72F"));
        assertTrue(source.contains("return InteractionResult.PASS"));
    }

    @Test
    void rendererHasSeparateApronCapeAndPauldrons() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/render/ConvertedVillagerModel.java"));
        assertTrue(source.contains("\"right_pauldron\""));
        assertTrue(source.contains("\"left_pauldron\""));
        String renderer = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/client/render/ConvertedVillagerRenderer.java"));
        assertTrue(renderer.contains("villager/type/plains.png"));
        assertTrue(renderer.contains("villager/profession/leatherworker.png"));
    }
}
