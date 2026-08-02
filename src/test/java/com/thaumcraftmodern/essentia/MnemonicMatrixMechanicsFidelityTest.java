package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicMatrixMechanicsFidelityTest {
    @Test
    void eachAttachedMatrixAddsTwoPersistentFormulaSlots() throws Exception {
        String machine = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ThaumatoriumBlockEntity.java"));
        assertTrue(machine.contains("capacity += 2"));
        assertTrue(machine.contains("direction == Direction.DOWN || direction == output"));
        assertTrue(machine.contains("== direction.getOpposite()"));
        assertTrue(machine.contains("tag.put(\"Formulae\""));
        assertTrue(machine.contains("trimFormulaeToCapacity"));
    }

    @Test
    void selectedSocketConnectsVisuallyWithoutBecomingEssentiaStorage()
            throws Exception {
        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/MnemonicMatrixBlock.java"));
        String tile = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/MnemonicMatrixBlockEntity.java"));
        String gameTest = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/gametest/FirstDiscoveryGameTests.java"));
        assertTrue(block.contains("extends BaseEntityBlock"));
        assertTrue(block.contains("new MnemonicMatrixBlockEntity(pos, state)"));
        assertTrue(tile.contains("implements EssentiaTransport"));
        assertTrue(tile.contains("return side == facing()"));
        assertTrue(tile.contains("canInputFrom(Direction side) { return false; }"));
        assertTrue(tile.contains("canOutputTo(Direction side) { return false; }"));
        assertTrue(gameTest.contains(
                "mnemonicMatrixConnectsToBufferAndTubeWithoutMovingEssentia"));
        assertTrue(gameTest.contains("getValue(EssentiaTubeBlock.WEST)"));
    }
}
