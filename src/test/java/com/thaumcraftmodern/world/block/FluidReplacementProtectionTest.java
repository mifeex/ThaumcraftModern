package com.thaumcraftmodern.world.block;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidReplacementProtectionTest {
    @Test
    void jarsAndEveryAuraNodeFamilyRejectFluidReplacement() throws IOException {
        for (String source : new String[]{
                "world/block/EssentiaJarBlock.java",
                "world/block/VoidJarBlock.java",
                "world/block/CrystalClusterBlock.java",
                "aura/AuraNodeBlock.java",
                "visnet/EnergizedAuraNodeBlock.java",
                "nodejar/JarredAuraNodeBlock.java"
        }) {
            String text = Files.readString(Path.of(
                    "src/main/java/com/thaumcraftmodern/", source));
            assertTrue(text.contains("canBeReplaced(BlockState state, Fluid fluid)")
                            && text.contains("return false;"),
                    source + " must not be displaced by flowing water");
        }
    }
}
