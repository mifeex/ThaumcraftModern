package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumatoriumMenuSyncFidelityTest {
    @Test
    void reservedEssentiaUsesContainerDataInsteadOfClientBlockEntityPolling()
            throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ThaumatoriumMenu.java"
        ));
        assertTrue(source.contains("new ContainerData()"));
        assertTrue(source.contains("addDataSlots(reservedData)"));
        assertTrue(source.contains("synchronizedReserved[index]"));
        assertTrue(source.contains("machine.reservedEssentia().getOrDefault(aspect, 0)"));
        assertTrue(source.contains("Math.max("));
    }
}
