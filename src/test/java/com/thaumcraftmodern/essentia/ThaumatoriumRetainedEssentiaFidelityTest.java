package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumatoriumRetainedEssentiaFidelityTest {
    @Test
    void onlyCompletingACraftClearsTheStoredEssentia() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));

        assertEquals(1, occurrences(source, "reserved.clear();"));
    }

    @Test
    void removingTheCatalystExposesReservedEssentiaForPipeRefund() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ThaumatoriumBlockEntity.java"));

        assertTrue(source.contains("catalyst.isEmpty() && !reserved.isEmpty()"));
        assertTrue(source.contains("controller.reserved.remove(aspect, amount)"));
        assertTrue(source.contains("remote.suctionType(side.getOpposite())"));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
