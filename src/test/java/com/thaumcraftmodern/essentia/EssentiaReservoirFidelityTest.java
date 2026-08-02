package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaReservoirFidelityTest {
    @Test
    void reservoirKeepsTc4CapacityPortAndTransferRules() throws Exception {
        String block = read("src/main/java/com/thaumcraftmodern/world/block/EssentiaReservoirBlock.java");
        String tile = read("src/main/java/com/thaumcraftmodern/world/block/entity/EssentiaReservoirBlockEntity.java");
        String gameTest = read("src/main/java/com/thaumcraftmodern/gametest/FirstDiscoveryGameTests.java");

        assertTrue(tile.contains("CAPACITY = 256"));
        assertTrue(tile.contains("SUCTION = 24"));
        assertTrue(tile.contains("side == facing()"));
        assertTrue(tile.contains("ticks % 5 == 0"));
        assertTrue(tile.contains("EssentiaConnections.neighbour("));
        assertTrue(block.contains("context.getClickedFace().getOpposite()"));
        assertTrue(block.contains("player.isShiftKeyDown()"));
        assertTrue(gameTest.contains(
                "reservoirConnectsToBufferAndTubeThroughSelectedFace"));
    }

    @Test
    void reservoirUsesUnmodifiedClassicShellAndTextures() throws Exception {
        Path original = Path.of("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures");
        Path port = Path.of("src/main/resources/assets/thaumcraftmodern/textures");
        assertArrayEquals(Files.readAllBytes(original.resolve("models/reservoir.obj")),
                Files.readAllBytes(port.resolve("models/reservoir.obj")));
        assertArrayEquals(Files.readAllBytes(original.resolve("models/reservoir.png")),
                Files.readAllBytes(port.resolve("models/reservoir.png")));
        assertArrayEquals(Files.readAllBytes(original.resolve("blocks/essentiareservoir.png")),
                Files.readAllBytes(port.resolve("block/essentiareservoir.png")));

        String state = read("src/main/resources/assets/thaumcraftmodern/blockstates/essentia_reservoir.json");
        String shell = read("src/main/resources/assets/thaumcraftmodern/models/block/essentia_reservoir_shell.json");
        assertTrue(shell.contains("reservoir.obj"));
        assertTrue(shell.contains("[0.5, 0.5, 0]"));
        assertTrue(state.contains("\"facing\": \"north\""));
        assertTrue(state.contains("\"facing\": \"down\""));
        assertTrue(state.contains("\"x\": 90"));
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
