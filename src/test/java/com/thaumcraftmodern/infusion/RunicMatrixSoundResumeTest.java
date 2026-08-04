package com.thaumcraftmodern.infusion;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RunicMatrixSoundResumeTest {
    @Test
    void clientRestartsOriginalInfuserCadenceAfterChunkLoad()
            throws Exception {
        String matrix = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/RunicMatrixBlockEntity.java"
        ));
        assertTrue(matrix.contains("if (rawLevel.isClientSide)"));
        assertTrue(matrix.contains("clientSoundTicks == 0"));
        assertTrue(matrix.contains("clientSoundTicks % 65 == 0"));
        assertTrue(matrix.contains("ModSounds.INFUSER_START.get()"));
        assertTrue(matrix.contains("ModSounds.INFUSER.get()"));

        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/RunicMatrixBlock.java"
        ));
        assertTrue(block.contains("RunicMatrixBlockEntity::serverTick"));
        assertTrue(block.contains("return createTickerHelper("));
    }
}
