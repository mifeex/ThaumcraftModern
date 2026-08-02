package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicTubeMachineShapeTest {
    @Test
    void bufferAndCentrifugeUseClassicHalfBlockTubeBounds() throws Exception {
        assertClassicShape("EssentiaBufferBlock.java");
        assertClassicShape("EssentiaCentrifugeBlock.java");
    }

    private static void assertClassicShape(String file) throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/" + file));

        assertTrue(source.contains("box(4, 4, 4, 12, 12, 12)"));
        assertTrue(source.contains("getShape(BlockState state"));
        assertTrue(source.contains("getCollisionShape(BlockState state"));
        assertTrue(source.contains("return CLASSIC_TUBE_SHAPE;"));
    }
}
