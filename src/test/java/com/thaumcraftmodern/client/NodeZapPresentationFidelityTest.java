package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NodeZapPresentationFidelityTest {
    private static final Path MAIN = Path.of("src/main/java/com/thaumcraftmodern");

    @Test
    void nodeBullyingUsesOneContinuousClientBoltAndClassicSound()
            throws Exception {
        String ticker = Files.readString(MAIN.resolve(
                "aura/AuraNodeServerTicker.java"));
        String renderer = Files.readString(MAIN.resolve(
                "client/render/ClientNodeZapRenderer.java"));
        assertTrue(ticker.contains("new NodeZapPacket(from, to"));
        assertTrue(ticker.contains("ModSounds.ZAP.get()"));
        assertFalse(ticker.contains("ParticleTypes.ELECTRIC_SPARK"));
        assertTrue(renderer.contains("LIFETIME_TICKS = 10"));
        assertTrue(renderer.contains("ClassicBoltRenderTypes.bolt"));
        assertTrue(renderer.contains("0.5F"));
        assertTrue(renderer.contains("Vec3[] offsets"));
    }
}
